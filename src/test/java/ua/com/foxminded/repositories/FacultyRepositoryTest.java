package ua.com.foxminded.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.mockito.ArgumentMatchers.any;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.util.ReflectionTestUtils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import ua.com.foxminded.domain.Faculty;
import ua.com.foxminded.repositories.aspects.GeneralRepositoryAspect;
import ua.com.foxminded.repositories.exceptions.RepositoryException;

@DataJpaTest(showSql = true)
@Import({AopAutoConfiguration.class, GeneralRepositoryAspect.class})
@TestPropertySource("/application-test.properties")
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
class FacultyRepositoryTest {
    private final String testData = "/Test data.sql";
    
    private ListAppender<ILoggingEvent> testAppender;
    
    @Autowired
    private GeneralRepositoryAspect generalRepositoryAspect;
    
    @Autowired
    private TestEntityManager testEntityManager;
    
    @MockBean
    private EntityManager mockedEntityManager;

    @Autowired
    @SpyBean
    private FacultyRepository facultyRepository;
    
    private List<Faculty> expectedFaculties;

    @BeforeEach
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
        
        ReflectionTestUtils.setField(facultyRepository, "entityManager", testEntityManager.getEntityManager());
    }

    @AfterEach
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
    @Sql(testData)
    void shouldFindAllFaculties() {
        List<Faculty> actualFaculties = facultyRepository.findAll();
        assertTrue(expectedFaculties.containsAll(actualFaculties) && actualFaculties.containsAll(expectedFaculties));
    }

    @Test
    @Sql(testData)
    void shouldFindFacultyById(){
        int checkedId = 2;
        Faculty expectedFaculty = new Faculty();
        expectedFaculty.setId(checkedId);
        expectedFaculty.setName("TestFaculty2");
        assertEquals(expectedFaculty, facultyRepository.findById(checkedId));
    }

    @Test
    @Sql(testData)
    void shouldUpdateFaculty() {
        int testId = 2;
        Faculty testFaculty = facultyRepository.findById(testId);
        testFaculty.setName("TestFacultyUpdated");
        facultyRepository.update(testFaculty);
        assertEquals(testFaculty, facultyRepository.findById(testId));
    }

    @Test
    @Sql(testData)
    void shouldDeleteFaculty(){
        int deletedFacultyId = 2;
        Faculty deletedFaculty = testEntityManager.find(Faculty.class, deletedFacultyId);
        facultyRepository.delete(deletedFaculty);
        Faculty afterDeletingFaculty = testEntityManager.find(Faculty.class, deletedFacultyId);
        
        assertThat(afterDeletingFaculty).isNull();
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
        when(facultyRepository.findAll()).thenThrow(RepositoryException.class);
        assertThrows(RepositoryException.class, () -> facultyRepository.findAll());
    }

    @Test
    void shouldThrowRepositoryExceptionWhenResultIsNullPointerExceptionWhileFindById() {
        int testId = 1;
        assertThrows(RepositoryException.class, () -> facultyRepository.findById(testId));
    }

    @Test
    @Sql(testData)
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
                "Try to insert a new object: " + testFaculty + ".", "The object " + loggingResultFaculty + " was inserted."));
        
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
        int wrongId = 123;
        Faculty testFaculty = new Faculty();
        testFaculty.setName("Test");
        testFaculty.setId(wrongId);
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
        ReflectionTestUtils.setField(facultyRepository, "entityManager", mockedEntityManager);
        doThrow(PersistenceException.class).when(mockedEntityManager).createQuery("FROM Faculty", Faculty.class);
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
    @Sql(testData)
    void shouldGenerateLogsWhenThrowPersistenceExceptionWhileFindById() {
        ReflectionTestUtils.setField(facultyRepository, "entityManager", mockedEntityManager);        
        int testId = 1;
        when(mockedEntityManager.find(Faculty.class, testId)).thenThrow(PersistenceException.class);
        
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to find an object by id: " + testId + ".",
                "Can't find an object by id " + testId + "."));

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
        ReflectionTestUtils.setField(facultyRepository, "entityManager", mockedEntityManager);
        
        Faculty testFaculty = new Faculty();
        testFaculty.setId(3);
        testFaculty.setName("TestFaculty");

        when(mockedEntityManager.merge(testFaculty)).thenThrow(PersistenceException.class);

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
        Faculty deletedFaculty = testEntityManager.find(Faculty.class, testId);

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
        
        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenThrowPersistenceExceptionWhileDelete() {
        ReflectionTestUtils.setField(facultyRepository, "entityManager", mockedEntityManager);
        
        Faculty testFaculty = new Faculty();
        testFaculty.setId(5);
        testFaculty.setName("test");
        
        when(mockedEntityManager.merge(testFaculty)).thenThrow(PersistenceException.class);
        
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
