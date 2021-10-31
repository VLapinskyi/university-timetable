package ua.com.foxminded.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
import ua.com.foxminded.domain.Faculty;
import ua.com.foxminded.domain.Gender;
import ua.com.foxminded.domain.Group;
import ua.com.foxminded.domain.Lecturer;
import ua.com.foxminded.domain.Lesson;
import ua.com.foxminded.domain.LessonTime;
import ua.com.foxminded.repositories.aspects.GeneralRepositoryAspect;
import ua.com.foxminded.repositories.aspects.LessonRepositoryAspect;
import ua.com.foxminded.repositories.exceptions.RepositoryException;

@DataJpaTest(showSql = true)
@Import({AopAutoConfiguration.class, GeneralRepositoryAspect.class, LessonRepositoryAspect.class})
@TestPropertySource("/application-test.properties")
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
class LessonRepositoryTest {
private final String testData = "/Test data.sql";
    
    private ListAppender<ILoggingEvent> testGeneralAppender;
    private ListAppender<ILoggingEvent> testLessonAppender;
    
    @Autowired
    private GeneralRepositoryAspect generalRepositoryAspect;
    
    @Autowired
    private LessonRepositoryAspect lessonRepositoryAspect;
    
    @Autowired
    private TestEntityManager testEntityManager;
    
    @MockBean
    private EntityManager mockedEntityManager;   
    
    @Autowired
    @SpyBean
    private LessonRepository lessonRepository;
    
    private List<Lesson> expectedLessons;

    @BeforeEach
    void setUp() throws Exception {
        Logger generalLogger = (Logger) ReflectionTestUtils.getField(generalRepositoryAspect, "logger");
        testGeneralAppender = new ListAppender<>();
        LoggerContext generalLoggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        testGeneralAppender.setContext(generalLoggerContext);
        testGeneralAppender.start();
        generalLogger.addAppender(testGeneralAppender);
        
        Logger lessonLogger = (Logger) ReflectionTestUtils.getField(lessonRepositoryAspect, "logger");
        testLessonAppender = new ListAppender<>();
        LoggerContext lessonLoggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        testLessonAppender.setContext(lessonLoggerContext);
        testLessonAppender.start();
        lessonLogger.addAppender(testLessonAppender);
        
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
        lecturer1.setId(4);
        lecturer1.setFirstName("Olena");
        lecturer1.setLastName("Skladenko");
        lecturer1.setGender(Gender.FEMALE);
        lecturer1.setPhoneNumber("+380991111111");
        lecturer1.setEmail("oskladenko@gmail.com");
        
        Lecturer lecturer2 = new Lecturer();
        lecturer2.setId(5);
        lecturer2.setFirstName("Ihor");
        lecturer2.setLastName("Zakharchuk");
        lecturer2.setGender(Gender.MALE);
        lecturer2.setPhoneNumber("+380125263741");
        lecturer2.setEmail("i.zakharchuk@gmail.com");
        
        Lecturer lecturer3 = new Lecturer();
        lecturer3.setId(6);
        lecturer3.setFirstName("Vasyl");
        lecturer3.setLastName("Dudchenko");
        lecturer3.setGender(Gender.MALE);
        lecturer3.setPhoneNumber("+380457895263");
        lecturer3.setEmail("vdudchenko@test.com");
        
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
        
        ReflectionTestUtils.setField(lessonRepository, "entityManager", testEntityManager.getEntityManager());
        
    }

    @AfterEach
    void tearDown() throws Exception {
        testGeneralAppender.stop();
        testLessonAppender.stop();
    }

    @Test
    @Sql(testData)
    void shouldCreateLesson() {
        Group group = testEntityManager.find(Group.class, 1);
        Lecturer lecturer = testEntityManager.find(Lecturer.class, 5);
        LessonTime lessonTime = testEntityManager.find(LessonTime.class, 1);
        
        Lesson testLesson = new Lesson();
        testLesson.setName("Ukranian");
        testLesson.setAudience("101");
        testLesson.setDay(DayOfWeek.TUESDAY);
        testLesson.setGroup(group);
        testLesson.setLecturer(lecturer);
        testLesson.setLessonTime(lessonTime);
        
        int maxLessonId = testEntityManager.getEntityManager().createQuery("FROM Lesson", Lesson.class).getResultStream()
                .max((lesson1, lesson2) -> Integer.compare(lesson1.getId(), lesson2.getId())).get().getId();
        int nextLessonId = maxLessonId + 1;

        Lesson expectedLesson = new Lesson();
        expectedLesson.setId(nextLessonId);
        expectedLesson.setName("Ukranian");
        expectedLesson.setAudience("101");
        expectedLesson.setDay(DayOfWeek.TUESDAY);
        expectedLesson.setGroup(group);
        expectedLesson.setLecturer(lecturer);
        expectedLesson.setLessonTime(lessonTime);

        lessonRepository.create(testLesson);
        
        Lesson actualLesson = testEntityManager.find(Lesson.class, nextLessonId);
        assertEquals(expectedLesson, actualLesson);
    }

    @Test
    @Sql(testData)
    void shouldFindAllLessons() {
        List<Lesson> actualLessons = lessonRepository.findAll();
        assertTrue(expectedLessons.containsAll(actualLessons) && actualLessons.containsAll(expectedLessons));
    }

    @Test
    @Sql(testData)
    void shouldFindLessonById() {
        int testId = 2;
        Lesson expectedLesson = expectedLessons.stream().filter(lesson -> lesson.getId() == testId).findAny().get();

        assertEquals(expectedLesson, lessonRepository.findById(testId));
    }

    @Test
    @Sql(testData)
    void shouldUpdateLesson() {
        int testId = 1;
        
        Lesson testLesson = testEntityManager.find(Lesson.class, testId);
        testLesson.setAudience("999");

        lessonRepository.update(testLesson);
        assertEquals(testLesson, lessonRepository.findById(testId));

    }

    @Test
    @Sql(testData)
    void shouldDeleteLesson() {
        int deletedId = 3;
        Lesson deletedLesson = testEntityManager.find(Lesson.class, deletedId);
        
        lessonRepository.delete(deletedLesson);
        
        Lesson afterDeletingLesson = testEntityManager.find(Lesson.class, deletedId);
        assertThat(afterDeletingLesson).isNull();
    }

    @Test
    @Sql(testData)
    void shouldGetDayLessonsForGroup() {
        int groupId = 1;
        DayOfWeek testDay = DayOfWeek.SUNDAY;
        Lesson expectedLesson = expectedLessons.get(0);
        List<Lesson> actualLessons = lessonRepository.getGroupDayLessons(groupId, testDay);
        assertTrue(actualLessons.contains(expectedLesson) && actualLessons.size() == 1);
    }

    @Test
    @Sql(testData)
    void shouldGetDayLessonsForLecturer() {
        int lecturerId = 6;
        DayOfWeek testDay = DayOfWeek.WEDNESDAY;
        expectedLessons = new ArrayList<>(Arrays.asList(expectedLessons.get(2), expectedLessons.get(3)));

        List<Lesson> actualLessons = lessonRepository.getLecturerDayLessons(lecturerId, testDay);

        assertTrue(expectedLessons.containsAll(actualLessons) && actualLessons.containsAll(expectedLessons));
    }

    @Test
    @Sql(testData)
    void shouldGenerateLogsWhenCreate() {
        Group group = testEntityManager.find(Group.class, 2);
        Lecturer lecturer = testEntityManager.find(Lecturer.class, 5);
        LessonTime lessonTime = testEntityManager.find(LessonTime.class, 3);
        
        Lesson testLesson = new Lesson();
        testLesson.setName("Math");
        testLesson.setAudience("103");
        testLesson.setDay(DayOfWeek.MONDAY);
        testLesson.setGroup(group);
        testLesson.setLecturer(lecturer);
        testLesson.setLessonTime(lessonTime);
        
        int maxLessonId = testEntityManager.getEntityManager().createQuery("FROM Lesson", Lesson.class).getResultStream()
                .max((lesson1, lesson2) -> Integer.compare(lesson1.getId(), lesson2.getId())).get().getId();

        int nextLessonId = maxLessonId + 1;
        
        Lesson loggingResultLesson = new Lesson();
        loggingResultLesson.setId(nextLessonId);
        loggingResultLesson.setName("Math");
        loggingResultLesson.setAudience("103");
        loggingResultLesson.setDay(DayOfWeek.MONDAY);
        loggingResultLesson.setGroup(group);
        loggingResultLesson.setLecturer(lecturer);
        loggingResultLesson.setLessonTime(lessonTime);
        
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to insert a new object: " + testLesson + ".",
                "The object " + loggingResultLesson + " was inserted."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonRepository.create(testLesson);

        List<ILoggingEvent> actualLogs = testGeneralAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldThrowRepositoryExceptionWhenPersistenceExceptionWhileCreate() {
        Lesson lesson = new Lesson();
        lesson.setDay(DayOfWeek.MONDAY);
        lesson.setId(2);
        doThrow(PersistenceException.class).when(lessonRepository).create(lesson);
        assertThrows(RepositoryException.class, () -> lessonRepository.create(lesson));
    }

    @Test
    @Sql(testData)
    void shouldThrowRepositoryExceptionWhenPersistenceExceptionWhileFindAll() {
        ReflectionTestUtils.setField(lessonRepository, "entityManager", mockedEntityManager);
        doThrow(PersistenceException.class).when(mockedEntityManager).createQuery("FROM Lesson", Lesson.class);
        assertThrows(RepositoryException.class, () -> lessonRepository.findAll());
    }

    @Test
    void shouldThrowRepositoryExceptionWhenResultIsNullPointerExceptionWhileFindById() {
        int testId = 1;
        assertThrows(RepositoryException.class, () -> lessonRepository.findById(testId));
    }

    @Test
    @Sql(testData)
    void shouldThrowRepositoryExceptionWhenPersistenceExceptionWhileFindById() {
        int testId = 1;
        ReflectionTestUtils.setField(lessonRepository, "entityManager", mockedEntityManager);
        when(mockedEntityManager.find(Lesson.class, testId)).thenThrow(PersistenceException.class);
        assertThrows(RepositoryException.class, () -> lessonRepository.findById(testId));
    }

    @Test
    void shouldThrowRepositoryExceptionWhenPersistenceExceptionWhileUpdate() {
        Lesson testLesson = new Lesson();
        testLesson.setDay(DayOfWeek.FRIDAY);
        testLesson.setId(5);
        ReflectionTestUtils.setField(lessonRepository, "entityManager", mockedEntityManager);
        doThrow(PersistenceException.class).when(mockedEntityManager).merge(testLesson);
        assertThrows(RepositoryException.class, () -> lessonRepository.update(testLesson));
    }

    @Test
    @Sql(testData)
    void shouldThrowRepositoryExceptionWhenPersistenceExceptionWhileDelete() {
        int testId = 1;
        Lesson deletedLesson = testEntityManager.find(Lesson.class, testId);
        ReflectionTestUtils.setField(lessonRepository, "entityManager", mockedEntityManager);
        when(mockedEntityManager.merge(deletedLesson)).thenThrow(PersistenceException.class);
        assertThrows(RepositoryException.class, () -> lessonRepository.delete(deletedLesson));
    }

    @Test
    void shouldThrowRepositoryExceptionWhenPersistenceExceptionWhileGetGroupDayLessons() {
        int groupId = 2;
        DayOfWeek weekDay = DayOfWeek.SATURDAY;
        ReflectionTestUtils.setField(lessonRepository, "entityManager", mockedEntityManager);
        doThrow(PersistenceException.class).when(mockedEntityManager).createQuery("from Lesson where group_id = :groupId" +
                " and week_day = :weekDay", Lesson.class);
        assertThrows(RepositoryException.class, () -> lessonRepository.getGroupDayLessons(groupId, weekDay));
    }

    @Test
    @Sql(testData)
    void shouldThrowRepositoryExceptionWhenPersistenceExceptionWhileGetLecturerDayLessons() {
        int lecturerId = 1;
        DayOfWeek weekDay = DayOfWeek.MONDAY;
        ReflectionTestUtils.setField(lessonRepository, "entityManager", mockedEntityManager);
        doThrow(PersistenceException.class).when(mockedEntityManager).createQuery("from Lesson where lecturer_id = :lecturerId" +
                " and week_day = :weekDay", Lesson.class);
        assertThrows(RepositoryException.class, () -> lessonRepository.getLecturerDayLessons(lecturerId, weekDay));
    }

    @Test
    @Sql(testData)
    void shouldGenerateLogsWhenThrowPersistenceExceptionWhileCreate() {
        Group group = testEntityManager.find(Group.class, 1);
        Lecturer lecturer = testEntityManager.find(Lecturer.class, 5);
        LessonTime lessonTime = testEntityManager.find(LessonTime.class, 2);
        
        Lesson testLesson = new Lesson();
        testLesson.setName("Test lesson");
        testLesson.setGroup(group);
        testLesson.setLecturer(lecturer);
        testLesson.setLessonTime(lessonTime);
        testLesson.setDay(DayOfWeek.FRIDAY);
        testLesson.setId(8);
        testLesson.setAudience("102");

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

        List<ILoggingEvent> actualLogs = testGeneralAppender.list;

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

        List<ILoggingEvent> actualLogs = testGeneralAppender.list;

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
                Arrays.asList("Try to find all objects.", "The result is: " + expectedLessons + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonRepository.findAll();

        List<ILoggingEvent> actualLogs = testGeneralAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenThrowPersistenceExceptionWhileFindAll() {
        ReflectionTestUtils.setField(lessonRepository, "entityManager", mockedEntityManager);
        doThrow(PersistenceException.class).when(mockedEntityManager).createQuery("FROM Lesson", Lesson.class);

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
        } catch (RepositoryException repositoryException) {
            // do nothing
        }

        List<ILoggingEvent> actualLogs = testGeneralAppender.list;

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

        List<ILoggingEvent> actualLogs = testGeneralAppender.list;

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
            lessonRepository.findById(testId);
        } catch (RepositoryException repositoryException) {
            // do nothing
        }

        List<ILoggingEvent> actualLogs = testGeneralAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenThrowPersistenceExceptionWhileFindById() {
        int testId = 2;

        ReflectionTestUtils.setField(lessonRepository, "entityManager", mockedEntityManager);
        when(mockedEntityManager.find(Lesson.class, testId)).thenThrow(PersistenceException.class);

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
        } catch (RepositoryException repositoryException) {
            // do nothing
        }

        List<ILoggingEvent> actualLogs = testGeneralAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    @Sql(testData)
    void shouldGenerateLogsWhenUpdate() {
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

        List<ILoggingEvent> actualLogs = testGeneralAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    @Sql(testData)
    void shouldGenerateLogsWhenThrowPersistenceExceptionWhileUpdate() {
        int testId = 4;
        Lesson testLesson = expectedLessons.stream().filter(lesson -> lesson.getId() == testId).findAny().get();
        testLesson.setName("Wrong lesson");
        
        ReflectionTestUtils.setField(lessonRepository, "entityManager", mockedEntityManager);
        when(mockedEntityManager.merge(testLesson)).thenThrow(PersistenceException.class);

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
        } catch (RepositoryException repositoryException) {
            // do nothing
        }

        List<ILoggingEvent> actualLogs = testGeneralAppender.list;

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
        Lesson deletedLesson = testEntityManager.find(Lesson.class, testId);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to delete an object " + deletedLesson+ ".",
                "The object " + deletedLesson + " was deleted."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonRepository.delete(deletedLesson);

        List<ILoggingEvent> actualLogs = testGeneralAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenThrowPersistenceExceptionWhileDelete() {
        int testId = 3;
        Lesson deletedLesson = expectedLessons.stream().filter(lesson -> lesson.getId() == testId).findAny().get();

        ReflectionTestUtils.setField(lessonRepository, "entityManager", mockedEntityManager);
        when(mockedEntityManager.merge(deletedLesson)).thenThrow(PersistenceException.class);
        
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
        } catch (RepositoryException repositoryException) {
            // do nothing
        }

        List<ILoggingEvent> actualLogs = testGeneralAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    @Sql(testData)
    void shouldGenerateLogsWhenGetGroupDayLessonsIsEmpty() {
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

        List<ILoggingEvent> actualLogs = testLessonAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    @Sql(testData)
    void shouldGenerateLogsWhenGetGroupDayLessonsHasResult() {
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

        List<ILoggingEvent> actualLogs = testLessonAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenThrowPersistenceExceptionWhileGetGroupDayLessons() {
        int groupId = 2;
        DayOfWeek weekDay = DayOfWeek.SATURDAY;
        
        ReflectionTestUtils.setField(lessonRepository, "entityManager", mockedEntityManager);
        doThrow(PersistenceException.class).when(mockedEntityManager).createQuery("from Lesson where group_id = :groupId" +
                " and week_day = :weekDay", Lesson.class);

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
        } catch (RepositoryException repositoryException) {
            // do nothing
        }

        List<ILoggingEvent> actualLogs = testLessonAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    @Sql(testData)
    void shouldGenerateLogsWhenGetLecturerDayLessonsIsEmpty() {
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

        List<ILoggingEvent> actualLogs = testLessonAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    @Sql(testData)
    void shouldGenerateLogsWhenGetLecturerDayLessonsHasResult() {
        int lecturerId = 6;
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

        List<ILoggingEvent> actualLogs = testLessonAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenThrowPersistenceExceptionWhileGetLecturerDayLessonsHasResult() {
        int lecturerId = 3;
        DayOfWeek weekDay = DayOfWeek.THURSDAY;
        
        ReflectionTestUtils.setField(lessonRepository, "entityManager", mockedEntityManager);
        doThrow(PersistenceException.class).when(mockedEntityManager).createQuery("from Lesson where lecturer_id = :lecturerId" +
                " and week_day = :weekDay", Lesson.class);

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
        } catch (RepositoryException repositoryException) {
            // do nothing
        }

        List<ILoggingEvent> actualLogs = testLessonAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }
}