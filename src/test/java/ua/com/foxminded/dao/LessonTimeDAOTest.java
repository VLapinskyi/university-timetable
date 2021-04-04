package ua.com.foxminded.dao;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;

import java.sql.Connection;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ua.com.foxminded.dao.exceptions.DAOException;
import ua.com.foxminded.domain.LessonTime;
import ua.com.foxminded.mapper.LessonTimeMapper;
import ua.com.foxminded.settings.SpringTestConfiguration;
import ua.com.foxminded.settings.TestAppender;

@ContextConfiguration(classes = {SpringTestConfiguration.class})
@ExtendWith(SpringExtension.class)
class LessonTimeDAOTest {
    private final ClassPathResource testData = new ClassPathResource("/Test data.sql");
    private final ClassPathResource testTablesCreator = new ClassPathResource("/Creating tables.sql");
    private final ClassPathResource testDatabaseCleaner = new ClassPathResource("/Clearing database.sql");

    private TestAppender testAppender = new TestAppender();
    @Autowired
    private LessonTimeDAO lessonTimeDAO;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private List<LessonTime> expectedLessonTimes;
    private Connection connection;

    @BeforeEach
    void setUp() throws Exception {
        connection = jdbcTemplate.getDataSource().getConnection();
        ScriptUtils.executeSqlScript(connection, testTablesCreator);
        expectedLessonTimes = new ArrayList<>(Arrays.asList(
                new LessonTime(), new LessonTime(), new LessonTime()));
        List<Integer> indexes = new ArrayList<>(Arrays.asList(1, 2, 3));
        List<LocalTime> startTimes = new ArrayList<>(Arrays.asList(
                LocalTime.of(9, 0), LocalTime.of(10, 45), LocalTime.of(12, 30)));
        List<LocalTime> endTimes = new ArrayList<>(Arrays.asList(
                LocalTime.of(10, 30), LocalTime.of(12, 15), LocalTime.of(14, 0)));
        for (int i = 0; i < expectedLessonTimes.size(); i++) {
            expectedLessonTimes.get(i).setId(indexes.get(i));
            expectedLessonTimes.get(i).setStartTime(startTimes.get(i));
            expectedLessonTimes.get(i).setEndTime(endTimes.get(i));
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        testAppender.cleanEventList();
        ReflectionTestUtils.setField(lessonTimeDAO, "jdbcTemplate", jdbcTemplate);
        ScriptUtils.executeSqlScript(connection, testDatabaseCleaner);
    }

    @Test
    void shouldCreateLessonTime() {
        LessonTime testLessonTime = new LessonTime();
        testLessonTime.setStartTime(LocalTime.of(9, 0));
        testLessonTime.setEndTime(LocalTime.of(10, 0));

        LessonTime expectedLessonTime = new LessonTime();
        expectedLessonTime.setId(1);
        expectedLessonTime.setStartTime(LocalTime.of(9, 0));
        expectedLessonTime.setEndTime(LocalTime.of(10, 0));

        lessonTimeDAO.create(testLessonTime);
        assertEquals(expectedLessonTime, lessonTimeDAO.findAll().stream().findFirst().get());
    }

    @Test
    void shouldFindAllLessonTimes() {
        ScriptUtils.executeSqlScript(connection, testData);
        List<LessonTime> actualLessonTimes = lessonTimeDAO.findAll();
        assertTrue(expectedLessonTimes.containsAll(actualLessonTimes) && actualLessonTimes.containsAll(expectedLessonTimes));
    }

    @Test
    void shouldFindLessonTimeById() {
        ScriptUtils.executeSqlScript(connection, testData);
        int checkedId = 2;
        LessonTime expectedLessonTime = new LessonTime();
        expectedLessonTime.setId(checkedId);
        expectedLessonTime.setStartTime(LocalTime.of(10, 45));
        expectedLessonTime.setEndTime(LocalTime.of(12, 15));
        assertEquals(expectedLessonTime, lessonTimeDAO.findById(checkedId));
    }

    @Test
    void shouldUpdateLessonTime() {
        ScriptUtils.executeSqlScript(connection, testData);
        int testId = 2;
        LessonTime testLessonTime = new LessonTime();
        testLessonTime.setStartTime(LocalTime.of(15, 0));
        testLessonTime.setEndTime(LocalTime.of(16, 0));

        LessonTime expectedLessonTime = new LessonTime();
        expectedLessonTime.setId(testId);
        expectedLessonTime.setStartTime(LocalTime.of(15, 0));
        expectedLessonTime.setEndTime(LocalTime.of(16, 0));

        lessonTimeDAO.update(testId, testLessonTime);
        assertEquals(expectedLessonTime, lessonTimeDAO.findById(testId));
    }

    @Test
    void shouldDeleteLessonTimeById() {
        ScriptUtils.executeSqlScript(connection, testData);
        int deletedId = 2;
        for (int i = 0; i < expectedLessonTimes.size(); i++) {
            if (expectedLessonTimes.get(i).getId() == deletedId) {
                expectedLessonTimes.remove(i);
                i--;
            }
        }
        lessonTimeDAO.deleteById(deletedId);
        List<LessonTime> actualLessonTimes = lessonTimeDAO.findAll();
        assertTrue(expectedLessonTimes.containsAll(actualLessonTimes) && actualLessonTimes.containsAll(expectedLessonTimes));
    }

    @Test
    void shouldGenerateLogsWhenCreateLessonTime() {
        LessonTime testLessonTime = new LessonTime();
        testLessonTime.setStartTime(LocalTime.of(15, 0));
        testLessonTime.setEndTime(LocalTime.of(16, 0));

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to insert new lessonTime: " + testLessonTime + ".",
                "The lessonTime " + testLessonTime + " was inserted."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonTimeDAO.create(testLessonTime);

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenThrowDataAccessExceptionWhileCreateLessonTime() {
        LessonTime testLessonTime = new LessonTime();

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to insert new lessonTime: " + testLessonTime + ".",
                "Can't create lessonTime: " + testLessonTime + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonTimeDAO.create(testLessonTime);
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
    void shouldGenerateLogsWhenFindAllIsEmpty() {
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.WARN));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to find all lessonTimes.",
                "There are not any lessonTimes in the result."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonTimeDAO.findAll();

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
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
                "Try to find all lessonTimes.",
                "The result is: " + expectedLessonTimes + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonTimeDAO.findAll();

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
        ReflectionTestUtils.setField(lessonTimeDAO, "jdbcTemplate", mockedJdbcTemplate);
        when(mockedJdbcTemplate.query(anyString(), any(LessonTimeMapper.class))).thenThrow(QueryTimeoutException.class);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to find all lessonTimes.",
                "Can't find all lessonTimes."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonTimeDAO.findAll();
            verify(mockedJdbcTemplate).query(anyString(), any(LessonTimeMapper.class));
        } catch (DAOException daoException) {
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
    void shouldGenerateLogsWhenFindById() {
        ScriptUtils.executeSqlScript(connection, testData);
        int testId = 2;
        LessonTime expectedLessonTime = expectedLessonTimes.stream().filter(lessonTime -> lessonTime.getId() == testId).findFirst().get();
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to find lessonTime by id " + testId + ".",
                "The result lessonTime with id " + testId + " is " + expectedLessonTime + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonTimeDAO.findById(testId);

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenThrowEmptyResultDataAccessExceptionWhileFindById() {
        int testId = 2;
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to find lessonTime by id " + testId + ".",
                "There is no result when find by id " + testId + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonTimeDAO.findById(testId);
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
    void shouldGenerateLogsWhenThrowDataAccessExceptionWhileFindById() {
        int testId = 2;

        JdbcTemplate mockedJdbcTemplate = Mockito.mock(JdbcTemplate.class);
        ReflectionTestUtils.setField(lessonTimeDAO, "jdbcTemplate", mockedJdbcTemplate);
        when(mockedJdbcTemplate.queryForObject(anyString(), any(LessonTimeMapper.class), anyInt())).thenThrow(QueryTimeoutException.class);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to find lessonTime by id " + testId + ".",
                "Can't find lessonTime by id " + testId + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonTimeDAO.findById(testId);
            verify(mockedJdbcTemplate).queryForObject(anyString(), any(LessonTimeMapper.class), anyInt());
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
    void shouldGenerateLogsWhenUpdateLessonTime() {
        ScriptUtils.executeSqlScript(connection, testData);
        int testId = 1;
        LessonTime testLessonTime = new LessonTime();
        testLessonTime.setStartTime(LocalTime.of(16, 0));
        testLessonTime.setEndTime(LocalTime.of(17, 30));

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to update lessonTime " + testLessonTime + " with id " + testId + ".",
                "The lessonTime " + testLessonTime + " with id " + testId + " was changed."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonTimeDAO.update(testId, testLessonTime);

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenThrowDataAccessExceptionWhileUpdateLessonTime() {
        int testId = 1;
        LessonTime testLessonTime = new LessonTime();
        testLessonTime.setStartTime(LocalTime.of(16, 0));
        testLessonTime.setEndTime(LocalTime.of(17, 30));

        JdbcTemplate mockedJdbcTemplate = Mockito.mock(JdbcTemplate.class);
        ReflectionTestUtils.setField(lessonTimeDAO, "jdbcTemplate", mockedJdbcTemplate);
        doThrow(QueryTimeoutException.class).when(mockedJdbcTemplate).update(anyString(), any(LocalTime.class), any(LocalTime.class), anyInt());

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to update lessonTime " + testLessonTime + " with id " + testId + ".",
                "Can't update lessonTime " + testLessonTime + " with id " + testId + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonTimeDAO.update(testId, testLessonTime);
            verify(mockedJdbcTemplate).update(anyString(), any(LocalTime.class), any(LocalTime.class), anyInt());
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
    void shouldGenerateLogsWhenDeleteById() {
        ScriptUtils.executeSqlScript(connection, testData);
        int testId = 3;
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to delete lessonTime by id " + testId + ".",
                "The lessonTime with id " + testId + " was deleted."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonTimeDAO.deleteById(testId);

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
        ReflectionTestUtils.setField(lessonTimeDAO, "jdbcTemplate", mockedJdbcTemplate);
        doThrow(QueryTimeoutException.class).when(mockedJdbcTemplate).update(anyString(), anyInt());

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to delete lessonTime by id " + testId + ".",
                "Can't delete lessonTime by id " + testId + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonTimeDAO.deleteById(testId);
            verify(mockedJdbcTemplate).update(anyString(), anyInt());
        } catch (DAOException daoException) {
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
