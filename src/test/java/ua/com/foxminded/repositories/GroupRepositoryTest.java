package ua.com.foxminded.repositories;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.PersistenceException;

import org.hibernate.SessionFactory;
import org.hibernate.internal.SessionImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptException;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ua.com.foxminded.domain.Faculty;
import ua.com.foxminded.domain.Group;
import ua.com.foxminded.repositories.exceptions.RepositoryException;
import ua.com.foxminded.settings.SpringTestLogConfiguration;
import ua.com.foxminded.settings.TestAppender;

@ContextConfiguration(classes = { SpringTestLogConfiguration.class })
@ExtendWith(SpringExtension.class)
class GroupRepositoryTest {
    private final ClassPathResource testData = new ClassPathResource("/Test data.sql");
    private final ClassPathResource testTablesCreator = new ClassPathResource("/Creating tables.sql");
    private final ClassPathResource testDatabaseCleaner = new ClassPathResource("/Clearing database.sql");

    private TestAppender testAppender = new TestAppender();
    
    @Autowired
    private GroupRepository groupRepository;
    
    @Autowired
    private SessionFactory sessionFactory;
    
    private List<Group> expectedGroups;
    private Connection connection;
    
    @Mock
    private SessionFactory mockedSessionFactory;

    @BeforeEach
    @Transactional
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        connection = ((SessionImpl)sessionFactory.getCurrentSession()).connection();
        ScriptUtils.executeSqlScript(connection, testTablesCreator);
        
        expectedGroups = new ArrayList<>(Arrays.asList(new Group(), new Group(), new Group()));
        List<String> groupNames = new ArrayList<>(Arrays.asList("TestGroup1", "TestGroup2", "TestGroup3"));
        List<Integer> groupIndexes = new ArrayList<>(Arrays.asList(1, 2, 3));
        
        Faculty faculty1 = new Faculty();
        faculty1.setId(1);
        faculty1.setName("TestFaculty1");
        Faculty faculty2 = new Faculty();
        faculty2.setId(2);
        faculty2.setName("TestFaculty2");
        
        List<Faculty> expectedFaculties = new ArrayList<>(Arrays.asList(faculty1, faculty2, faculty1));
        
        for (int i = 0; i < expectedGroups.size(); i++) {
            expectedGroups.get(i).setId(groupIndexes.get(i));
            expectedGroups.get(i).setName(groupNames.get(i));
            expectedGroups.get(i).setFaculty(expectedFaculties.get(i));
        }
    }

    @AfterEach
    @Transactional
    void tearDown() throws Exception {
        testAppender.cleanEventList();
        ReflectionTestUtils.setField(groupRepository, "sessionFactory", sessionFactory);
        ScriptUtils.executeSqlScript(connection, testDatabaseCleaner);
    }

    @Test
    @Transactional
    void shouldCreateGroup() {
        Group testGroup = new Group();
        testGroup.setName("TestGroup");
        Group expectedGroup = new Group();
        expectedGroup.setId(1);
        expectedGroup.setName("TestGroup");
        groupRepository.create(testGroup);
        Group actualGroup = groupRepository.findAll().stream().findFirst().get();
        assertEquals(expectedGroup, actualGroup);
    }

    @Test
    @Transactional
    void shouldFindAllGroups() throws ScriptException, SQLException {
        ScriptUtils.executeSqlScript(connection, testData);
        List<Group> actualGroups = groupRepository.findAll();
        assertTrue(expectedGroups.containsAll(actualGroups) && actualGroups.containsAll(expectedGroups));
    }

    @Test
    @Transactional
    void shouldFindGroupById() throws ScriptException, SQLException {
        ScriptUtils.executeSqlScript(connection, testData);
        int testId = 2;
        Group expectedGroup = expectedGroups.stream().filter(group -> group.getId() == testId).findFirst().get();
        assertEquals(expectedGroup, groupRepository.findById(testId));
    }

    @Test
    @Transactional
    void shouldUpdateGroup() throws ScriptException, SQLException {
        ScriptUtils.executeSqlScript(connection, testData);
        int testGroupId = 2;
        Group testGroup = groupRepository.findById(testGroupId);
        testGroup.setName("TestGroupUpdated");
        groupRepository.update(testGroup);
        assertEquals(testGroup, groupRepository.findById(testGroupId));
    }

    @Test
    @Transactional
    void shouldDeleteGroupById() {
        ScriptUtils.executeSqlScript(connection, testData);
        int deletedGroupId = 2;
        Group deletedGroup = new Group();
        for (int i = 0; i < expectedGroups.size(); i++) {
            if (expectedGroups.get(i).getId() == deletedGroupId) {
                Group groupFromList = expectedGroups.get(i);
                deletedGroup.setId(groupFromList.getId());
                deletedGroup.setName(groupFromList.getName());
                deletedGroup.setFaculty(groupFromList.getFaculty());
                deletedGroup.setStudents(groupFromList.getStudents());
                expectedGroups.remove(i);
                i--;
            }
        }
        groupRepository.delete(deletedGroup);
        List<Group> actualGroups = groupRepository.findAll();
        assertTrue(expectedGroups.containsAll(actualGroups) && actualGroups.containsAll(expectedGroups));
    }

    @Test
    @Transactional
    void shouldThrowRepositoryExceptionWhenPersistenceExceptionWhileCreate() {
        Group group = new Group();
        group.setId(1);
        group.setName("Test");
        assertThrows(RepositoryException.class, () -> groupRepository.create(group));
    }

    @Test
    @Transactional
    void shouldThrowRepositoryExceptionWhenPersistenceExceptionWhileFindAll() {
        ReflectionTestUtils.setField(groupRepository, "sessionFactory", mockedSessionFactory);
        when(mockedSessionFactory.getCurrentSession()).thenThrow(PersistenceException.class);
        assertThrows(RepositoryException.class, () -> groupRepository.findAll());
    }

    @Test
    @Transactional
    void shouldThrowRepositoryExceptionWhenResultIsNullPointerExceptionWhileFindById() {
        int testId = 1;
        assertThrows(RepositoryException.class, () -> groupRepository.findById(testId));
    }

    @Test
    @Transactional
    void shouldThrowRepositoryExceptionWhenPersistenceExceptionWhileFindById() {
        int testId = 1;
        ReflectionTestUtils.setField(groupRepository, "sessionFactory", mockedSessionFactory);
        when(mockedSessionFactory.getCurrentSession()).thenThrow(PersistenceException.class);
        assertThrows(RepositoryException.class, () -> groupRepository.findById(testId));
    }

    @Test
    @Transactional
    void shouldThrowRepositoryExceptionWhenPersistenceExceptionWhileUpdate() {
        Group testGroup = new Group();
        testGroup.setId(1);
        testGroup.setName("Test");
        ReflectionTestUtils.setField(groupRepository, "sessionFactory", mockedSessionFactory);
        when(mockedSessionFactory.getCurrentSession()).thenThrow(PersistenceException.class);
        assertThrows(RepositoryException.class, () -> groupRepository.update(testGroup));
    }

    @Test
    @Transactional
    void shouldThrowRepositoryExceptionWhenPersistenceExceptionWhileDelete() {
        Group group = new Group();
        group.setId(2);
        group.setName("Test");
        ReflectionTestUtils.setField(groupRepository, "sessionFactory", mockedSessionFactory);
        when(mockedSessionFactory.getCurrentSession()).thenThrow(PersistenceException.class);
        assertThrows(RepositoryException.class, () -> groupRepository.delete(group));
    }

    @Test
    @Transactional
    void shouldGenerateLogsWhenCreateGroup() {
        Group testGroup = new Group();
        testGroup.setName("Test Group");
        
        Group loggingResultGroup = new Group();
        loggingResultGroup.setId(1);
        loggingResultGroup.setName("Test Group");
        
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to insert a new object: " + loggingResultGroup + ".",
                "The object " + loggingResultGroup + " was inserted."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        groupRepository.create(testGroup);

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    @Transactional
    void shouldGenerateLogsWhenThrowPersistenceExceptionWhileCreate() {
        Group testGroup = new Group();
        testGroup.setId(1);
        testGroup.setName("Test");
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to insert a new object: " + testGroup + ".",
                "Can't insert the object: " + testGroup + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }
        try {
            groupRepository.create(testGroup);
        } catch (RepositoryException exception) {
            // do nothing
        }

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    @Transactional
    void shouldGenerateLogsWhenFindAllIsEmpty() {
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.WARN));
        List<String> expectedMessages = new ArrayList<>(
                Arrays.asList("Try to find all objects.", "There are not any objects in the result when findAll."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        groupRepository.findAll();
        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    @Transactional
    void shouldGenerateLogsWhenFindAllHasResult() {
        ScriptUtils.executeSqlScript(connection, testData);
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(
                Arrays.asList("Try to find all objects.", "The result is: " + expectedGroups + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        groupRepository.findAll();

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    @Transactional
    void shouldGenerateLogsWhenThrowPersistenceExceptionWhileFindAll() {
        ReflectionTestUtils.setField(groupRepository, "sessionFactory", mockedSessionFactory);
        when(mockedSessionFactory.getCurrentSession()).thenThrow(PersistenceException.class);
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(
                Arrays.asList("Try to find all objects.", "Can't find all objects."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            groupRepository.findAll();
            verify(mockedSessionFactory.getCurrentSession()).createQuery(anyString(), Group.class);
        } catch (RepositoryException repositoryException) {
            // do nothing
        }
        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getMessage(), actualLogs.get(i).getMessage());
        }
    }

    @Test
    @Transactional
    void shouldGenerateLogsWhenFindById() {
        ScriptUtils.executeSqlScript(connection, testData);
        int testId = 2;
        Group expectedGroup = expectedGroups.stream().filter(group -> group.getId() == testId).findFirst().get();
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to find an object by id: " + testId + ".",
                "The result object with id " + testId + " is " + expectedGroup + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        groupRepository.findById(testId);

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    @Transactional
    void shouldGenerateLogsWhenResultIsNullPointerExceptionWhileFindById() {
        int testId = 1;
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to find an object by id: " + testId + ".",
                "There is no result when find an object by id " + testId + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            groupRepository.findById(testId);
        } catch (RepositoryException repositoryEcxeption) {
            // do nothing
        }

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    @Transactional
    void shouldGenerateLogsWhenPersistenceExceptionWhileFindById() {
        int testId = 1;

        ReflectionTestUtils.setField(groupRepository, "sessionFactory", mockedSessionFactory);
        when(mockedSessionFactory.getCurrentSession()).thenThrow(PersistenceException.class);
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to find an object by id: " + testId + ".",
                "Can't find an object by id " + testId + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            groupRepository.findById(testId);
            verify(mockedSessionFactory.getCurrentSession()).createQuery(anyString(), Group.class);
        } catch (RepositoryException repositoryEcxeption) {
            // do nothing
        }

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    @Transactional
    void shouldGenerateLogsWhenUpdate() {
        ScriptUtils.executeSqlScript(connection, testData);
        Group testGroup = new Group();
        testGroup.setName("Test Group");
        testGroup.setId(1);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(
                Arrays.asList("Try to update an object " + testGroup + ".",
                        "The object " + testGroup + " was updated."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        groupRepository.update(testGroup);

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    @Transactional
    void shouldGenerateLogsWhenThrowPersistenceExceptionWhileUpdate() {
        Group testGroup = new Group();
        testGroup.setName("TestGroup");
        testGroup.setId(1);

        ReflectionTestUtils.setField(groupRepository, "sessionFactory", mockedSessionFactory);
        when(mockedSessionFactory.getCurrentSession()).thenThrow(PersistenceException.class);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(
                Arrays.asList("Try to update an object " + testGroup + ".",
                        "Can't update an object " + testGroup + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            groupRepository.update(testGroup);
            verify(mockedSessionFactory.getCurrentSession()).update(testGroup);
        } catch (RepositoryException repositoryEcxeption) {
            // do nothing
        }

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    @Transactional
    void shouldGenerateLogsWhenDelete() {
        ScriptUtils.executeSqlScript(connection, testData);
        int testId = 3;
        Group deletedGroup = expectedGroups.stream().filter(group -> group.getId() == testId).findFirst().get();

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to delete an object " + deletedGroup + ".",
                "The object " + deletedGroup + " was deleted."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        groupRepository.delete(deletedGroup);

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    @Transactional
    void shouldGenerateLogsWhenThrowPersistenceExceptionWhileDeleteById() {
        Group testGroup = new Group();
        testGroup.setId(1);
        testGroup.setName("Test");

        ReflectionTestUtils.setField(groupRepository, "sessionFactory", mockedSessionFactory);
        when(mockedSessionFactory.getCurrentSession()).thenThrow(PersistenceException.class);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to delete an object " + testGroup + ".",
                "Can't delete an object " + testGroup+ "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            groupRepository.delete(testGroup);
            verify(mockedSessionFactory.getCurrentSession()).delete(testGroup);
        } catch (RepositoryException repositoryException) {
            // do nothing
        }

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }
}
