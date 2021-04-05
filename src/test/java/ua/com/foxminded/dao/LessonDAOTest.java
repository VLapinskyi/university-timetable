package ua.com.foxminded.dao;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;

import java.sql.Connection;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.EmptyResultDataAccessException;
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
import ua.com.foxminded.domain.Gender;
import ua.com.foxminded.domain.Group;
import ua.com.foxminded.domain.Lecturer;
import ua.com.foxminded.domain.Lesson;
import ua.com.foxminded.domain.LessonTime;
import ua.com.foxminded.mapper.GroupMapper;
import ua.com.foxminded.mapper.LecturerMapper;
import ua.com.foxminded.mapper.LessonMapper;
import ua.com.foxminded.mapper.LessonTimeMapper;
import ua.com.foxminded.settings.SpringTestConfiguration;
import ua.com.foxminded.settings.TestAppender;

@ContextConfiguration(classes = {SpringTestConfiguration.class})
@ExtendWith(SpringExtension.class)
class LessonDAOTest {
    private final ClassPathResource testData = new ClassPathResource("/Test data.sql");
    private final ClassPathResource testTablesCreator = new ClassPathResource("/Creating tables.sql");
    private final ClassPathResource testDatabaseCleaner = new ClassPathResource("/Clearing database.sql");

    private TestAppender testAppender = new TestAppender();
    @Autowired
    private LessonDAO lessonDAO;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private List<Lesson> expectedLessons;
    private Connection connection;
    @Mock
    private JdbcTemplate mockedJdbcTemplate;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        connection = jdbcTemplate.getDataSource().getConnection();
        ScriptUtils.executeSqlScript(connection, testTablesCreator);        
        expectedLessons = new ArrayList<>(Arrays.asList(
                new Lesson(), new Lesson(), new Lesson(), new Lesson()));
        List<Integer> lessonIndexes = new ArrayList<>(Arrays.asList(1, 2, 3, 4));
        List<String> lessonNames = new ArrayList<>(Arrays.asList(
                "Ukranian", "Music", "Physical Exercises", "Physical Exercises"));
        List<String> audiences = new ArrayList<>(Arrays.asList("101", "102", "103", "103"));
        List<DayOfWeek> weekDays = new ArrayList<>(Arrays.asList(
                DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.THURSDAY));
        for (int i = 0; i < expectedLessons.size(); i++) {
            expectedLessons.get(i).setId(lessonIndexes.get(i));
            expectedLessons.get(i).setName(lessonNames.get(i));
            expectedLessons.get(i).setAudience(audiences.get(i));
            expectedLessons.get(i).setDay(weekDays.get(i));
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        testAppender.cleanEventList();
        ReflectionTestUtils.setField(lessonDAO, "jdbcTemplate", jdbcTemplate);
        ScriptUtils.executeSqlScript(connection, testDatabaseCleaner);
    }

    @Test
    void shouldCreateLesson() {  
        Lesson testLesson = new Lesson();
        testLesson.setName("Ukranian");
        testLesson.setAudience("101");
        testLesson.setDay(DayOfWeek.TUESDAY);

        Lesson expectedLesson = new Lesson();
        expectedLesson.setId(1);
        expectedLesson.setName("Ukranian");
        expectedLesson.setAudience("101");
        expectedLesson.setDay(DayOfWeek.TUESDAY);

        lessonDAO.create(testLesson);
        assertEquals(expectedLesson, lessonDAO.findAll().stream().findFirst().get());
    }

    @Test
    void shouldFindAllLessons() {
        ScriptUtils.executeSqlScript(connection, testData);
        List<Lesson> actualLessons = lessonDAO.findAll();
        assertTrue(expectedLessons.containsAll(actualLessons) && actualLessons.containsAll(expectedLessons));
    }

    @Test
    void shouldFindLessonById() {
        ScriptUtils.executeSqlScript(connection, testData);
        int checkedId = 2;
        Lesson expectedLesson = new Lesson();
        expectedLesson.setId(checkedId);
        expectedLesson.setName("Music");
        expectedLesson.setAudience("102");
        expectedLesson.setDay(DayOfWeek.WEDNESDAY);

        assertEquals(expectedLesson, lessonDAO.findById(checkedId));
    }

    @Test
    void shouldUpdateLesson() {
        ScriptUtils.executeSqlScript(connection, testData);  
        int testId = 2;
        Lesson testLesson = new Lesson();
        testLesson.setName("History");
        testLesson.setAudience("105");
        testLesson.setDay(DayOfWeek.TUESDAY);

        Lesson expectedLesson = new Lesson();
        expectedLesson.setId(testId);
        expectedLesson.setName("History");
        expectedLesson.setAudience("105");
        expectedLesson.setDay(DayOfWeek.TUESDAY);

        lessonDAO.update(testId, testLesson);
        assertEquals(expectedLesson, lessonDAO.findById(testId));

    }

    @Test
    void shouldDeleteLessonById() {
        ScriptUtils.executeSqlScript(connection, testData);
        int deletedId = 2;
        for (int i = 0; i < expectedLessons.size(); i++) {
            if (expectedLessons.get(i).getId() == deletedId) {
                expectedLessons.remove(i);
                i--;
            }
        }
        lessonDAO.deleteById(deletedId);
        List<Lesson> actualLessons = lessonDAO.findAll();
        assertTrue(expectedLessons.containsAll(actualLessons) && actualLessons.containsAll(expectedLessons));
    }

    @Test
    void shouldSetLessonLecturer() {
        ScriptUtils.executeSqlScript(connection, testData);
        int lecturerId = 2;
        Lecturer lecturer = new Lecturer();
        lecturer.setId(lecturerId);
        lecturer.setFirstName("Ihor");
        lecturer.setLastName("Zakharchuk");
        lecturer.setGender(Gender.MALE);
        lecturer.setEmail("i.zakharchuk@gmail.com");

        int lessonId = 2;
        Lesson expectedLesson = expectedLessons.stream().filter(lesson -> lesson.getId() == lessonId).findFirst().get();
        expectedLesson.setLecturer(lecturer);

        lessonDAO.setLessonLecturer(lecturerId, lessonId);
        Lesson actualLesson = lessonDAO.findById(lessonId);
        actualLesson.setLecturer(lessonDAO.getLessonLecturer(lessonId));
        assertEquals(expectedLesson, actualLesson);
    }

    @Test
    void shouldGetLessonLecturer() {
        ScriptUtils.executeSqlScript(connection, testData);
        int lecturerId = 3;
        Lecturer expectedLecturer = new Lecturer();
        expectedLecturer.setId(lecturerId);
        expectedLecturer.setFirstName("Vasyl");
        expectedLecturer.setLastName("Dudchenko");
        expectedLecturer.setGender(Gender.MALE);

        int lessonId = 1;
        lessonDAO.setLessonLecturer(lecturerId, lessonId);
        Lecturer actualLecturer = lessonDAO.getLessonLecturer(lessonId);
        assertEquals(expectedLecturer, actualLecturer);
    }

    @Test
    void shouldSetLessonGroup() {
        ScriptUtils.executeSqlScript(connection, testData);
        int groupId = 2;
        Group group = new Group();
        group.setId(groupId);
        group.setName("TestGroup2");

        int lessonId = 2;
        Lesson expectedLesson = expectedLessons.stream().filter(lesson -> lesson.getId() == lessonId).findFirst().get();
        expectedLesson.setGroup(group);

        lessonDAO.setLessonGroup(groupId, lessonId);
        Lesson actualLesson = lessonDAO.findById(lessonId);
        actualLesson.setGroup(lessonDAO.getLessonGroup(lessonId));
        assertEquals(expectedLesson, actualLesson);
    }

    @Test
    void shouldGetLessonGroup() {
        ScriptUtils.executeSqlScript(connection, testData);
        int groupId = 3;
        Group expectedGroup = new Group();
        expectedGroup.setId(groupId);
        expectedGroup.setName("TestGroup3");

        int lessonId = 1;
        lessonDAO.setLessonGroup(groupId, lessonId);
        Group actualGroup = lessonDAO.getLessonGroup(lessonId);
        assertEquals(expectedGroup, actualGroup);
    }

    @Test
    void shouldSetLessonTime() {
        ScriptUtils.executeSqlScript(connection, testData);
        int lessonTimeId = 1;
        LessonTime lessonTime = new LessonTime();
        lessonTime.setId(lessonTimeId);
        lessonTime.setStartTime(LocalTime.of(9, 0));
        lessonTime.setEndTime(LocalTime.of(10, 30));

        int lessonId = 2;
        Lesson expectedLesson = expectedLessons.stream().filter(lesson -> lesson.getId() == lessonId).findFirst().get();
        expectedLesson.setLessonTime(lessonTime);

        lessonDAO.setLessonTime(lessonTimeId, lessonId);
        Lesson actualLesson = lessonDAO.findById(lessonId);
        actualLesson.setLessonTime(lessonDAO.getLessonTime(lessonId));
        assertEquals(expectedLesson, actualLesson);
    }

    @Test
    void shouldGetLessonTime() {
        ScriptUtils.executeSqlScript(connection, testData);
        int lessonTimeId = 2;
        LessonTime expectedLessonTime = new LessonTime();
        expectedLessonTime.setId(lessonTimeId);
        expectedLessonTime.setStartTime(LocalTime.of(10, 45));
        expectedLessonTime.setEndTime(LocalTime.of(12, 15));

        int lessonId = 1;
        lessonDAO.setLessonTime(lessonTimeId, lessonId);
        LessonTime actualLessonTime = lessonDAO.getLessonTime(lessonId);
        assertEquals(expectedLessonTime, actualLessonTime);	
    }

    @Test
    void shouldGetDayLessonsForGroup() {
        ScriptUtils.executeSqlScript(connection, testData);
        int groupId = 1;
        DayOfWeek testDay = DayOfWeek.MONDAY;
        Lesson expectedLesson = expectedLessons.get(0);
        List<Lesson> actualLessons = lessonDAO.getGroupDayLessons(groupId, testDay);
        assertTrue(actualLessons.contains(expectedLesson) && actualLessons.size() == 1);
    }

    @Test
    void shouldGetDayLessonsForLecturer() {
        ScriptUtils.executeSqlScript(connection, testData);
        int lecturerId = 3;
        DayOfWeek testDay = DayOfWeek.THURSDAY;
        expectedLessons = new ArrayList<> (Arrays.asList(
                this.expectedLessons.get(2), this.expectedLessons.get(3)));

        List<Lesson> actualLessons = lessonDAO.getLecturerDayLessons(lecturerId, testDay);

        assertTrue(expectedLessons.containsAll(actualLessons) && actualLessons.containsAll(expectedLessons));
    }

    @Test
    void shouldGenerateLogsWhenCreate() {
        Lesson testLesson = new Lesson();
        testLesson.setName("Math");
        testLesson.setAudience("103");
        testLesson.setDay(DayOfWeek.MONDAY);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to create lesson: " + testLesson + ".",
                "The lesson " + testLesson + " was inserted."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonDAO.create(testLesson);

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenThrowDataAccessExceptionWhileCreate() {
        Lesson testLesson = new Lesson();
        testLesson.setDay(DayOfWeek.FRIDAY);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to create lesson: " + testLesson + ".",
                "Can't create lesson: " + testLesson + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }
        try {
            lessonDAO.create(testLesson);
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
                "Try to find all lessons.",
                "There are not any lessons in the result."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonDAO.findAll();

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
                "Try to find all lessons.",
                "The result is: " + expectedLessons + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonDAO.findAll();

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenThrowDataAccessExceptionWhileFindAll() {
        ReflectionTestUtils.setField(lessonDAO, "jdbcTemplate", mockedJdbcTemplate);
        when(mockedJdbcTemplate.query(anyString(), any(LessonMapper.class))).thenThrow(QueryTimeoutException.class);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to find all lessons.",
                "Can't find all lessons."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }
        try {
            lessonDAO.findAll();
            verify(mockedJdbcTemplate).query(anyString(), any(LessonMapper.class));
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
    void shouldGenerateLogsWhenFindById() {
        ScriptUtils.executeSqlScript(connection, testData);
        int testId = 2;
        Lesson expectedLesson = expectedLessons.stream().filter(lesson -> lesson.getId() == testId).findFirst().get();
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to find lesson by id " + testId + ".",
                "The result lesson with id " + testId + " is " + expectedLesson + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonDAO.findById(testId);

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

        JdbcTemplate mockedJdbcTemplate = Mockito.mock(JdbcTemplate.class);
        ReflectionTestUtils.setField(lessonDAO, "jdbcTemplate", mockedJdbcTemplate);
        when(mockedJdbcTemplate.queryForObject(anyString(), any(LessonMapper.class), any())).thenThrow(EmptyResultDataAccessException.class);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to find lesson by id " + testId + ".",
                "There is no result when find by id " + testId + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonDAO.findById(testId);
            verify(mockedJdbcTemplate).queryForObject(anyString(), any(LessonMapper.class), any());
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

        ReflectionTestUtils.setField(lessonDAO, "jdbcTemplate", mockedJdbcTemplate);
        when(mockedJdbcTemplate.queryForObject(anyString(), any(LessonMapper.class), any())).thenThrow(QueryTimeoutException.class);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to find lesson by id " + testId + ".",
                "Can't find lesson by id " + testId + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonDAO.findById(testId);
            verify(mockedJdbcTemplate).queryForObject(anyString(), any(LessonMapper.class), any());
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
    void shouldGenerateLogsWhenUpdate () {
        ScriptUtils.executeSqlScript(connection, testData);
        int testId = 1;
        Lesson testLesson = new Lesson();
        testLesson.setName("Test Lesson");
        testLesson.setAudience("109");
        testLesson.setDay(DayOfWeek.SATURDAY);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to update lesson " + testLesson + " with id " + testId + ".",
                "The lesson " + testLesson + " with id " + testId + " was changed."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonDAO.update(testId, testLesson);

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenThrowDataAccessExceptionWhileUpdate () {
        int testId = 1;
        Lesson testLesson = new Lesson();
        testLesson.setDay(DayOfWeek.SATURDAY);

        ReflectionTestUtils.setField(lessonDAO, "jdbcTemplate", mockedJdbcTemplate);
        doThrow(QueryTimeoutException.class).when(mockedJdbcTemplate).update(anyString(), (Object) any());

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to update lesson " + testLesson + " with id " + testId + ".",
                "Can't update lesson " + testLesson + " with id " + testId + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonDAO.update(testId, testLesson);
            verify(mockedJdbcTemplate).update(anyString(), (Object) any());
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
    void shouldGenerateLogsWhenDeleteById() {
        ScriptUtils.executeSqlScript(connection, testData);
        int testId = 3;

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to delete lesson by id " + testId + ".",
                "The lesson with id " + testId + " was deleted."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonDAO.deleteById(testId);

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

        ReflectionTestUtils.setField(lessonDAO, "jdbcTemplate", mockedJdbcTemplate);
        doThrow(QueryTimeoutException.class).when(mockedJdbcTemplate).update(anyString(), anyInt());

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to delete lesson by id " + testId + ".",
                "Can't delete lesson by id " + testId + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonDAO.deleteById(testId);
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
    void shouldGenerateLogsWhenSetLessonLecturer() {
        ScriptUtils.executeSqlScript(connection, testData);
        int lecturerId = 1;
        int lessonId = 2;

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to set lecturer with id " + lecturerId + " for lesson with id " + lessonId + ".",
                "The lecturer with id " + lecturerId + " was setted for lesson with id " + lessonId + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonDAO.setLessonLecturer(lecturerId, lessonId);

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenThrowDataAccessExceptionWhileSetLessonLecturer() {
        int lecturerId = 1;
        int lessonId = 2;

        ReflectionTestUtils.setField(lessonDAO, "jdbcTemplate", mockedJdbcTemplate);
        doThrow(QueryTimeoutException.class).when(mockedJdbcTemplate).update(anyString(), anyInt(), anyInt());

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to set lecturer with id " + lecturerId + " for lesson with id " + lessonId + ".",
                "Can't set lecturer with id " + lecturerId + " to lesson with id " + lessonId + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonDAO.setLessonLecturer(lecturerId, lessonId);
        } catch(DAOException daoException) {
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
    void shouldGenerateLogsWhenGetLessonLecturer() {
        ScriptUtils.executeSqlScript(connection, testData);
        int lessonId = 2;
        Lecturer expectedLecturer = new Lecturer();
        expectedLecturer.setId(2);
        expectedLecturer.setFirstName("Ihor");
        expectedLecturer.setLastName("Zakharchuk");
        expectedLecturer.setGender(Gender.MALE);
        expectedLecturer.setEmail("i.zakharchuk@gmail.com");

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to get lecturer for lesson with id " + lessonId + ".",
                "The result lecturer for lesson with id " + lessonId + " is " + expectedLecturer + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonDAO.getLessonLecturer(lessonId);

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenThrowEmptyResultDataAccessExcpetionWhileGetLessonLecturer() {
        int lessonId = 2;

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to get lecturer for lesson with id " + lessonId + ".",
                "There is no a lecturer for lesson with id " + lessonId + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonDAO.getLessonLecturer(lessonId);
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
    void shouldGenerateLogsWhenThrowDataAccessExcpetionWhileGetLessonLecturer() {
        int lessonId = 2;

        ReflectionTestUtils.setField(lessonDAO, "jdbcTemplate", mockedJdbcTemplate);
        when(mockedJdbcTemplate.queryForObject(anyString(), any(LecturerMapper.class), anyInt())).thenThrow(QueryTimeoutException.class);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to get lecturer for lesson with id " + lessonId + ".",
                "Can't get lecturer for lesson with id " + lessonId + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonDAO.getLessonLecturer(lessonId);
            verify(mockedJdbcTemplate).queryForObject(anyString(), any(LecturerMapper.class), anyInt());
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
    void shouldGenerateLogsWhenSetLessonGroup() {
        ScriptUtils.executeSqlScript(connection, testData);
        int groupId = 1;
        int lessonId = 2;

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to assign lesson with id " + lessonId + " to group with id " + groupId + ".",
                "The lesson with id " + lessonId + " was assigned to group with id " + groupId + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonDAO.setLessonGroup(groupId, lessonId);

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenThrowDataAccessExceptionWhileSetLessonGroup() {
        int groupId = 1;
        int lessonId = 2;

        ReflectionTestUtils.setField(lessonDAO, "jdbcTemplate", mockedJdbcTemplate);
        doThrow(QueryTimeoutException.class).when(mockedJdbcTemplate).update(anyString(), anyInt(), anyInt());

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to assign lesson with id " + lessonId + " to group with id " + groupId + ".",
                "Can't set lesson with id " + lessonId + " to group with id " + groupId + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonDAO.setLessonGroup(groupId, lessonId);
            verify(mockedJdbcTemplate).update(anyString(), anyInt(), anyInt());
        } catch(DAOException daoException) {
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
    void shouldGenerateLogsWhenGetLessonGroup() {
        ScriptUtils.executeSqlScript(connection, testData);
        int lessonId = 2;
        Group expectedGroup = new Group();
        expectedGroup.setId(3);
        expectedGroup.setName("TestGroup3");

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to get group which was assigned for lesson with id " + lessonId + ".",
                "The result group for lesson with id " + lessonId + " is " + expectedGroup + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonDAO.getLessonGroup(lessonId);

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenThrowEmptyResultDataAccessExceptionWhileGetLessonGroup() {
        int lessonId = 2;

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to get group which was assigned for lesson with id " + lessonId + ".",
                "There is no group from lesson with id " + lessonId + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonDAO.getLessonGroup(lessonId);
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
    void shouldGenerateLogsWhenThrowDataAccessExceptionWhileGetLessonGroup() {
        int lessonId = 2;

        ReflectionTestUtils.setField(lessonDAO, "jdbcTemplate", mockedJdbcTemplate);
        when(mockedJdbcTemplate.queryForObject(anyString(), any(GroupMapper.class), any())).thenThrow(QueryTimeoutException.class);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to get group which was assigned for lesson with id " + lessonId + ".",
                "Can't get group from lesson with id " + lessonId + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonDAO.getLessonGroup(lessonId);
            verify(mockedJdbcTemplate).queryForObject(anyString(), any(GroupMapper.class), anyInt());
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
    void shouldGenerateLogsWhenSetLessonTime() {
        ScriptUtils.executeSqlScript(connection, testData);
        int lessonTimeId = 1;
        int lessonId = 2;

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to set lessonTime with id " + lessonTimeId + " for lesson with id " + lessonId + ".",
                "The lessonTime with id " + lessonTimeId + " was setted for lesson with id " + lessonId + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonDAO.setLessonTime(lessonTimeId, lessonId);

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenThrowDataAccessExceptionWhileSetLessonTime() {
        int lessonTimeId = 1;
        int lessonId = 2;

        ReflectionTestUtils.setField(lessonDAO, "jdbcTemplate", mockedJdbcTemplate);
        doThrow(QueryTimeoutException.class).when(mockedJdbcTemplate).update(anyString(), anyInt(), any());

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to set lessonTime with id " + lessonTimeId + " for lesson with id " + lessonId + ".",
                "Can't set lessonTime with id " + lessonTimeId + " for lesson with id " + lessonId + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonDAO.setLessonTime(lessonTimeId, lessonId);
            verify(mockedJdbcTemplate).update(anyString(), anyInt(), anyInt());
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
    void shouldGenerateLogsWhenGetLessonTime() {
        ScriptUtils.executeSqlScript(connection, testData);
        int lessonId = 2;
        LessonTime expectedLessonTime = new LessonTime();
        expectedLessonTime.setId(1);
        expectedLessonTime.setStartTime(LocalTime.of(9, 0));
        expectedLessonTime.setEndTime(LocalTime.of(10, 30));

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to get lessonTime for lesson with id " + lessonId + ".",
                "The result lessonTime for lesson with id " + lessonId + " is " + expectedLessonTime + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonDAO.getLessonTime(lessonId);

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenThrowEmptyResultDataAccesExceptionWhileGetLessonTime() {
        int lessonId = 2;

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to get lessonTime for lesson with id " + lessonId + ".",
                "There is no lessonTime for lesson with id " + lessonId + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonDAO.getLessonTime(lessonId);
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
    void shouldGenerateLogsWhenThrowDataAccesExceptionWhileGetLessonTime() {
        int lessonId = 2;

        ReflectionTestUtils.setField(lessonDAO, "jdbcTemplate", mockedJdbcTemplate);
        when(mockedJdbcTemplate.queryForObject(anyString(), any(LessonTimeMapper.class), anyInt())).thenThrow(QueryTimeoutException.class);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to get lessonTime for lesson with id " + lessonId + ".",
                "Can't get lessonTime for lesson with id " + lessonId + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonDAO.getLessonTime(lessonId);
            verify(mockedJdbcTemplate).queryForObject(anyString(), any(LessonTimeMapper.class), anyInt());
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
    void shouldGenerateLogsWhenGetGroupDayLessonsIsEmpty() {
        ScriptUtils.executeSqlScript(connection, testData);
        int groupId = 2;
        DayOfWeek weekDay = DayOfWeek.SATURDAY;

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.WARN));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to get all lessons for group with id " + groupId + " which is on a day " + weekDay + ".",
                "There are not any lessons for group with id " + groupId + " on a day " + weekDay + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonDAO.getGroupDayLessons(groupId, weekDay);

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenGetGroupDayLessonsHasResult() {
        ScriptUtils.executeSqlScript(connection, testData);
        int groupId = 2;
        DayOfWeek weekDay = DayOfWeek.THURSDAY;
        int expectedLessonId = 4;

        List<Lesson> expectedGroupLessons = expectedLessons.stream().filter(lesson -> lesson.getId() == expectedLessonId)
                .collect(Collectors.toList());

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to get all lessons for group with id " + groupId + " which is on a day " + weekDay + ".",
                "For group with id " + groupId + " on a day " + weekDay + " there are lessons: " + expectedGroupLessons + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonDAO.getGroupDayLessons(groupId, weekDay);

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenThrowDataAccessExceptionWhileGetGroupDayLessonsIsEmpty() {
        int groupId = 2;
        DayOfWeek weekDay = DayOfWeek.SATURDAY;

        ReflectionTestUtils.setField(lessonDAO, "jdbcTemplate", mockedJdbcTemplate);
        when(mockedJdbcTemplate.query(anyString(), any(LessonMapper.class), anyInt(), anyInt())).thenThrow(QueryTimeoutException.class);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to get all lessons for group with id " + groupId + " which is on a day " + weekDay + ".",
                "Can't get lessons for group with id " + groupId + " on a day " + weekDay + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonDAO.getGroupDayLessons(groupId, weekDay);
            verify(mockedJdbcTemplate).query(anyString(), any(LessonMapper.class), anyInt(), anyInt());
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
    void shouldGenerateLogsWhenGetLecturerDayLessonsIsEmpty() {
        ScriptUtils.executeSqlScript(connection, testData);
        int lecturerId = 2;
        DayOfWeek weekDay = DayOfWeek.SATURDAY;

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.WARN));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to get all lessons for lecturer with id " + lecturerId + " on a day " + weekDay + ".",
                "There are not any lessons for lecturer with id " + lecturerId + " on a day " + weekDay + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonDAO.getLecturerDayLessons(lecturerId, weekDay);

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenGetLecturerDayLessonsHasResult() {
        ScriptUtils.executeSqlScript(connection, testData);
        int lecturerId = 3;
        DayOfWeek weekDay = DayOfWeek.THURSDAY;
        List<Integer> expectedLessonIdList = new ArrayList<>(Arrays.asList(3, 4));

        List<Lesson> expectedLecturerLessons = expectedLessons.stream().filter(lesson -> expectedLessonIdList.contains(lesson.getId()))
                .collect(Collectors.toList());

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to get all lessons for lecturer with id " + lecturerId + " on a day " + weekDay + ".",
                "For lecturer with id " + lecturerId + " on a day " + weekDay + " there are lessons: " + expectedLecturerLessons + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonDAO.getLecturerDayLessons(lecturerId, weekDay);

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenThrowDataAccessExceptionWhileGetLecturerDayLessonsHasResult() {
        int lecturerId = 3;
        DayOfWeek weekDay = DayOfWeek.THURSDAY;

        ReflectionTestUtils.setField(lessonDAO, "jdbcTemplate", mockedJdbcTemplate);
        when(mockedJdbcTemplate.query(anyString(), any(LessonMapper.class), anyInt(), anyInt())).thenThrow(QueryTimeoutException.class);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to get all lessons for lecturer with id " + lecturerId + " on a day " + weekDay + ".",
                "Can't get lessons for lecturer with id " + lecturerId + " on a day " + weekDay + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonDAO.getLecturerDayLessons(lecturerId, weekDay);
            verify(mockedJdbcTemplate).query(anyString(), any(LessonMapper.class), anyInt(), anyInt());
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