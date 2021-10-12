package ua.com.foxminded.repositories;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

import java.sql.Connection;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
import ua.com.foxminded.domain.Faculty;
import ua.com.foxminded.domain.Gender;
import ua.com.foxminded.domain.Group;
import ua.com.foxminded.domain.Lecturer;
import ua.com.foxminded.domain.Lesson;
import ua.com.foxminded.domain.LessonTime;
import ua.com.foxminded.repositories.exceptions.RepositoryException;
import ua.com.foxminded.settings.SpringTestConfiguration;
import ua.com.foxminded.settings.TestAppender;

@ContextConfiguration(classes = { SpringTestConfiguration.class })
@ExtendWith(SpringExtension.class)
class LessonRepositoryTest {
    private final ClassPathResource testData = new ClassPathResource("/Test data.sql");
    private final ClassPathResource testTablesCreator = new ClassPathResource("/Creating tables.sql");
    private final ClassPathResource testDatabaseCleaner = new ClassPathResource("/Clearing database.sql");

    private TestAppender testAppender = new TestAppender();
    @Autowired
    private LessonRepository lessonRepository;
    @Autowired
    private SessionFactory sessionFactory;
    private List<Lesson> expectedLessons;
    private Connection connection;
    @Mock
    private SessionFactory mockedSessionFactory;

    @BeforeEach
    @Transactional
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        connection = ((SessionImpl)sessionFactory.getCurrentSession()).connection();
        ScriptUtils.executeSqlScript(connection, testTablesCreator);
        expectedLessons = new ArrayList<>(Arrays.asList(new Lesson(), new Lesson(), new Lesson(), new Lesson()));
        List<Integer> lessonIndexes = new ArrayList<>(Arrays.asList(1, 2, 3, 4));
        List<String> lessonNames = new ArrayList<>(
                Arrays.asList("Ukranian", "Music", "Physical Exercises", "Physical Exercises"));
        List<String> audiences = new ArrayList<>(Arrays.asList("101", "102", "103", "103"));
        List<DayOfWeek> weekDays = new ArrayList<>(
                Arrays.asList(DayOfWeek.SUNDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.WEDNESDAY));
        
        Faculty faculty1 = new Faculty();
        faculty1.setId(1);
        faculty1.setName("TestFaculty1");
        
        Faculty faculty2 = new Faculty();
        faculty2.setId(2);
        faculty2.setName("TestFaculty2");
        
        Group group1 = new Group();
        group1.setId(1);
        group1.setName("TestGroup1");
        group1.setFaculty(faculty1);
        
        Group group2 = new Group();
        group2.setId(2);
        group2.setName("TestGroup2");
        group2.setFaculty(faculty2);
        
        Group group3 = new Group();
        group3.setId(3);
        group3.setName("TestGroup3");
        group3.setFaculty(faculty1);
        
        List<Group> groups = new ArrayList<>(Arrays.asList(group1, group3, group1, group2));
        
        Lecturer lecturer1 = new Lecturer();
        lecturer1.setId(1);
        lecturer1.setFirstName("Olena");
        lecturer1.setLastName("Skladenko");
        lecturer1.setGender(Gender.FEMALE);
        lecturer1.setPhoneNumber("+380991111111");
        lecturer1.setEmail("oskladenko@gmail.com");
        
        Lecturer lecturer2 = new Lecturer();
        lecturer2.setId(2);
        lecturer2.setFirstName("Ihor");
        lecturer2.setLastName("Zakharchuk");
        lecturer2.setGender(Gender.MALE);
        lecturer2.setEmail("i.zakharchuk@gmail.com");
        
        Lecturer lecturer3 = new Lecturer();
        lecturer3.setId(3);
        lecturer3.setFirstName("Vasyl");
        lecturer3.setLastName("Dudchenko");
        lecturer3.setGender(Gender.MALE);
        
        List<Lecturer> lecturers = new ArrayList<>(Arrays.asList(lecturer1, lecturer2, lecturer3, lecturer3));
        
        LessonTime lessonTime1 = new LessonTime();
        lessonTime1.setId(1);
        lessonTime1.setStartTime(LocalTime.of(9, 0));
        lessonTime1.setEndTime(LocalTime.of(10, 30));
        
        LessonTime lessonTime2 = new LessonTime();
        lessonTime2.setId(2);
        lessonTime2.setStartTime(LocalTime.of(10, 45));
        lessonTime2.setEndTime(LocalTime.of(12, 15));
        
        LessonTime lessonTime3 = new LessonTime();
        lessonTime3.setId(3);
        lessonTime3.setStartTime(LocalTime.of(12, 30));
        lessonTime3.setEndTime(LocalTime.of(14, 00));
        
        List<LessonTime> lessonTimes = new ArrayList<>(Arrays.asList(lessonTime1, lessonTime1, lessonTime3, lessonTime2));
        
        for (int i = 0; i < expectedLessons.size(); i++) {
            expectedLessons.get(i).setId(lessonIndexes.get(i));
            expectedLessons.get(i).setName(lessonNames.get(i));
            expectedLessons.get(i).setAudience(audiences.get(i));
            expectedLessons.get(i).setDay(weekDays.get(i));
            expectedLessons.get(i).setGroup(groups.get(i));
            expectedLessons.get(i).setLecturer(lecturers.get(i));
            expectedLessons.get(i).setLessonTime(lessonTimes.get(i));
        }
        
    }

    @AfterEach
    @Transactional
    void tearDown() throws Exception {
        testAppender.cleanEventList();
        ReflectionTestUtils.setField(lessonRepository, "sessionFactory", sessionFactory);
        ScriptUtils.executeSqlScript(connection, testDatabaseCleaner);
    }

    @Test
    @Transactional
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

        lessonRepository.create(testLesson);
        assertEquals(expectedLesson, lessonRepository.findAll().stream().findFirst().get());
    }

    @Test
    @Transactional
    void shouldFindAllLessons() {
        ScriptUtils.executeSqlScript(connection, testData);
        List<Lesson> actualLessons = lessonRepository.findAll();
        assertTrue(expectedLessons.containsAll(actualLessons) && actualLessons.containsAll(expectedLessons));
    }

    @Test
    @Transactional
    void shouldFindLessonById() {
        ScriptUtils.executeSqlScript(connection, testData);
        int testId = 2;
        Lesson expectedLesson = expectedLessons.stream().filter(lesson -> lesson.getId() == testId).findAny().get();

        assertEquals(expectedLesson, lessonRepository.findById(testId));
    }

    @Test
    @Transactional
    void shouldUpdateLesson() {
        ScriptUtils.executeSqlScript(connection, testData);
        int testId = 1;
        
        Lesson testLesson = expectedLessons.stream().filter(lesson -> lesson.getId() == testId).findAny().get();
        testLesson.setAudience("999");

        lessonRepository.update(testLesson);
        assertEquals(testLesson, lessonRepository.findById(testId));

    }

    @Test
    @Transactional
    void shouldDeleteLesson() {
        ScriptUtils.executeSqlScript(connection, testData);
        int deletedId = 3;
        Lesson deletedLesson = new Lesson();
        for (int i = 0; i < expectedLessons.size(); i++) {
            if (expectedLessons.get(i).getId() == deletedId) {
                Lesson lessonFromList = expectedLessons.get(i);
                deletedLesson.setId(lessonFromList.getId());
                deletedLesson.setName(lessonFromList.getName());
                deletedLesson.setAudience(lessonFromList.getAudience());
                deletedLesson.setDay(lessonFromList.getDay());
                deletedLesson.setGroup(lessonFromList.getGroup());
                deletedLesson.setLecturer(lessonFromList.getLecturer());
                deletedLesson.setLessonTime(lessonFromList.getLessonTime());
                
                expectedLessons.remove(i);
                i--;
            }
        }
        lessonRepository.delete(deletedLesson);
        List<Lesson> actualLessons = lessonRepository.findAll();
        assertTrue(expectedLessons.containsAll(actualLessons) && actualLessons.containsAll(expectedLessons));
    }

    @Test
    @Transactional
    void shouldGetDayLessonsForGroup() {
        ScriptUtils.executeSqlScript(connection, testData);
        int groupId = 1;
        DayOfWeek testDay = DayOfWeek.SUNDAY;
        Lesson expectedLesson = expectedLessons.get(0);
        List<Lesson> actualLessons = lessonRepository.getGroupDayLessons(groupId, testDay);
        assertTrue(actualLessons.contains(expectedLesson) && actualLessons.size() == 1);
    }

    @Test
    @Transactional
    void shouldGetDayLessonsForLecturer() {
        ScriptUtils.executeSqlScript(connection, testData);
        int lecturerId = 3;
        DayOfWeek testDay = DayOfWeek.WEDNESDAY;
        expectedLessons = new ArrayList<>(Arrays.asList(expectedLessons.get(2), expectedLessons.get(3)));

        List<Lesson> actualLessons = lessonRepository.getLecturerDayLessons(lecturerId, testDay);

        assertTrue(expectedLessons.containsAll(actualLessons) && actualLessons.containsAll(expectedLessons));
    }

    @Test
    @Transactional
    void shouldGenerateLogsWhenCreate() {
        Lesson testLesson = new Lesson();
        testLesson.setName("Math");
        testLesson.setAudience("103");
        testLesson.setDay(DayOfWeek.MONDAY);

        Lesson loggingResultLesson = new Lesson();
        loggingResultLesson.setId(1);
        loggingResultLesson.setName("Math");
        loggingResultLesson.setAudience("103");
        loggingResultLesson.setDay(DayOfWeek.MONDAY);
        
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to insert a new object: " + loggingResultLesson + ".",
                "The object " + loggingResultLesson + " was inserted."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonRepository.create(testLesson);

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    @Transactional
    void shouldThrowRepositoryExceptionWhenPersistenceExceptionWhileCreate() {
        Lesson lesson = new Lesson();
        lesson.setDay(DayOfWeek.MONDAY);
        lesson.setId(2);
        assertThrows(RepositoryException.class, () -> lessonRepository.create(lesson));
    }

    @Test
    @Transactional
    void shouldThrowRepositoryExceptionWhenPersistenceExceptionWhileFindAll() {
        ReflectionTestUtils.setField(lessonRepository, "sessionFactory", mockedSessionFactory);
        when(mockedSessionFactory.getCurrentSession()).thenThrow(PersistenceException.class);
        assertThrows(RepositoryException.class, () -> lessonRepository.findAll());
    }

    @Test
    @Transactional
    void shouldThrowRepositoryExceptionWhenResultIsNullPointerExceptionWhileFindById() {
        int testId = 1;
        assertThrows(RepositoryException.class, () -> lessonRepository.findById(testId));
    }

    @Test
    @Transactional
    void shouldThrowRepositoryExceptionWhenPersistenceExceptionWhileFindById() {
        int testId = 1;
        ReflectionTestUtils.setField(lessonRepository, "sessionFactory", mockedSessionFactory);
        when(mockedSessionFactory.getCurrentSession()).thenThrow(PersistenceException.class);
        assertThrows(RepositoryException.class, () -> lessonRepository.findById(testId));
    }

    @Test
    @Transactional
    void shouldThrowRepositoryExceptionWhenPersistenceExceptionWhileUpdate() {
        Lesson testLesson = new Lesson();
        testLesson.setDay(DayOfWeek.FRIDAY);
        testLesson.setId(5);
        ReflectionTestUtils.setField(lessonRepository, "sessionFactory", mockedSessionFactory);
        when(mockedSessionFactory.getCurrentSession()).thenThrow(PersistenceException.class);
        assertThrows(RepositoryException.class, () -> lessonRepository.update(testLesson));
    }

    @Test
    @Transactional
    void shouldThrowRepositoryExceptionWhenPersistenceExceptionWhileDelete() {
        int testId = 1;
        Lesson deletedLesson = expectedLessons.stream().filter(lesson -> lesson.getId() == testId).findAny().get();
        ReflectionTestUtils.setField(lessonRepository, "sessionFactory", mockedSessionFactory);
        when(mockedSessionFactory.getCurrentSession()).thenThrow(PersistenceException.class);
        assertThrows(RepositoryException.class, () -> lessonRepository.delete(deletedLesson));
    }

    @Test
    @Transactional
    void shouldThrowRepositoryExceptionWhenPersistenceExceptionWhileGetGroupDayLessons() {
        int groupId = 2;
        DayOfWeek weekDay = DayOfWeek.SATURDAY;
        ReflectionTestUtils.setField(lessonRepository, "sessionFactory", mockedSessionFactory);
        when(mockedSessionFactory.getCurrentSession()).thenThrow(PersistenceException.class);
        assertThrows(RepositoryException.class, () -> lessonRepository.getGroupDayLessons(groupId, weekDay));
    }

    @Test
    @Transactional
    void shouldThrowRepositoryExceptionWhenPersistenceExceptionWhileGetLecturerDayLessons() {
        int lecturerId = 1;
        DayOfWeek weekDay = DayOfWeek.MONDAY;
        ReflectionTestUtils.setField(lessonRepository, "sessionFactory", mockedSessionFactory);
        when(mockedSessionFactory.getCurrentSession()).thenThrow(PersistenceException.class);
        assertThrows(RepositoryException.class, () -> lessonRepository.getLecturerDayLessons(lecturerId, weekDay));
    }

    @Test
    @Transactional
    void shouldGenerateLogsWhenThrowPersistenceExceptionWhileCreate() {
        Lesson testLesson = new Lesson();
        testLesson.setDay(DayOfWeek.FRIDAY);
        testLesson.setId(8);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to insert a new object: " + testLesson + ".",
                "Can't insert the object: " + testLesson + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }
        try {
            lessonRepository.create(testLesson);
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

        lessonRepository.findAll();

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
                Arrays.asList("Try to find all objects.", "The result is: " + expectedLessons + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonRepository.findAll();

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
        ReflectionTestUtils.setField(lessonRepository, "sessionFactory", mockedSessionFactory);
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
            lessonRepository.findAll();
            verify(mockedSessionFactory.getCurrentSession()).createQuery(anyString(), Lesson.class);
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
        Lesson expectedLesson = expectedLessons.stream().filter(lesson -> lesson.getId() == testId).findFirst().get();
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to find an object by id: " + testId + ".",
                "The result object with id " + testId + " is " + expectedLesson + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonRepository.findById(testId);

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
            lessonRepository.findById(testId);
            verify(mockedSessionFactory.getCurrentSession()).get(Lesson.class, testId);
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

        ReflectionTestUtils.setField(lessonRepository, "sessionFactory", mockedSessionFactory);
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
            lessonRepository.findById(testId);
            verify(mockedSessionFactory.getCurrentSession()).get(Lesson.class, testId);
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
    void shouldGenerateLogsWhenUpdate() {
        ScriptUtils.executeSqlScript(connection, testData);
        int testId = 1;
        Lesson testLesson = expectedLessons.stream().filter(lesson -> lesson.getId() == testId).findAny().get();
        testLesson.setAudience("6585");

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(
                Arrays.asList("Try to update an object " + testLesson + ".",
                        "The object " + testLesson + " was updated."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonRepository.update(testLesson);

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
        Lesson testLesson = new Lesson();
        testLesson.setDay(DayOfWeek.SATURDAY);
        testLesson.setId(42);

        ReflectionTestUtils.setField(lessonRepository, "sessionFactory", mockedSessionFactory);
        when(mockedSessionFactory.getCurrentSession()).thenThrow(PersistenceException.class);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(
                Arrays.asList("Try to update an object " + testLesson + ".",
                        "Can't update an object " + testLesson + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonRepository.update(testLesson);
            verify(mockedSessionFactory.getCurrentSession()).update(testLesson);
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
        Lesson deletedLesson = expectedLessons.stream().filter(lesson -> lesson.getId() == testId).findAny().get();

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to delete an object " + deletedLesson+ ".",
                "The object " + deletedLesson + " was deleted."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonRepository.delete(deletedLesson);

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    @Transactional
    void shouldGenerateLogsWhenThrowPersistenceExceptionWhileDelete() {
        int testId = 3;
        Lesson deletedLesson = expectedLessons.stream().filter(lesson -> lesson.getId() == testId).findAny().get();

        ReflectionTestUtils.setField(lessonRepository, "sessionFactory", mockedSessionFactory);
        when(mockedSessionFactory.getCurrentSession()).thenThrow(PersistenceException.class);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to delete an object " + deletedLesson + ".",
                "Can't delete an object " + deletedLesson + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonRepository.delete(deletedLesson);
            verify(mockedSessionFactory.getCurrentSession()).delete(deletedLesson);
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
    void shouldGenerateLogsWhenGetGroupDayLessonsIsEmpty() {
        ScriptUtils.executeSqlScript(connection, testData);
        int groupId = 2;
        DayOfWeek weekDay = DayOfWeek.SATURDAY;

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.WARN));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to get all lessons for a group with id " + groupId + " which is on a day " + weekDay + ".",
                "There are not any lesson for the group with id " + groupId + " on a day " + weekDay + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonRepository.getGroupDayLessons(groupId, weekDay);

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    @Transactional
    void shouldGenerateLogsWhenGetGroupDayLessonsHasResult() {
        ScriptUtils.executeSqlScript(connection, testData);
        int groupId = 2;
        DayOfWeek weekDay = DayOfWeek.WEDNESDAY;
        int expectedLessonId = 4;

        List<Lesson> expectedGroupLessons = expectedLessons.stream()
                .filter(lesson -> lesson.getId() == expectedLessonId).collect(Collectors.toList());

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to get all lessons for a group with id " + groupId + " which is on a day " + weekDay + ".",
                "For the group with id " + groupId + " on a day " + weekDay + " there are lessons: "
                        + expectedGroupLessons + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonRepository.getGroupDayLessons(groupId, weekDay);

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    @Transactional
    void shouldGenerateLogsWhenThrowPersistenceExceptionWhileGetGroupDayLessonsIsEmpty() {
        int groupId = 2;
        DayOfWeek weekDay = DayOfWeek.SATURDAY;

        ReflectionTestUtils.setField(lessonRepository, "sessionFactory", mockedSessionFactory);
        when(mockedSessionFactory.getCurrentSession()).thenThrow(PersistenceException.class);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to get all lessons for a group with id " + groupId + " which is on a day " + weekDay + ".",
                "Can't get lessons for a group with id " + groupId + " on a day " + weekDay + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonRepository.getGroupDayLessons(groupId, weekDay);
            verify(mockedSessionFactory.getCurrentSession()).createQuery(anyString(), Lesson.class);
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
    void shouldGenerateLogsWhenGetLecturerDayLessonsIsEmpty() {
        ScriptUtils.executeSqlScript(connection, testData);
        int lecturerId = 2;
        DayOfWeek weekDay = DayOfWeek.SATURDAY;

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.WARN));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to get all lessons for a lecturer with id " + lecturerId + " on a day " + weekDay + ".",
                "There are not any lesson for the lecturer with id " + lecturerId + " on a day " + weekDay + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonRepository.getLecturerDayLessons(lecturerId, weekDay);

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    @Transactional
    void shouldGenerateLogsWhenGetLecturerDayLessonsHasResult() {
        ScriptUtils.executeSqlScript(connection, testData);
        int lecturerId = 3;
        DayOfWeek weekDay = DayOfWeek.WEDNESDAY;
        List<Integer> expectedLessonIdList = new ArrayList<>(Arrays.asList(3, 4));

        List<Lesson> expectedLecturerLessons = expectedLessons.stream()
                .filter(lesson -> expectedLessonIdList.contains(lesson.getId())).collect(Collectors.toList());

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to get all lessons for a lecturer with id " + lecturerId + " on a day " + weekDay + ".",
                "For the lecturer with id " + lecturerId + " on a day " + weekDay + " there are lessons: "
                        + expectedLecturerLessons + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonRepository.getLecturerDayLessons(lecturerId, weekDay);

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    @Transactional
    void shouldGenerateLogsWhenThrowPersistenceExceptionWhileGetLecturerDayLessonsHasResult() {
        int lecturerId = 3;
        DayOfWeek weekDay = DayOfWeek.THURSDAY;

        ReflectionTestUtils.setField(lessonRepository, "sessionFactory", mockedSessionFactory);
        when(mockedSessionFactory.getCurrentSession()).thenThrow(PersistenceException.class);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to get all lessons for a lecturer with id " + lecturerId + " on a day " + weekDay + ".",
                "Can't get lessons for a lecturer with id " + lecturerId + " on a day " + weekDay + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonRepository.getLecturerDayLessons(lecturerId, weekDay);
            verify(mockedSessionFactory.getCurrentSession()).createQuery(anyString(), Lesson.class);
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