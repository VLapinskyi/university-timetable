package ua.com.foxminded.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

import java.time.LocalTime;
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
import org.springframework.transaction.annotation.Transactional;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import ua.com.foxminded.domain.LessonTime;
import ua.com.foxminded.repositories.aspects.GeneralRepositoryAspect;
import ua.com.foxminded.repositories.exceptions.RepositoryException;

@DataJpaTest(showSql = true)
@Import({AopAutoConfiguration.class, GeneralRepositoryAspect.class})
@TestPropertySource("/application-test.properties")
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
class LessonTimeRepositoryTest {
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
    private LessonTimeRepository lessonTimeRepository;
    
    private List<LessonTime> expectedLessonTimes;
    

    @BeforeEach
    void setUp() throws Exception {
        Logger logger = (Logger) ReflectionTestUtils.getField(generalRepositoryAspect, "logger");
        testAppender = new ListAppender<>();
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        testAppender.setContext(loggerContext);
        testAppender.start();
        logger.addAppender(testAppender);
        
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
        ReflectionTestUtils.setField(lessonTimeRepository, "entityManager", testEntityManager.getEntityManager());
    }

    @AfterEach
    void tearDown() throws Exception {
        testAppender.stop();
    }

    @Test
    @Sql(testData)
    void shouldCreateLessonTime() {
        LessonTime testLessonTime = new LessonTime();
        testLessonTime.setStartTime(LocalTime.of(9, 0));
        testLessonTime.setEndTime(LocalTime.of(10, 0));
        
        int maxLessonTimeId = testEntityManager.getEntityManager().createQuery("from LessonTime", LessonTime.class)
                .getResultStream().max((lessonTime1, lessonTime2) -> Integer.compare(lessonTime1.getId(), lessonTime2.getId())).get().getId();
        
        int nextLessonTimeId = maxLessonTimeId + 1; 

        LessonTime expectedLessonTime = new LessonTime();
        expectedLessonTime.setId(nextLessonTimeId);
        expectedLessonTime.setStartTime(LocalTime.of(9, 0));
        expectedLessonTime.setEndTime(LocalTime.of(10, 0));

        lessonTimeRepository.create(testLessonTime);
        LessonTime actualLessonTime = testEntityManager.find(LessonTime.class, nextLessonTimeId);
        assertEquals(expectedLessonTime, actualLessonTime);
    }

    @Test
    @Sql(testData)
    void shouldFindAllLessonTimes() {
        List<LessonTime> actualLessonTimes = lessonTimeRepository.findAll();
        assertTrue(expectedLessonTimes.containsAll(actualLessonTimes)
                && actualLessonTimes.containsAll(expectedLessonTimes));
    }

    @Test
    @Sql(testData)
    void shouldFindLessonTimeById() {
        int checkedId = 2;
        LessonTime expectedLessonTime = new LessonTime();
        expectedLessonTime.setId(checkedId);
        expectedLessonTime.setStartTime(LocalTime.of(10, 45));
        expectedLessonTime.setEndTime(LocalTime.of(12, 15));
        assertEquals(expectedLessonTime, lessonTimeRepository.findById(checkedId));
    }

    @Test
    @Sql(testData)
    void shouldUpdateLessonTime() {
        int testId = 2;
        LessonTime testLessonTime = lessonTimeRepository.findById(testId);
        testLessonTime.setStartTime(LocalTime.of(11,0));

        lessonTimeRepository.update(testLessonTime);
        assertEquals(testLessonTime, lessonTimeRepository.findById(testId));
    }

    @Test
    @Sql(testData)
    void shouldDeleteLessonTime() {
        int deletedId = 2;
        LessonTime deletedLessonTime = testEntityManager.find(LessonTime.class, deletedId);
        
        lessonTimeRepository.delete(deletedLessonTime);
        
        LessonTime afterDeletingLessonTime = testEntityManager.find(LessonTime.class, deletedId);
        assertThat(afterDeletingLessonTime).isNull();
    }

    @Test
    void shouldThrowRepositoryExceptionWhenPersistenceExceptionWhileCreate() {
        LessonTime lessonTime = new LessonTime();
        lessonTime.setStartTime(LocalTime.of(9, 0));
        lessonTime.setEndTime(LocalTime.of(10, 0));
        
        doThrow(PersistenceException.class).when(lessonTimeRepository).create(lessonTime);
        assertThrows(RepositoryException.class, () -> lessonTimeRepository.create(lessonTime));
    }

    @Test
    void shouldThrowRepositoryExceptionWhenPersistenceExceptionWhileFindAll() {
        ReflectionTestUtils.setField(lessonTimeRepository, "entityManager", mockedEntityManager);
        doThrow(PersistenceException.class).when(mockedEntityManager).createQuery("from LessonTime", LessonTime.class);
        assertThrows(RepositoryException.class, () -> lessonTimeRepository.findAll());
    }

    @Test
    void shouldThrowRepositoryExceptionWhenResultIsNullPointerExceptionWhileFindById() {
        int testId = 1;
        assertThrows(RepositoryException.class, () -> lessonTimeRepository.findById(testId));
    }

    @Test
    @Sql(testData)
    void shouldThrowRepositoryExceptionWhenPersistenceExceptionWhileFindById() {
        int testId = 1;
        ReflectionTestUtils.setField(lessonTimeRepository, "entityManager", mockedEntityManager);
        when(mockedEntityManager.find(LessonTime.class,testId)).thenThrow(PersistenceException.class);
        assertThrows(RepositoryException.class, () -> lessonTimeRepository.findById(testId));
    }

    @Test
    void shouldThrowRepositoryExceptionWhenPersistenceExceptionWhileUpdate() {
        LessonTime testLessonTime = new LessonTime();
        testLessonTime.setId(4);
        testLessonTime.setStartTime(LocalTime.of(14, 0));
        testLessonTime.setEndTime(LocalTime.of(15, 0));
        
        ReflectionTestUtils.setField(lessonTimeRepository, "entityManager", mockedEntityManager);
        when(mockedEntityManager.merge(testLessonTime)).thenThrow(PersistenceException.class);
        assertThrows(RepositoryException.class, () -> lessonTimeRepository.update(testLessonTime));
    }

    @Test
    @Sql(testData)
    void shouldThrowRepositoryExceptionWhenPersistenceExceptionWhileDelete() {
        LessonTime lessonTime = new LessonTime();
        lessonTime.setId(4);
        lessonTime.setStartTime(LocalTime.of(10, 0));
        lessonTime.setEndTime(LocalTime.of(11, 0));
        
        ReflectionTestUtils.setField(lessonTimeRepository, "entityManager", mockedEntityManager);
        when(mockedEntityManager.merge(lessonTime)).thenThrow(PersistenceException.class);
        assertThrows(RepositoryException.class, () -> lessonTimeRepository.delete(lessonTime));
    }

    @Test
    @Sql(testData)
    void shouldGenerateLogsWhenCreateLessonTime() {
        LessonTime testLessonTime = new LessonTime();
        testLessonTime.setStartTime(LocalTime.of(15, 0));
        testLessonTime.setEndTime(LocalTime.of(16, 0));
        
        int maxLessonTimeId = testEntityManager.getEntityManager().createQuery("from LessonTime", LessonTime.class)
                .getResultStream().max((lessonTime1, lessonTime2) -> Integer.compare(lessonTime1.getId(), lessonTime2.getId())).get().getId();
        
        int nextLessonTimeId = maxLessonTimeId + 1;
        
        LessonTime loggingResultLessonTime = new LessonTime();
        loggingResultLessonTime.setId(nextLessonTimeId);
        loggingResultLessonTime.setStartTime(LocalTime.of(15, 0));
        loggingResultLessonTime.setEndTime(LocalTime.of(16, 0));

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(
                Arrays.asList("Try to insert a new object: " + testLessonTime + ".",
                        "The object " + loggingResultLessonTime + " was inserted."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonTimeRepository.create(testLessonTime);

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenThrowPersistenceExceptionWhileCreateLessonTime() {
        LessonTime testLessonTime = new LessonTime();
        testLessonTime.setId(4);
        testLessonTime.setStartTime(LocalTime.of(9, 0));
        testLessonTime.setEndTime(LocalTime.of(10, 0));

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

        lessonTimeRepository.findAll();

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    @Sql(testData)
    void shouldGenerateLogsWhenFindAllHasResult() {
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(
                Arrays.asList("Try to find all objects.", "The result is: " + expectedLessonTimes + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonTimeRepository.findAll();

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenThrowPersistenceExceptionWhileFindAll() {
        ReflectionTestUtils.setField(lessonTimeRepository, "entityManager", mockedEntityManager);
        doThrow(PersistenceException.class).when(mockedEntityManager).createQuery("from LessonTime", LessonTime.class);

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

    @Test
    @Sql(testData)
    void shouldGenerateLogsWhenFindById() {
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

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
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
        int testId = 2;

        ReflectionTestUtils.setField(lessonTimeRepository, "entityManager", mockedEntityManager);
        when(mockedEntityManager.find(LessonTime.class, testId)).thenThrow(PersistenceException.class);

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

    @Test
    @Sql(testData)
    void shouldGenerateLogsWhenUpdateLessonTime() {
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

        List<ILoggingEvent> actualLogs = testAppender.list;

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

        ReflectionTestUtils.setField(lessonTimeRepository, "entityManager", mockedEntityManager);
        when(mockedEntityManager.merge(testLessonTime)).thenThrow(PersistenceException.class);

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

    @Test
    @Sql(testData)
    void shouldGenerateLogsWhenDelete() {
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

        List<ILoggingEvent> actualLogs = testAppender.list;

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
        
        ReflectionTestUtils.setField(lessonTimeRepository, "entityManager", mockedEntityManager);
        when(mockedEntityManager.merge(testLessonTime)).thenThrow(PersistenceException.class);

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
