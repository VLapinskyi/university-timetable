package ua.com.foxminded.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
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
import ua.com.foxminded.domain.LessonTime;
import ua.com.foxminded.repositories.aspects.GeneralRepositoryAspect;
import ua.com.foxminded.repositories.exceptions.RepositoryException;
import ua.com.foxminded.repositories.interfaces.LessonTimeRepository;

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

        lessonTimeRepository.save(testLessonTime);
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
        Optional<LessonTime> expectedResult = Optional.of(expectedLessonTime);
        assertEquals(expectedResult, lessonTimeRepository.findById(checkedId));
    }

    @Test
    @Sql(testData)
    void shouldUpdateLessonTime() {
        int testId = 2;
        LessonTime testLessonTime = testEntityManager.find(LessonTime.class, testId);
        testLessonTime.setStartTime(LocalTime.of(11,0));

        lessonTimeRepository.save(testLessonTime);
        assertEquals(testLessonTime, testEntityManager.find(LessonTime.class, testId));
    }

    @Test
    @Sql(testData)
    void shouldDeleteById() {
        int deletedId = 2;
        
        lessonTimeRepository.deleteById(deletedId);
        
        LessonTime afterDeletingLessonTime = testEntityManager.find(LessonTime.class, deletedId);
        assertThat(afterDeletingLessonTime).isNull();
    }

    @Test
    void shouldThrowRepositoryExceptionWhenDataAccessExceptionWhileSave() {
        LessonTime lessonTime = null;
        
        assertThrows(RepositoryException.class, () -> lessonTimeRepository.save(lessonTime));
    }

    @Test
    void shouldThrowRepositoryExceptionWhenResultIsNullPointerExceptionWhileFindById() {
        int testId = 1;
        assertThrows(RepositoryException.class, () -> lessonTimeRepository.findById(testId));
    }

    @Test
    @Sql(testData)
    void shouldThrowRepositoryExceptionWhenDataAccessExceptionWhileFindById() {
        Integer testId = null;
        assertThrows(RepositoryException.class, () -> lessonTimeRepository.findById(testId));
    }
    
    @Test
    @Sql(testData)
    void shouldThrowRepositoryExceptionWhenPersistenceExceptionWhileDeleteById() {
        Integer testId =  null;
        
        assertThrows(RepositoryException.class, () -> lessonTimeRepository.deleteById(testId));
    }

    @Test
    @Sql(testData)
    void shouldGenerateLogsWhenSaveLessonTime() {
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
                Arrays.asList("Try to save/update an object: " + testLessonTime + ".",
                        "The object " + loggingResultLessonTime + " was saved/updated."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonTimeRepository.save(testLessonTime);

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenThrowDataAccessExceptionWhileSaveLessonTime() {
        LessonTime testLessonTime = null;

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(
                Arrays.asList("Try to save/update an object: " + testLessonTime + ".",
                        "Can't save/update the object: " + testLessonTime + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonTimeRepository.save(testLessonTime);
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
    @Sql(testData)
    void shouldGenerateLogsWhenFindById() {
        int testId = 2;
        Optional<LessonTime> expectedLessonTime = expectedLessonTimes.stream().filter(lessonTime -> lessonTime.getId() == testId)
                .findFirst();
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
    void shouldGenerateLogsWhenResultIsNullPointerExceptionWhileFindById() {
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
    void shouldGenerateLogsWhenThrowDataAccesExceptionWhileFindById() {
        Integer testId = null;

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
    void shouldGenerateLogsWhenDeleteById() {
        int testId = 3;
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to delete an object by id " + testId + ".",
                "The object with id " + testId +  " was deleted."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonTimeRepository.deleteById(testId);

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenThrowDataAccessExceptionWhileDeleteById() {
        Integer testId = null;
        
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to delete an object by id " + testId +  ".",
                "Can't delete an object by id " + testId + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonTimeRepository.deleteById(testId);
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
