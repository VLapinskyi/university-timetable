package ua.com.foxminded.dao;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
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
import ua.com.foxminded.domain.Group;
import ua.com.foxminded.mapper.FacultyMapper;
import ua.com.foxminded.mapper.GroupMapper;
import ua.com.foxminded.settings.SpringTestConfiguration;
import ua.com.foxminded.settings.TestAppender;

@ContextConfiguration(classes = { SpringTestConfiguration.class})
@ExtendWith(SpringExtension.class)
class GroupDAOTest {
    private final ClassPathResource testData = new ClassPathResource("/Test data.sql");
    private final ClassPathResource testTablesCreator = new ClassPathResource("/Creating tables.sql");
    private final ClassPathResource testDatabaseCleaner = new ClassPathResource("/Clearing database.sql");
    
    private TestAppender testAppender = new TestAppender();
    @Autowired
    private GroupDAO groupDAO;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private List<Group> expectedGroups;
    private Connection connection;
    @Mock
    private JdbcTemplate mockedJdbcTemplate;


    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        connection = jdbcTemplate.getDataSource().getConnection();
        ScriptUtils.executeSqlScript(connection, testTablesCreator);      
        expectedGroups = new ArrayList<>(Arrays.asList(
                new Group(), new Group(), new Group()));
        List<String> groupNames = new ArrayList<>(Arrays.asList(
                "TestGroup1", "TestGroup2", "TestGroup3"));
        List<Integer> groupIndexes = new ArrayList<>(Arrays.asList(
                1, 2, 3));
        for (int i = 0; i < expectedGroups.size(); i++) {
            expectedGroups.get(i).setId(groupIndexes.get(i));
            expectedGroups.get(i).setName(groupNames.get(i));
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        testAppender.cleanEventList();
        ReflectionTestUtils.setField(groupDAO, "jdbcTemplate", jdbcTemplate);
        ScriptUtils.executeSqlScript(connection, testDatabaseCleaner);
    }

    @Test
    void shouldCreateGroup() {
        Group testGroup = new Group();
        testGroup.setName("TestGroup");
        Group expectedGroup = new Group();
        expectedGroup.setId(1);
        expectedGroup.setName("TestGroup");
        groupDAO.create(testGroup);
        Group actualGroup = groupDAO.findAll().stream().findFirst().get();
        assertEquals(expectedGroup, actualGroup);
    }

    @Test
    void shouldFindAllGroups() throws ScriptException, SQLException {
        ScriptUtils.executeSqlScript(connection, testData);
        List<Group> actualGroups = groupDAO.findAll();
        assertTrue(expectedGroups.containsAll(actualGroups) && actualGroups.containsAll(expectedGroups));
    }

    @Test
    void shouldFindGroupById() throws ScriptException, SQLException {
        ScriptUtils.executeSqlScript(connection, testData);
        int checkedGroupId = 2;
        Group expectedGroup = new Group();
        expectedGroup.setId(checkedGroupId);
        expectedGroup.setName("TestGroup2");

        assertEquals(expectedGroup, groupDAO.findById(checkedGroupId));
    }

    @Test
    void shouldUpdateGroup() throws ScriptException, SQLException {
        ScriptUtils.executeSqlScript(connection, testData);
        int testGroupId = 2;        
        Group testGroup = new Group();
        testGroup.setName("TestGroupUpdated");
        groupDAO.update(testGroupId, testGroup);
        Group expectedGroup = new Group();
        expectedGroup.setId(testGroupId);
        expectedGroup.setName("TestGroupUpdated");
        assertEquals(expectedGroup, groupDAO.findById(testGroupId));
    }

    @Test
    void shouldDeleteGroupById() {
        ScriptUtils.executeSqlScript(connection, testData);
        int deletedGroupId = 2;
        for (int i = 0; i < expectedGroups.size(); i++) {
            if (expectedGroups.get(i).getId() == deletedGroupId) {
                expectedGroups.remove(i);
                i--;
            }
        }
        groupDAO.deleteById(deletedGroupId);
        List<Group> actualGroups = groupDAO.findAll();
        assertTrue(expectedGroups.containsAll(actualGroups) && actualGroups.containsAll(expectedGroups));
    }

    @Test
    void shouldSetGroupFaculty() {
        ScriptUtils.executeSqlScript(connection, testData);
        int facultyId = 1;
        Faculty faculty = new Faculty();
        faculty.setId(facultyId);
        faculty.setName("TestFaculty1");

        int groupId = 2;
        Group expectedGroup = expectedGroups.stream().filter(group -> group.getId() == groupId).findFirst().get();
        expectedGroup.setFaculty(faculty);

        groupDAO.setGroupFaculty(facultyId, groupId);
        Group actualGroup = groupDAO.findById(groupId);
        actualGroup.setFaculty(groupDAO.getGroupFaculty(groupId));
        assertEquals(expectedGroup, actualGroup);
    }

    @Test
    void shouldGetGroupFaculty() {
        ScriptUtils.executeSqlScript(connection, testData);
        int facultyId = 2;
        Faculty expectedFaculty = new Faculty();
        expectedFaculty.setId(facultyId);
        expectedFaculty.setName("TestFaculty2");

        int groupId = 2;
        Faculty actualFaculty = groupDAO.getGroupFaculty(groupId);
        assertEquals(expectedFaculty, actualFaculty);
    }

    @Test
    void shouldThrowDAOExceptionWhenDataAccessExceptionWhileCreate() {
        Group group = new Group();
        assertThrows(DAOException.class, () -> groupDAO.create(group));
    }
    
    @Test
    void shouldThrowDAOExceptionWhenDataAccessExceptionWhileFindAll() {
        ReflectionTestUtils.setField(groupDAO, "jdbcTemplate", mockedJdbcTemplate);
        when(mockedJdbcTemplate.query(anyString(), any(GroupMapper.class))).thenThrow(QueryTimeoutException.class);
        assertThrows(DAOException.class, () -> groupDAO.findAll());
    }
    
    @Test
    void shouldThrowDAOExceptionWhenEmptyResultDataAccessExceptionWhileFindById() {
        int testId = 1;
        assertThrows(DAOException.class, () -> groupDAO.findById(testId));
    }
    
    @Test
    void shouldThrowDAOExceptionWhenDataAccessExceptionWhileFindById() {
        int testId = 1;
        ReflectionTestUtils.setField(groupDAO, "jdbcTemplate", mockedJdbcTemplate);
        when(mockedJdbcTemplate.queryForObject(anyString(), any(GroupMapper.class), anyInt())).thenThrow(QueryTimeoutException.class);
        assertThrows(DAOException.class, () -> groupDAO.findById(testId));
    }
    
    @Test
    void shouldThrowDAOExceptionWhenDataAccessExceptionWhileUpdate() {
        int testId = 1;
        Group testGroup = new Group();
        ReflectionTestUtils.setField(groupDAO, "jdbcTemplate", mockedJdbcTemplate);
        doThrow(QueryTimeoutException.class).when(mockedJdbcTemplate).update(anyString(), (Object) any());
        assertThrows(DAOException.class, () -> groupDAO.update(testId, testGroup));
    }
    
    @Test
    void shouldThrowDAOExceptionWhenDataAccessExceptionWhileDeleteById() {
        int testId = 1;
        ReflectionTestUtils.setField(groupDAO, "jdbcTemplate", mockedJdbcTemplate);
        doThrow(QueryTimeoutException.class).when(mockedJdbcTemplate).update(anyString(), anyInt());
        assertThrows(DAOException.class, () -> groupDAO.deleteById(testId));
    }
    
    @Test
    void shouldThrowDAOExceptionWhenDataAccessExeceptionWhileSetGroupFaculty() {
        int facultyId = 1;
        int groupId = 2;
        ReflectionTestUtils.setField(groupDAO, "jdbcTemplate", mockedJdbcTemplate);
        doThrow(QueryTimeoutException.class).when(mockedJdbcTemplate).update(anyString(), anyInt(), anyInt());
        assertThrows(DAOException.class, () -> groupDAO.setGroupFaculty(facultyId, groupId));
    }
    
    @Test
    void shouldDAOExceptionWhenEmptyResultDataAccessExceptionWhileGetGroupFaculty() {
        int groupId = 1;
        assertThrows(DAOException.class, () -> groupDAO.getGroupFaculty(groupId));
    }
    
    @Test
    void shouldThrowDAOExceptionWhenDataAccessExceptionWhileGetGroupFaculty() {
        int groupId = 1;
        ReflectionTestUtils.setField(groupDAO, "jdbcTemplate", mockedJdbcTemplate);
        when(mockedJdbcTemplate.queryForObject(anyString(), any(FacultyMapper.class), anyInt())).thenThrow(QueryTimeoutException.class);
        assertThrows(DAOException.class, () -> groupDAO.getGroupFaculty(groupId));
    }
    
    @Test
    void shouldGenerateLogsWhenCreateGroup() {
        Group testGroup = new Group();
        testGroup.setName("Test Group");
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to insert a new object: " + testGroup + ".",
                "The object " + testGroup + " was inserted."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }
        
        groupDAO.create(testGroup);
        
        List<ILoggingEvent> actualLogs = testAppender.getEvents();
        
        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }
    
    @Test
    void shouldGenerateLogsWhenThrowDataAccessExceptionWhileCreate() {
        Group testGroup = new Group();
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to insert a new object: " + testGroup + ".",
                "Can't insert the object: " + testGroup + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }
        try {
            groupDAO.create(testGroup);
        } catch (DAOException exception) {
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
    void shouldGenerateLogsWhenFindAllIsEmpty() {
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.WARN));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to find all objects.",
                "There are not any objects in the result when findAll."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }
        
        groupDAO.findAll();
        List<ILoggingEvent> actualLogs = testAppender.getEvents();
        
        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i ++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
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
                "Try to find all objects.",
                "The result is: " + expectedGroups + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }
        
        groupDAO.findAll();
        
        List<ILoggingEvent> actualLogs = testAppender.getEvents();
        
        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }
    
    @Test
    void shouldGenerateLogsWhenThrowDataAccessExceptionWhileFindAll() {
        ReflectionTestUtils.setField(groupDAO, "jdbcTemplate", mockedJdbcTemplate);
        when(mockedJdbcTemplate.query(anyString(), any(GroupMapper.class))).thenThrow(QueryTimeoutException.class);
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to find all objects.",
                "Can't find all objects."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            groupDAO.findAll();
            verify(mockedJdbcTemplate).query(anyString(), any(GroupMapper.class));
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
        Group expectedGroup = expectedGroups.stream().filter(group -> group.getId() == testId).findFirst().get();
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to find an object by id: " + testId + ".",
                "The result object with id " + testId + " is " + expectedGroup + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }
        
        groupDAO.findById(testId);
        
        List<ILoggingEvent> actualLogs = testAppender.getEvents();
        
        assertEquals(expectedLogs.size(), actualLogs.size());
        for(int i = 0; i < actualLogs.size(); i++) {
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
                "Try to find an object by id: " + testId + ".",
                "There is no result when find an object by id " + testId + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }
        
        try {
            groupDAO.findById(testId);
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
        
        ReflectionTestUtils.setField(groupDAO, "jdbcTemplate", mockedJdbcTemplate);
        when(mockedJdbcTemplate.queryForObject(anyString(), any(GroupMapper.class), any())).thenThrow(QueryTimeoutException.class);
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to find an object by id: " + testId + ".",
                "Can't find an object by id " + testId + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }
        
        try {
            groupDAO.findById(testId);
            verify(mockedJdbcTemplate).queryForObject(anyString(), any(GroupMapper.class), any());
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
        int testId = 1;
        Group testGroup = new Group();
        testGroup.setName("Test Group");
        
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to update an object " + testGroup + " with id " + testId + ".",
                "The object " + testGroup + " with id " + testId + " was updated."));
        
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }
        
        groupDAO.update(testId, testGroup);
        
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
        Group testGroup = new Group();
        testGroup.setName("TestGroup");
        
        JdbcTemplate mockedJdbcTemplate = Mockito.mock(JdbcTemplate.class);
        ReflectionTestUtils.setField(groupDAO, "jdbcTemplate", mockedJdbcTemplate);
        doThrow(QueryTimeoutException.class).when(mockedJdbcTemplate).update(anyString(), anyString(), anyInt());
        
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to update an object " + testGroup + " with id " + testId + ".",
                "Can't update an object " + testGroup + " with id " + testId +  "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }
        
        try {
            groupDAO.update(testId, testGroup);
            verify(mockedJdbcTemplate).update(anyString(), anyString(), anyInt());
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
                "Try to delete an object by id " + testId + ".",
                "The object was deleted by id " + testId + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }
        
        groupDAO.deleteById(testId);
        
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

        ReflectionTestUtils.setField(groupDAO, "jdbcTemplate", mockedJdbcTemplate);
        doThrow(QueryTimeoutException.class).when(mockedJdbcTemplate).update(anyString(), anyInt());
        
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to delete an object by id " + testId + ".",
                "Can't delete an object by id " + testId +  "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }
        
        try {
            groupDAO.deleteById(testId);
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
    
    @Test
    void shouldGenerateLogsWhenSetGroupFaculty() {
        ScriptUtils.executeSqlScript(connection, testData);
        int facultyId = 1;
        int groupId = 2;
        
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to set a faculty with id " + facultyId + " for a group with id " + groupId + ".",
                "The faculty with id " + facultyId + " was setted for the group with id " + groupId + "."));
        
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }
        
        groupDAO.setGroupFaculty(facultyId, groupId);
        
        List<ILoggingEvent> actualLogs = testAppender.getEvents();
        
        assertEquals(expectedLogs.size(), actualLogs.size());
        for(int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }
    
    @Test
    void shouldGenerateLogsWhenThrowDataAccessExceptionWhileSetGroupFaculty() {
        int facultyId = 1;
        int groupId = 2;

        ReflectionTestUtils.setField(groupDAO, "jdbcTemplate", mockedJdbcTemplate);
        doThrow(QueryTimeoutException.class).when(mockedJdbcTemplate).update(anyString(), anyInt(), anyInt());
        
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to set a faculty with id " + facultyId + " for a group with id " + groupId + ".",
                "Can't set a faculty with id " + facultyId + " for a group with id " + groupId + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }
        
        try {
            groupDAO.setGroupFaculty(facultyId, groupId);
            verify(mockedJdbcTemplate).update(anyString(), anyInt(), anyInt());
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
    
    @Test
    void shouldGenerateLogsWhenGetGroupFaculty() {
        ScriptUtils.executeSqlScript(connection, testData);
        int groupId = 2;
        Faculty expectedFaculty = new Faculty();
        expectedFaculty.setId(2);
        expectedFaculty.setName("TestFaculty2");
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to get a faculty for a group with id " + groupId + ".",
                "The result faculty for the group with id " + groupId + " is " + expectedFaculty + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }
        
        groupDAO.getGroupFaculty(groupId);
        
        List<ILoggingEvent> actualLogs = testAppender.getEvents();
        
        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenThrowEmptyResultDataAccesEcxeptionWhileGetGroupFaculty() {
        int groupId = 2;
         
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to get a faculty for a group with id " + groupId + ".",
                "There is no a faculty for a group with id " + groupId + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }
        
        try {
            groupDAO.getGroupFaculty(groupId);
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
    
    @Test
    void shouldGenerateLogsWhenThrowDataAccesEcxeptionWhileGetGroupFaculty() {
        int groupId = 2;
        
        ReflectionTestUtils.setField(groupDAO, "jdbcTemplate", mockedJdbcTemplate);
        when(mockedJdbcTemplate.queryForObject(anyString(), any(FacultyMapper.class), any())).thenThrow(QueryTimeoutException.class);
        
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to get a faculty for a group with id " + groupId + ".",
                "Can't get a faculty for a group with id " + groupId + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }
        
        try {
            groupDAO.getGroupFaculty(groupId);
            verify(mockedJdbcTemplate).queryForObject(anyString(), any(FacultyMapper.class), any());
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
