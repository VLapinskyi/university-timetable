package ua.com.foxminded.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
import ua.com.foxminded.domain.Faculty;
import ua.com.foxminded.domain.Gender;
import ua.com.foxminded.domain.Group;
import ua.com.foxminded.domain.Lecturer;
import ua.com.foxminded.domain.Lesson;
import ua.com.foxminded.domain.LessonTime;
import ua.com.foxminded.repositories.aspects.GeneralRepositoryAspect;
import ua.com.foxminded.repositories.aspects.LessonRepositoryAspect;
import ua.com.foxminded.repositories.exceptions.RepositoryException;
import ua.com.foxminded.repositories.interfaces.LessonRepository;

@DataJpaTest(showSql = true)
@Import({AopAutoConfiguration.class, GeneralRepositoryAspect.class, LessonRepositoryAspect.class})
@TestPropertySource("/application-test.properties")
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
class LessonRepositoryTest {
private final String testData = "/Test data.sql";
    
    private ListAppender<ILoggingEvent> testAppender;
    
    @Autowired
    private GeneralRepositoryAspect generalRepositoryAspect;
    
    @Autowired
    private LessonRepositoryAspect lessonRepositoryAspect;
    
    @Autowired
    private TestEntityManager testEntityManager;
    
    @Autowired
    @SpyBean
    private LessonRepository lessonRepository;
    
    private List<Lesson> expectedLessons;

    @BeforeEach
    void setUp() throws Exception {
        Logger generalLogger = (Logger) ReflectionTestUtils.getField(generalRepositoryAspect, "logger");
        Logger lessonLogger = (Logger) ReflectionTestUtils.getField(lessonRepositoryAspect, "logger");
        testAppender = new ListAppender<>();
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        testAppender.setContext(loggerContext);
        testAppender.start();
        generalLogger.addAppender(testAppender);
        lessonLogger.addAppender(testAppender);
        
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
    }

    @AfterEach
    void tearDown() throws Exception {
        testAppender.stop();
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

        lessonRepository.save(testLesson);
        
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
        Optional<Lesson> expectedLesson = expectedLessons.stream().filter(lesson -> lesson.getId() == testId).findAny();

        assertEquals(expectedLesson, lessonRepository.findById(testId));
    }

    @Test
    @Sql(testData)
    void shouldUpdateLesson() {
        int testId = 1;
        
        Lesson testLesson = testEntityManager.find(Lesson.class, testId);
        testLesson.setAudience("999");

        lessonRepository.save(testLesson);
        assertEquals(testLesson, testEntityManager.find(Lesson.class, testId));

    }

    @Test
    @Sql(testData)
    void shouldDeleteById() {
        int deletedId = 3;        
        lessonRepository.deleteById(deletedId);
        
        Lesson afterDeletingLesson = testEntityManager.find(Lesson.class, deletedId);
        assertThat(afterDeletingLesson).isNull();
    }

    @Test
    @Sql(testData)
    void shouldFindDayLessonsForGroup() {
        int groupId = 1;
        DayOfWeek testDay = DayOfWeek.SUNDAY;
        Lesson expectedLesson = expectedLessons.get(0);
        List<Lesson> actualLessons = lessonRepository.findByGroupIdAndDay(groupId, testDay);
        assertTrue(actualLessons.contains(expectedLesson) && actualLessons.size() == 1);
    }

    @Test
    @Sql(testData)
    void shouldFindDayLessonsForLecturer() {
        int lecturerId = 6;
        DayOfWeek testDay = DayOfWeek.WEDNESDAY;
        expectedLessons = new ArrayList<>(Arrays.asList(expectedLessons.get(2), expectedLessons.get(3)));

        List<Lesson> actualLessons = lessonRepository.findByLecturerIdAndDay(lecturerId, testDay);

        assertTrue(expectedLessons.containsAll(actualLessons) && actualLessons.containsAll(expectedLessons));
    }

    @Test
    @Sql(testData)
    void shouldGenerateLogsWhenSave() {
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
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to save/update an object: " + testLesson + ".",
                "The object " + loggingResultLesson + " was saved/updated."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonRepository.save(testLesson);

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldThrowRepositoryExceptionWhenDataAccessExceptionWhileSave() {
        Lesson lesson = null;
        RuntimeException exception = assertThrows(RepositoryException.class, () -> lessonRepository.save(lesson));
    
        String message = "Can't save/update the object.";
        assertEquals(message, exception.getMessage());
    }

    @Test
    void shouldThrowRepositoryExceptionWhenResultIsNullPointerExceptionWhileFindById() {
        int testId = 1;
        RuntimeException exception = assertThrows(RepositoryException.class, () -> lessonRepository.findById(testId));
    
        String message = "There is no object with specified id.";
        assertEquals(message, exception.getMessage());
    }

    @Test
    @Sql(testData)
    void shouldThrowRepositoryExceptionWhenDataAccessExceptionWhileFindById() {
        Integer testId = null;
        RuntimeException exception = assertThrows(RepositoryException.class, () -> lessonRepository.findById(testId));
    
        String message = "Can't find an object by id.";
        assertEquals(message, exception.getMessage());
    }

    @Test
    @Sql(testData)
    void shouldThrowRepositoryExceptionWhenDataAccessExceptionWhileDeleteById() {
        Integer testId = null;
        RuntimeException exception = assertThrows(RepositoryException.class, () -> lessonRepository.deleteById(testId));
    
        String message = "Can't delete an object by id.";
        assertEquals(message, exception.getMessage());
    }

    @Test
    @Sql(testData)
    void shouldGenerateLogsWhenThrowDataAccessExceptionWhileSave() {        
        Lesson testLesson = null;

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to save/update an object: " + testLesson + ".",
                "Can't save/update the object: " + testLesson + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }
        try {
            lessonRepository.save(testLesson);
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

        lessonRepository.findAll();

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
                Arrays.asList("Try to find all objects.", "The result is: " + expectedLessons + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonRepository.findAll();

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
        Optional<Lesson> expectedLesson = expectedLessons.stream().filter(lesson -> lesson.getId() == testId).findFirst();
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to find an object by id: " + testId + ".",
                "The result object with id " + testId + " is " + expectedLesson + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonRepository.findById(testId);

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
            lessonRepository.findById(testId);
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
            lessonRepository.findById(testId);
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
                "The object with id " + testId + " was deleted."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonRepository.deleteById(testId);

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
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to delete an object by id " + testId + ".",
                "Can't delete an object by id " + testId + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonRepository.deleteById(testId);
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
    void shouldGenerateLogsWhenFindGroupDayLessonsIsEmpty() {
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

        lessonRepository.findByGroupIdAndDay(groupId, weekDay);

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    @Sql(testData)
    void shouldGenerateLogsWhenFindGroupDayLessonsHasResult() {
        int groupId = 2;
        DayOfWeek weekDay = DayOfWeek.WEDNESDAY;
        
        int expectedLessonId = 4;
        Lesson expectedLesson = testEntityManager.find(Lesson.class, expectedLessonId);        
        
        List<Lesson> expectedGroupLessons = new ArrayList<>(Arrays.asList(expectedLesson));

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

        lessonRepository.findByGroupIdAndDay(groupId, weekDay);

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenFindLecturerDayLessonsIsEmpty() {
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

        lessonRepository.findByLecturerIdAndDay(lecturerId, weekDay);

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    @Sql(testData)
    void shouldGenerateLogsWhenFindLecturerDayLessonsHasResult() {
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

        lessonRepository.findByLecturerIdAndDay(lecturerId, weekDay);

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }
}