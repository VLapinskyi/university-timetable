package ua.com.foxminded.repositories;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.sql.Connection;
import java.time.LocalTime;
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
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ua.com.foxminded.domain.LessonTime;
import ua.com.foxminded.repositories.exceptions.RepositoryException;
import ua.com.foxminded.settings.SpringTestLogConfiguration;
import ua.com.foxminded.settings.TestAppender;

@ContextConfiguration(classes = { SpringTestLogConfiguration.class })
@ExtendWith(SpringExtension.class)
class LessonTimeRepositoryTest {
    private final ClassPathResource testData = new ClassPathResource("/Test data.sql");
    private final ClassPathResource testTablesCreator = new ClassPathResource("/Creating tables.sql");
    private final ClassPathResource testDatabaseCleaner = new ClassPathResource("/Clearing database.sql");

    private TestAppender testAppender = new TestAppender();
    
    @Autowired
    private LessonTimeRepository lessonTimeRepository;
    
    @Autowired
    private SessionFactory sessionFactory;
    
    private List<LessonTime> expectedLessonTimes;
    private Connection connection;
    
    @Mock
    private SessionFactory mockedSessionFactory;

    @BeforeEach
    @Transactional
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        connection = ((SessionImpl)sessionFactory.getCurrentSession()).connection();
        ScriptUtils.executeSqlScript(connection, testTablesCreator);
        expectedLessonTimes = new ArrayList<>(Arrays.asList(new LessonTime(), new LessonTime(), new LessonTime()));
        List<Integer> indexes = new ArrayList<>(Arrays.asList(1, 2, 3));
        List<LocalTime> startTimes = new ArrayList<>(
                Arrays.asList(LocalTime.of(9, 0), LocalTime.of(10, 45), LocalTime.of(12, 30)));
        List<LocalTime> endTimes = new ArrayList<>(
                Arrays.asList(LocalTime.of(10, 30), LocalTime.of(12, 15), LocalTime.of(14, 0)));
        for (int i = 0; i < expectedLessonTimes.size(); i++) {
            expectedLessonTimes.get(i).setId(indexes.get(i));
            expectedLessonTimes.get(i).setStartTime(startTimes.get(i));
            expectedLessonTimes.get(i).setEndTime(endTimes.get(i));
        }
    }

    @AfterEach
    @Transactional
    void tearDown() throws Exception {
        testAppender.cleanEventList();
        ReflectionTestUtils.setField(lessonTimeRepository, "sessionFactory", sessionFactory);
        ScriptUtils.executeSqlScript(connection, testDatabaseCleaner);
    }

    @Test
    @Transactional
    void shouldCreateLessonTime() {
        LessonTime testLessonTime = new LessonTime();
        testLessonTime.setStartTime(LocalTime.of(9, 0));
        testLessonTime.setEndTime(LocalTime.of(10, 0));

        LessonTime expectedLessonTime = new LessonTime();
        expectedLessonTime.setId(1);
        expectedLessonTime.setStartTime(LocalTime.of(9, 0));
        expectedLessonTime.setEndTime(LocalTime.of(10, 0));

        lessonTimeRepository.create(testLessonTime);
        assertEquals(expectedLessonTime, lessonTimeRepository.findAll().stream().findFirst().get());
    }

    @Test
    @Transactional
    void shouldFindAllLessonTimes() {
        ScriptUtils.executeSqlScript(connection, testData);
        List<LessonTime> actualLessonTimes = lessonTimeRepository.findAll();
        assertTrue(expectedLessonTimes.containsAll(actualLessonTimes)
                && actualLessonTimes.containsAll(expectedLessonTimes));
    }

    @Test
    @Transactional
    void shouldFindLessonTimeById() {
        ScriptUtils.executeSqlScript(connection, testData);
        int checkedId = 2;
        LessonTime expectedLessonTime = new LessonTime();
        expectedLessonTime.setId(checkedId);
        expectedLessonTime.setStartTime(LocalTime.of(10, 45));
        expectedLessonTime.setEndTime(LocalTime.of(12, 15));
        assertEquals(expectedLessonTime, lessonTimeRepository.findById(checkedId));
    }

    @Test
    @Transactional
    void shouldUpdateLessonTime() {
        ScriptUtils.executeSqlScript(connection, testData);
        int testId = 2;
        LessonTime testLessonTime = lessonTimeRepository.findById(testId);
        testLessonTime.setStartTime(LocalTime.of(11,0));

        lessonTimeRepository.update(testLessonTime);
        assertEquals(testLessonTime, lessonTimeRepository.findById(testId));
    }

    @Test
    @Transactional
    void shouldDeleteLessonTime() {
        ScriptUtils.executeSqlScript(connection, testData);
        int deletedId = 2;
        LessonTime deletedLessonTime = new LessonTime();
        for (int i = 0; i < expectedLessonTimes.size(); i++) {
            if (expectedLessonTimes.get(i).getId() == deletedId) {
                LessonTime lessonTimeFromList = expectedLessonTimes.get(i);
                deletedLessonTime.setId(lessonTimeFromList.getId());
                deletedLessonTime.setStartTime(lessonTimeFromList.getStartTime());
                deletedLessonTime.setEndTime(lessonTimeFromList.getEndTime());
                
                expectedLessonTimes.remove(i);
                i--;
            }
        }
        lessonTimeRepository.delete(deletedLessonTime);
        List<LessonTime> actualLessonTimes = lessonTimeRepository.findAll();
        assertTrue(expectedLessonTimes.containsAll(actualLessonTimes)
                && actualLessonTimes.containsAll(expectedLessonTimes));
    }

    @Test
    @Transactional
    void shouldThrowRepositoryExceptionWhenPersistenceExceptionWhileCreate() {
        LessonTime lessonTime = new LessonTime();
        lessonTime.setId(8);
        assertThrows(RepositoryException.class, () -> lessonTimeRepository.create(lessonTime));
    }

    @Test
    @Transactional
    void shouldThrowRepositoryExceptionWhenPersistenceExceptionWhileFindAll() {
        ReflectionTestUtils.setField(lessonTimeRepository, "sessionFactory", mockedSessionFactory);
        when(mockedSessionFactory.getCurrentSession()).thenThrow(PersistenceException.class);
        assertThrows(RepositoryException.class, () -> lessonTimeRepository.findAll());
    }

    @Test
    @Transactional
    void shouldThrowRepositoryExceptionWhenResultIsNullPointerExceptionWhileFindById() {
        int testId = 1;
        assertThrows(RepositoryException.class, () -> lessonTimeRepository.findById(testId));
    }

    @Test
    @Transactional
    void shouldThrowRepositoryExceptionWhenPersistenceExceptionWhileFindById() {
        int testId = 1;
        ReflectionTestUtils.setField(lessonTimeRepository, "sessionFactory", mockedSessionFactory);
        when(mockedSessionFactory.getCurrentSession()).thenThrow(PersistenceException.class);
        assertThrows(RepositoryException.class, () -> lessonTimeRepository.findById(testId));
    }

    @Test
    @Transactional
    void shouldThrowRepositoryExceptionWhenPersistenceExceptionWhileUpdate() {
        LessonTime testLessonTime = new LessonTime();
        testLessonTime.setId(4);
        testLessonTime.setStartTime(LocalTime.of(14, 0));
        testLessonTime.setEndTime(LocalTime.of(15, 0));
        
        ReflectionTestUtils.setField(lessonTimeRepository, "sessionFactory", mockedSessionFactory);
        when(mockedSessionFactory.getCurrentSession()).thenThrow(PersistenceException.class);
        assertThrows(RepositoryException.class, () -> lessonTimeRepository.update(testLessonTime));
    }

    @Test
    @Transactional
    void shouldThrowRepositoryExceptionWhenPersistenceExceptionWhileDelete() {
        LessonTime lessonTime = new LessonTime();
        lessonTime.setId(4);
        lessonTime.setStartTime(LocalTime.of(10, 0));
        lessonTime.setEndTime(LocalTime.of(11, 0));
        
        ReflectionTestUtils.setField(lessonTimeRepository, "sessionFactory", mockedSessionFactory);
        when(mockedSessionFactory.getCurrentSession()).thenThrow(PersistenceException.class);
        assertThrows(RepositoryException.class, () -> lessonTimeRepository.delete(lessonTime));
    }

    @Test
    @Transactional
    void shouldGenerateLogsWhenCreateLessonTime() {
        LessonTime testLessonTime = new LessonTime();
        testLessonTime.setStartTime(LocalTime.of(15, 0));
        testLessonTime.setEndTime(LocalTime.of(16, 0));
        
        LessonTime loggingResultLessonTime = new LessonTime();
        loggingResultLessonTime.setId(1);
        loggingResultLessonTime.setStartTime(LocalTime.of(15, 0));
        loggingResultLessonTime.setEndTime(LocalTime.of(16, 0));

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(
                Arrays.asList("Try to insert a new object: " + loggingResultLessonTime + ".",
                        "The object " + loggingResultLessonTime + " was inserted."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonTimeRepository.create(testLessonTime);

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    @Transactional
    void shouldGenerateLogsWhenThrowPersistenceExceptionWhileCreateLessonTime() {
        LessonTime testLessonTime = new LessonTime();
        testLessonTime.setId(4);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(
                Arrays.asList("Try to insert a new object: " + testLessonTime + ".",
                        "Can't insert the object: " + testLessonTime + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonTimeRepository.create(testLessonTime);
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

        lessonTimeRepository.findAll();

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
                Arrays.asList("Try to find all objects.", "The result is: " + expectedLessonTimes + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonTimeRepository.findAll();

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
        ReflectionTestUtils.setField(lessonTimeRepository, "sessionFactory", mockedSessionFactory);
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
            lessonTimeRepository.findAll();
            verify(mockedSessionFactory.getCurrentSession()).createQuery(anyString(), LessonTime.class);
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

    @Test
    @Transactional
    void shouldGenerateLogsWhenFindById() {
        ScriptUtils.executeSqlScript(connection, testData);
        int testId = 2;
        LessonTime expectedLessonTime = expectedLessonTimes.stream().filter(lessonTime -> lessonTime.getId() == testId)
                .findFirst().get();
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to find an object by id: " + testId + ".",
                "The result object with id " + testId + " is " + expectedLessonTime + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonTimeRepository.findById(testId);

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    @Transactional
    void shouldGenerateLogsWhenResulyIsNullPointerExceptionWhileFindById() {
        int testId = 2;
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to find an object by id: " + testId + ".",
                "There is no result when find an object by id " + testId + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonTimeRepository.findById(testId);
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

    @Test
    @Transactional
    void shouldGenerateLogsWhenThrowPersistenceExceptionWhileFindById() {
        int testId = 2;

        ReflectionTestUtils.setField(lessonTimeRepository, "sessionFactory", mockedSessionFactory);
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
            lessonTimeRepository.findById(testId);
            verify(mockedSessionFactory.getCurrentSession()).get(LessonTime.class, testId);
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

    @Test
    @Transactional
    void shouldGenerateLogsWhenUpdateLessonTime() {
        ScriptUtils.executeSqlScript(connection, testData);
        LessonTime testLessonTime = new LessonTime();
        testLessonTime.setId(1);
        testLessonTime.setStartTime(LocalTime.of(16, 0));
        testLessonTime.setEndTime(LocalTime.of(17, 30));

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(
                Arrays.asList("Try to update an object " + testLessonTime + ".",
                        "The object " + testLessonTime + " was updated."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonTimeRepository.update(testLessonTime);

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    @Transactional
    void shouldGenerateLogsWhenThrowPersistenceExceptionWhileUpdateLessonTime() {
        LessonTime testLessonTime = new LessonTime();
        testLessonTime.setId(2);
        testLessonTime.setStartTime(LocalTime.of(16, 0));
        testLessonTime.setEndTime(LocalTime.of(17, 30));

        ReflectionTestUtils.setField(lessonTimeRepository, "sessionFactory", mockedSessionFactory);
        when(mockedSessionFactory.getCurrentSession()).thenThrow(PersistenceException.class);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(
                Arrays.asList("Try to update an object " + testLessonTime + ".",
                        "Can't update an object " + testLessonTime + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonTimeRepository.update(testLessonTime);
            verify(mockedSessionFactory.getCurrentSession()).update(testLessonTime);
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

    @Test
    @Transactional
    void shouldGenerateLogsWhenDelete() {
        ScriptUtils.executeSqlScript(connection, testData);
        int testId = 3;
        LessonTime testLessonTime = expectedLessonTimes.stream().filter(lessonTime -> lessonTime.getId() == testId).findAny().get();
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to delete an object " + testLessonTime + ".",
                "The object " + testLessonTime +  " was deleted."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonTimeRepository.delete(testLessonTime);

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
        int testId = 3;
        LessonTime testLessonTime = expectedLessonTimes.stream().filter(lessonTime -> lessonTime.getId() == testId).findAny().get();
        
        ReflectionTestUtils.setField(lessonTimeRepository, "sessionFactory", mockedSessionFactory);
        when(mockedSessionFactory.getCurrentSession()).thenThrow(PersistenceException.class);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to delete an object " + testLessonTime +  ".",
                "Can't delete an object " + testLessonTime + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonTimeRepository.delete(testLessonTime);
            verify(mockedSessionFactory.getCurrentSession()).delete(testLessonTime);
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
