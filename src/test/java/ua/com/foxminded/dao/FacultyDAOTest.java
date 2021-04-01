package ua.com.foxminded.dao;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.any;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptException;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ua.com.foxminded.dao.exceptions.DAOException;
import ua.com.foxminded.domain.Faculty;
import ua.com.foxminded.mapper.FacultyMapper;
import ua.com.foxminded.settings.SpringTestConfiguration;
import ua.com.foxminded.settings.TestAppender;

@ContextConfiguration(classes = { SpringTestConfiguration.class})
@ExtendWith(SpringExtension.class)
class FacultyDAOTest {
    private final ClassPathResource testData = new ClassPathResource("/Test data.sql");
    private final ClassPathResource testTablesCreator = new ClassPathResource("/Creating tables.sql");
    private final ClassPathResource testDatabaseCleaner = new ClassPathResource("/Clearing database.sql");

    private TestAppender testAppender = new TestAppender();
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private FacultyDAO facultyDAO;
    private List<Faculty> expectedFaculties;
    private Connection connection;   

    @BeforeEach
    void setUp() throws ScriptException, SQLException {
        MockitoAnnotations.openMocks(this);
        connection = jdbcTemplate.getDataSource().getConnection();
        ScriptUtils.executeSqlScript(connection, testTablesCreator);

        expectedFaculties = new ArrayList<> (Arrays.asList(
                new Faculty(), new Faculty(), new Faculty()));
        List<String> facultyNames = new ArrayList<>(Arrays.asList(
                "TestFaculty1", "TestFaculty2", "TestFaculty3"));
        List<Integer> facultyIndexes = new ArrayList<>(Arrays.asList(
                1, 2, 3));
        for (int i = 0; i < expectedFaculties.size(); i++) {
            expectedFaculties.get(i).setId(facultyIndexes.get(i));
            expectedFaculties.get(i).setName(facultyNames.get(i));
        }
    }

    @AfterEach
    void tearDown() throws ScriptException, SQLException {
        testAppender.cleanEventList();
        ReflectionTestUtils.setField(facultyDAO, "jdbcTemplate", jdbcTemplate);
        ScriptUtils.executeSqlScript(connection, testDatabaseCleaner);
    }

    @Test
    void shouldCreateFaculty() {
        Faculty expectedFaculty = new Faculty();
        expectedFaculty.setId(1);
        expectedFaculty.setName("TestFaculty");
        Faculty testFaculty = new Faculty();
        testFaculty.setName("TestFaculty");
        facultyDAO.create(testFaculty);
        Faculty actualFaculty = facultyDAO.findAll().stream().findFirst().get();
        assertEquals(expectedFaculty, actualFaculty);
    }

    @Test
    void shouldFindAllFaculties() throws ScriptException, SQLException {
        ScriptUtils.executeSqlScript(connection, testData);
        List<Faculty> actualFaculties = facultyDAO.findAll();
        assertTrue(expectedFaculties.containsAll(actualFaculties) && actualFaculties.containsAll(expectedFaculties));
    }

    @Test
    void shouldFindFacultyById() throws ScriptException, SQLException {
        ScriptUtils.executeSqlScript(connection, testData);
        int checkedId = 2;
        Faculty expectedFaculty = new Faculty();
        expectedFaculty.setId(checkedId);
        expectedFaculty.setName("TestFaculty2");
        assertEquals(expectedFaculty, facultyDAO.findById(checkedId));
    }

    @Test
    void shouldUpdateFaculty() throws ScriptException, SQLException {
        ScriptUtils.executeSqlScript(connection, testData);
        int testId = 2;
        Faculty testFaculty = new Faculty();
        testFaculty.setName("TestFacultyUpdated");
        facultyDAO.update(testId, testFaculty);
        Faculty expectedFaculty = new Faculty();
        expectedFaculty.setId(testId);
        expectedFaculty.setName("TestFacultyUpdated");
        assertEquals(expectedFaculty, facultyDAO.findById(testId));
    }

    @Test
    void shouldDeleteFacultyById() throws ScriptException, SQLException {
        ScriptUtils.executeSqlScript(connection, testData);
        int deletedId = 2;
        for (int i = 0; i < expectedFaculties.size(); i++) {
            if (expectedFaculties.get(i).getId() == deletedId) {
                expectedFaculties.remove(i);
                i--;
            }
        }
        facultyDAO.deleteById(deletedId);
        List<Faculty> actualFaculties = facultyDAO.findAll();
        assertTrue(expectedFaculties.containsAll(actualFaculties) && actualFaculties.containsAll(expectedFaculties));
    }

    @Test
    void shouldGenerateLogsWhenCreateFaculty() {
        Faculty testFaculty = new Faculty();
        testFaculty.setName("Test Faculty");
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to insert new faculty: " + testFaculty + ".",
                "The faculty " + testFaculty + " was inserted."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        facultyDAO.create(testFaculty);
        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(actualLogs.size(), expectedLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenThrowDataAccessExceptionWhileCreate() {
        Faculty testFaculty = new Faculty();
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to insert new faculty: " + testFaculty + ".",
                "Can't create faculty: " + testFaculty + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }
        try {
            facultyDAO.create(testFaculty);
        } catch (DAOException exception) {
            //do nothing
        }

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(actualLogs.size(), expectedLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenFindAllIsEmpty() {
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.WARN));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to find all faculties.",
                "There are not any faculties in the result."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        facultyDAO.findAll();

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getMessage(), actualLogs.get(i).getMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenFindAllHasResult() {
        ScriptUtils.executeSqlScript(connection, testData);
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to find all faculties.",
                "The result is: " + expectedFaculties + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        facultyDAO.findAll();

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenThrowDataAccessExceptionWhileFindAll() {
        JdbcTemplate mockedJdbcTemplate = Mockito.mock(JdbcTemplate.class);
        ReflectionTestUtils.setField(facultyDAO, "jdbcTemplate", mockedJdbcTemplate);
        when(mockedJdbcTemplate.query(anyString(), any(FacultyMapper.class))).thenThrow(QueryTimeoutException.class);
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to find all faculties.",
                "Can't find all faculties."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            facultyDAO.findAll();
            verify(mockedJdbcTemplate).query(anyString(), any(FacultyMapper.class));
        } catch (DAOException daoException) {
            //do nothing
        }
        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getMessage(), actualLogs.get(i).getMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenFindById() {
        ScriptUtils.executeSqlScript(connection, testData);
        int testId = 2;
        Faculty expectedFaculty = expectedFaculties.stream().filter(faculty -> faculty.getId() == testId).findFirst().get();
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to find a faculty by id " + testId + ".",
                "The result faculty with id " + testId + " is " + expectedFaculty + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        facultyDAO.findById(testId);

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }
    
    @Test
    void shouldGenerateLogsWhenThrowEmptyResultDataAccessExceptionWhileFindById() {
        int testId = 1;
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to find a faculty by id " + testId + ".",
                "There is no result when find by id " + testId + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }
        
        try {
            facultyDAO.findById(testId);
        } catch (DAOException daoEcxeption) {
            //do nothing
        }
        
        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }
    
    @Test
    void shouldGenerateLogsWhenThrowDataAccessExceptionWhileFindById() {
        int testId = 1;
        JdbcTemplate mockedJdbcTemplate = Mockito.mock(JdbcTemplate.class);
        ReflectionTestUtils.setField(facultyDAO, "jdbcTemplate", mockedJdbcTemplate);
        when(mockedJdbcTemplate.queryForObject(anyString(), any(FacultyMapper.class), any())).thenThrow(QueryTimeoutException.class);
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to find a faculty by id " + testId + ".",
                "Can't find faculty by id " + testId + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }
        
        try {
            facultyDAO.findById(testId);
            verify(mockedJdbcTemplate).queryForObject(anyString(), any(FacultyMapper.class), any());
        } catch (DAOException daoEcxeption) {
            //do nothing
        }
        
        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenUpdate() {
        ScriptUtils.executeSqlScript(connection, testData);
        int testId = 30;
        Faculty testFaculty = new Faculty();
        testFaculty.setName("TestFaculty");

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to update faculty " + testFaculty + " with id " + testId + ".",
                "The faculty " + testFaculty + " with id " + testId +  " was changed."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        facultyDAO.update(testId, testFaculty);

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }
    
    @Test
    void shouldGenerateLogsWhenThrowDataAccessExceptionWhileUpdate() {
        int testId = 1;
        Faculty testFaculty = new Faculty();
        testFaculty.setName("TestFaculty");
        
        JdbcTemplate mockedJdbcTemplate = Mockito.mock(JdbcTemplate.class);
        ReflectionTestUtils.setField(facultyDAO, "jdbcTemplate", mockedJdbcTemplate);
        doThrow(QueryTimeoutException.class).when(mockedJdbcTemplate).update(anyString(), (Object) any());
        
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to update faculty " + testFaculty + " with id " + testId + ".",
                "Can't update faculty " + testFaculty + " by id " + testId +  "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }
        
        try {
            facultyDAO.update(testId, testFaculty);
            verify(mockedJdbcTemplate).update(anyString(), (Object) any());
        } catch (DAOException daoEcxeption) {
            //do nothing
        }
        
        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }  
    }

    @Test
    void shouldGenerateLogsWhenDeleteById() {
        ScriptUtils.executeSqlScript(connection, testData);
        int testId = 3;

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to delete faculty by id " + testId + ".",
                "The faculty with id " + testId +  " was deleted."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        facultyDAO.deleteById(testId);

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }
    
    @Test
    void shouldGenerateLogsWhenThrowDataAccessExceptionWhileDeleteById() {
        int testId = 3;

        JdbcTemplate mockedJdbcTemplate = Mockito.mock(JdbcTemplate.class);
        ReflectionTestUtils.setField(facultyDAO, "jdbcTemplate", mockedJdbcTemplate);
        doThrow(QueryTimeoutException.class).when(mockedJdbcTemplate).update(anyString(), anyInt());
        
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to delete faculty by id " + testId + ".",
                "Can't delete faculty by id " + testId +  "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }
        
        try {
            facultyDAO.deleteById(testId);
            verify(mockedJdbcTemplate).update(anyString(), anyInt());
        } catch (DAOException daoException) {
            //do nothing
        }

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }
}
