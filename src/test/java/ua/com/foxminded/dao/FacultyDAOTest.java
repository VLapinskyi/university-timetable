package ua.com.foxminded.dao;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptException;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ua.com.foxminded.domain.Faculty;
import ua.com.foxminded.settings.SpringTestConfiguration;
import ua.com.foxminded.settings.TestAppender;

@ContextConfiguration(classes = { SpringTestConfiguration.class})
@ExtendWith(SpringExtension.class)
class FacultyDAOTest {
    private final ClassPathResource testData = new ClassPathResource("/Test data.sql");
    private final ClassPathResource testTablesCreator = new ClassPathResource("/Creating tables.sql");
    private final ClassPathResource testDatabaseCleaner = new ClassPathResource("/Clearing database.sql");
    
    private TestAppender testAppender;
    @Autowired
    private FacultyDAO facultyDAO;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private List<Faculty> expectedFaculties;
    private Connection connection;   

    @BeforeEach
    void setUp() throws ScriptException, SQLException {
        testAppender = new TestAppender();
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
        testAppender = null;
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
    void shouldGenerateRightLogsWhenCreateFacultyWithId() {
        Faculty testFaculty = new Faculty();
        testFaculty.setId(100);
        testFaculty.setName("Test Faculty");
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.WARN, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to insert new faculty: \"" + testFaculty + "\"",
                "The faculty has already setted id: \"" + testFaculty.getId() + "\". The setted id will be ignored.",
                "The faculty \"" + testFaculty + "\" was inserted."));
        
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }
        
        facultyDAO.create(testFaculty);
        List<ILoggingEvent> actualLogs = testAppender.getEvents();
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getMessage(), actualLogs.get(i).getMessage());
        }
    }
    
    @Test
    void shouldGenerateRightLogsWhenCreateFacultyWithoutId() {
        Faculty testFaculty = new Faculty();
        testFaculty.setName("Test Faculty");
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to insert new faculty: \"" + testFaculty + "\"",
                "The faculty \"" + testFaculty + "\" was inserted."));
        
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }
        
        facultyDAO.create(testFaculty);
        List<ILoggingEvent> actualLogs = testAppender.getEvents();
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getMessage(), actualLogs.get(i).getMessage());
        }
    }
}
