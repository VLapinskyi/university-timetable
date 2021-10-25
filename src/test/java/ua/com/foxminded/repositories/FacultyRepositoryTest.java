package ua.com.foxminded.repositories;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.any;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.JpaBaseConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.JpaContext;
import org.springframework.jdbc.datasource.init.ScriptException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.jdbc.SqlConfig.ErrorMode;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import ua.com.foxminded.domain.Faculty;
import ua.com.foxminded.repositories.aspects.GeneralRepositoryAspect;
import ua.com.foxminded.repositories.exceptions.RepositoryException;
import ua.com.foxminded.settings.SpringTestConfiguration;
import ua.com.foxminded.settings.TestAppender;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@AutoConfigureTestDatabase
@Import({AopAutoConfiguration.class, GeneralRepositoryAspect.class})
@TestPropertySource("/application-test.properties")
class FacultyRepositoryTest {
    private final String testData = "/Test data.sql";
    private final String testTablesCreator = "/Creating tables.sql";
    private final String testDatabaseCleaner = "/Clearing database.sql";
    
    private ListAppender<ILoggingEvent> testAppender;
    
    @Autowired
    private GeneralRepositoryAspect generalRepositoryAspect;

    @Autowired
    @SpyBean
    private FacultyRepository facultyRepository;
    
    private List<Faculty> expectedFaculties;

    @BeforeEach
    @Sql(scripts = testTablesCreator, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
    void setUp() {
        Logger logger = (Logger) ReflectionTestUtils.getField(generalRepositoryAspect, "logger");
        testAppender = new ListAppender<>();
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        testAppender.setContext(loggerContext);
        testAppender.start();
        logger.addAppender(testAppender);
        expectedFaculties = new ArrayList<>(Arrays.asList(new Faculty(), new Faculty(), new Faculty()));
        List<String> facultyNames = new ArrayList<>(Arrays.asList("TestFaculty1", "TestFaculty2", "TestFaculty3"));
        List<Integer> facultyIndexes = new ArrayList<>(Arrays.asList(1, 2, 3));
        for (int i = 0; i < expectedFaculties.size(); i++) {
            expectedFaculties.get(i).setId(facultyIndexes.get(i));
            expectedFaculties.get(i).setName(facultyNames.get(i));
        }
    }

    @AfterEach
    @Sql(scripts = testDatabaseCleaner, executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
    void tearDown() {
        testAppender.stop();
    }

    @Test
    void shouldCreateFaculty() {
        Faculty expectedFaculty = new Faculty();
        expectedFaculty.setId(1);
        expectedFaculty.setName("TestFaculty");
        Faculty testFaculty = new Faculty();
        testFaculty.setName("TestFaculty");
        facultyRepository.create(testFaculty);
        Faculty actualFaculty = facultyRepository.findAll().stream().findFirst().get();
        assertEquals(expectedFaculty, actualFaculty);
    }

    @Test
    @Sql(scripts = testData, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void shouldFindAllFaculties() throws ScriptException, SQLException {
        List<Faculty> actualFaculties = facultyRepository.findAll();
        assertTrue(expectedFaculties.containsAll(actualFaculties) && actualFaculties.containsAll(expectedFaculties));
    }

    @Test
    @Sql(scripts = testData, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void shouldFindFacultyById() throws ScriptException, SQLException {
        int checkedId = 2;
        Faculty expectedFaculty = new Faculty();
        expectedFaculty.setId(checkedId);
        expectedFaculty.setName("TestFaculty2");
        assertEquals(expectedFaculty, facultyRepository.findById(checkedId));
    }

    @Test
    @Sql(scripts = testData, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void shouldUpdateFaculty() throws ScriptException, SQLException {
        int testId = 2;
        Faculty testFaculty = facultyRepository.findById(testId);
        testFaculty.setName("TestFacultyUpdated");
        facultyRepository.update(testFaculty);
        assertEquals(testFaculty, facultyRepository.findById(testId));
    }

    @Test
    @Sql(scripts = testData, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void shouldDeleteFaculty(){
        int deletedFacultyId = 2;
        Faculty deletedFaculty = new Faculty();
        for (int i = 0; i < expectedFaculties.size(); i++) {
            if (expectedFaculties.get(i).getId() == deletedFacultyId) {
                Faculty facultyFromList = expectedFaculties.get(i);
                deletedFaculty.setId(facultyFromList.getId());
                deletedFaculty.setName(facultyFromList.getName());
                deletedFaculty.setGroups(facultyFromList.getGroups());
                expectedFaculties.remove(i);
                i--;
            }
        }
        facultyRepository.delete(deletedFaculty);
        List<Faculty> actualFaculties = facultyRepository.findAll();
        assertTrue(expectedFaculties.containsAll(actualFaculties) && actualFaculties.containsAll(expectedFaculties));
    }

    @Test
    void shouldThrowRepositoryExceptionWhenPersistenceExceptionWhileCreate() {
        Faculty testFaculty = new Faculty();
        testFaculty.setId(1);
        testFaculty.setName("Test");
        doThrow(PersistenceException.class).when(facultyRepository).create(testFaculty);
        assertThrows(RepositoryException.class, () -> facultyRepository.create(testFaculty));
    }

    @Test
    void shouldThrowRepositoryExceptionWhenPersistenceExceptionWhileFindAll() {
        doThrow(PersistenceException.class).when(facultyRepository).create(any(Faculty.class));
        assertThrows(RepositoryException.class, () -> facultyRepository.findAll());
    }

    @Test
    void shouldThrowRepositoryExceptionWhenResultIsNullPointerExceptionWhileFindById() {
        int testId = 1;
        assertThrows(RepositoryException.class, () -> facultyRepository.findById(testId));
    }

    @Test
    void shouldThrowRepositoryExceptionWhenPersistenceExceptionWhileFindById() {
        int testId = 1;
        when(facultyRepository.findById(testId)).thenThrow(PersistenceException.class);
        assertThrows(RepositoryException.class, () -> facultyRepository.findById(testId));
    }

    @Test
    void shouldThrowRepositoryExceptionWhenPersistenceExceptionWhileUpdate() {
        Faculty testFaculty = new Faculty();
        testFaculty.setId(1);
        testFaculty.setName("Test faculty");
        
        doThrow(PersistenceException.class).when(facultyRepository).update(testFaculty);
        assertThrows(RepositoryException.class, () -> facultyRepository.update(testFaculty));
    }

    @Test
    void shouldThrowRepositoryExceptionWhenPersistenceExceptionWhileDelete() {
        Faculty testFaculty = new Faculty();
        testFaculty.setId(2);
        testFaculty.setName("Test faculty");
        doThrow(PersistenceException.class).when(facultyRepository).delete(any(Faculty.class));
        assertThrows(RepositoryException.class, () -> facultyRepository.delete(testFaculty));
    }

    @Test
    void shouldGenerateLogsWhenCreateFaculty() {
        Faculty testFaculty = new Faculty();
        testFaculty.setName("Test Faculty");
        
        Faculty loggingResultFaculty = new Faculty();
        loggingResultFaculty.setId(1);
        loggingResultFaculty.setName("Test Faculty");
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to insert a new object: " + loggingResultFaculty + ".", "The object " + loggingResultFaculty + " was inserted."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        facultyRepository.create(testFaculty);
        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenThrowPersistenceExceptionWhileCreate() {
        Faculty testFaculty = new Faculty();
        testFaculty.setId(1);
        testFaculty.setName("Test");
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to insert a new object: " + testFaculty + ".", "Can't insert the object: " + testFaculty + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }
        try {
            facultyRepository.create(testFaculty);
        } catch (RepositoryException exception) {
            // do nothing
        }

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenFindAllIsEmpty() {
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.WARN));
        List<String> expectedMessages = new ArrayList<>(
                Arrays.asList("Try to find all objects.", "There are not any objects in the result when findAll."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        facultyRepository.findAll();

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getMessage(), actualLogs.get(i).getMessage());
        }
    }

    @Test
    @Sql(testData)
    void shouldGenerateLogsWhenFindAllHasResult() {
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(
                Arrays.asList("Try to find all objects.", "The result is: " + expectedFaculties + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        facultyRepository.findAll();

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenThrowPersistenceExceptionWhileFindAll() {
        doThrow(PersistenceException.class).when(facultyRepository).findAll();
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(
                Arrays.asList("Try to find all objects.", "Can't find all objects."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            facultyRepository.findAll();
        } catch (RepositoryException repositoryException) {
            // do nothing
        }
        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getMessage(), actualLogs.get(i).getMessage());
        }
    }

    @Test
    @Sql(testData)
    void shouldGenerateLogsWhenFindById() {
        int testId = 2;
        Faculty expectedFaculty = expectedFaculties.stream().filter(faculty -> faculty.getId() == testId).findFirst()
                .get();
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to find an object by id: " + testId + ".",
                "The result object with id " + testId + " is " + expectedFaculty + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        facultyRepository.findById(testId);

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
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
            facultyRepository.findById(testId);
        } catch (RepositoryException repositoryEcxeption) {
            // do nothing
        }

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenThrowPersistenceExceptionWhileFindById() {
        int testId = 1;
        doThrow(NullPointerException.class).when(facultyRepository).findById(testId);
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to find an object by id: " + testId + ".",
                "There is no result when find an object by id " + testId + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            facultyRepository.findById(testId);
        } catch (RepositoryException repositoryEcxeption) {
            // do nothing
        }

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    @Sql(testData)
    void shouldGenerateLogsWhenUpdate() {
        Faculty testFaculty = new Faculty();
        testFaculty.setId(2);
        testFaculty.setName("TestFaculty");

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(
                Arrays.asList("Try to update an object " + testFaculty + ".",
                        "The object " + testFaculty + " was updated."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        facultyRepository.update(testFaculty);

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenThrowPersistenceExceptionWhileUpdate() {
        Faculty testFaculty = new Faculty();
        testFaculty.setId(3);
        testFaculty.setName("TestFaculty");

        doThrow(PersistenceException.class).when(facultyRepository).update(testFaculty);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(
                Arrays.asList("Try to update an object " + testFaculty + ".",
                        "Can't update an object " + testFaculty + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            facultyRepository.update(testFaculty);
        } catch (RepositoryException repositoryEcxeption) {
            // do nothing
        }

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    @Sql(scripts = testData)
    void shouldGenerateLogsWhenDelete() {
        int testId = 3;
        Faculty deletedFaculty = expectedFaculties.stream().filter(faculty -> faculty.getId() == testId).findFirst().get();

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to delete an object " + deletedFaculty + ".",
                "The object " + deletedFaculty + " was deleted."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        facultyRepository.delete(deletedFaculty);

        List<ILoggingEvent> actualLogs = testAppender.list;
        System.out.println(actualLogs);
        
        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenThrowPersistenceExceptionWhileDelete() {
        Faculty testFaculty = new Faculty();
        testFaculty.setId(5);
        testFaculty.setName("test");

        doThrow(PersistenceException.class).when(facultyRepository).delete(testFaculty);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to delete an object " + testFaculty + ".",
                "Can't delete an object " + testFaculty + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            facultyRepository.delete(testFaculty);
        } catch (RepositoryException repositoryException) {
            // do nothing
        }

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }
}
