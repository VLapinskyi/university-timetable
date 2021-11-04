package ua.com.foxminded.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.any;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
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
import ua.com.foxminded.repositories.exceptions.RepositoryException;
import ua.com.foxminded.repositories.interfaces.LessonRepository;
import ua.com.foxminded.service.aspects.GeneralServiceAspect;
import ua.com.foxminded.service.aspects.LessonAspect;
import ua.com.foxminded.service.exceptions.ServiceException;
import ua.com.foxminded.settings.SpringTestConfiguration;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = SpringTestConfiguration.class)
class LessonServiceTest {
    private ListAppender<ILoggingEvent> testAppender;
    
    @Autowired
    private GeneralServiceAspect generalServiceAspect;
    
    @Autowired
    private LessonAspect lessonAspect;

    @Autowired
    private LessonService lessonService;

    @MockBean
    private LessonRepository lessonRepository;

    private Lecturer lecturer1;
    private Lecturer lecturer2;
    private Group group1;
    private Group group2;
    private LessonTime lessonTime1;
    private LessonTime lessonTime2;

    @Captor
    private ArgumentCaptor<DayOfWeek> dayCaptor;
    @Captor
    private ArgumentCaptor<Integer> numberCaptor;

    @BeforeEach
    void init() {
        ReflectionTestUtils.setField(lessonService, "lessonRepository", lessonRepository);

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger generalLogger = (Logger) ReflectionTestUtils.getField(generalServiceAspect, "logger");
        Logger lessonLogger = (Logger) ReflectionTestUtils.getField(lessonAspect, "logger");
        
        testAppender = new ListAppender<>();
        testAppender.setContext(loggerContext);
        testAppender.start();
        
        generalLogger.addAppender(testAppender);
        lessonLogger.addAppender(testAppender);
        
        lecturer1 = new Lecturer();
        lecturer1.setId(1);
        lecturer1.setFirstName("Roman");
        lecturer1.setLastName("Dudchenko");
        lecturer1.setGender(Gender.MALE);
        lecturer1.setEmail("dudchenko@gmail.com");
        lecturer1.setPhoneNumber("+380123456789");
        lecturer2 = new Lecturer();
        lecturer2.setId(2);
        lecturer2.setFirstName("Iryna");
        lecturer2.setLastName("Kasian");
        lecturer2.setGender(Gender.FEMALE);
        lecturer2.setEmail("kasian@gmail.com");
        lecturer2.setPhoneNumber("+380987654321");

        Faculty faculty = new Faculty();
        faculty.setId(1);
        faculty.setName("Faculty-1");

        group1 = new Group();
        group1.setId(1);
        group1.setName("Group-1");
        group1.setFaculty(faculty);
        group2 = new Group();
        group2.setId(2);
        group2.setName("Group-2");
        group2.setFaculty(faculty);

        lessonTime1 = new LessonTime();
        lessonTime1.setId(1);
        lessonTime1.setStartTime(LocalTime.of(9, 0));
        lessonTime1.setEndTime(LocalTime.of(11, 0));
        lessonTime2 = new LessonTime();
        lessonTime2.setId(2);
        lessonTime2.setStartTime(LocalTime.of(12, 0));
        lessonTime2.setEndTime(LocalTime.of(14, 0));
    }

    @AfterEach
    void tearDown() {
        testAppender.stop();
    }

    @Test
    void shouldCreateLesson() {
        Lesson creatingLesson = new Lesson();
        creatingLesson.setName("Lesson-2");
        creatingLesson.setAudience("103");
        creatingLesson.setLecturer(lecturer2);
        creatingLesson.setGroup(group1);
        creatingLesson.setDay(DayOfWeek.WEDNESDAY);
        creatingLesson.setLessonTime(lessonTime2);

        lessonService.create(creatingLesson);
        verify(lessonRepository).save(creatingLesson);
    }

    @Test
    void shouldGetAllLessons() {
        lessonService.getAll();
        verify(lessonRepository).findAll();
    }

    @Test
    void shouldGetLessonById() {
        int lessonId = 4;
        Lesson lesson = new Lesson();
        lesson.setId(lessonId);
        lesson.setName("Lesson-1");
        lesson.setAudience("107");
        lesson.setLecturer(lecturer2);
        lesson.setGroup(group1);
        lesson.setDay(DayOfWeek.MONDAY);
        lesson.setLessonTime(lessonTime1);
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        lessonService.getById(lessonId);
        verify(lessonRepository).findById(lessonId);
    }

    @Test
    void shouldUpdateLesson() {
        int lessonId = 7;
        Lesson lesson = new Lesson();
        lesson.setId(lessonId);
        lesson.setName("Lesson-1");
        lesson.setAudience("109");
        lesson.setLecturer(lecturer2);
        lesson.setGroup(group2);
        lesson.setDay(DayOfWeek.FRIDAY);
        lesson.setLessonTime(lessonTime2);

        lessonService.update(lesson);

        verify(lessonRepository).save(lesson);
    }

    @Test
    void shouldDeleteLessonById() {
        int lessonId = 456;
        lessonService.deleteById(lessonId);
        verify(lessonRepository).deleteById(lessonId);
    }

    @Test
    void shouldGetWeekLessonsForGroup() {
        int groupId = 2;
        lessonService.getGroupWeekLessons(groupId);
        for (int i = 1; i <= DayOfWeek.values().length; i++) {
            verify(lessonRepository).findByGroupIdAndDay(groupId, DayOfWeek.of(i));
        }
    }

    @Test
    void shouldGetMonthLessonForGroup() {
        int groupId = 1;
        YearMonth month = YearMonth.of(2021, Month.FEBRUARY);
        int monthLength = 28;
        List<DayOfWeek> expectedDays = new ArrayList<>();
        expectedDays.addAll(Arrays.asList(DayOfWeek.values()));
        expectedDays.addAll(Arrays.asList(DayOfWeek.values()));
        expectedDays.addAll(Arrays.asList(DayOfWeek.values()));
        expectedDays.addAll(Arrays.asList(DayOfWeek.values()));
        lessonService.getGroupMonthLessons(groupId, month);
        verify(lessonRepository, times(monthLength)).findByGroupIdAndDay(numberCaptor.capture(), dayCaptor.capture());

        List<DayOfWeek> actualDays = dayCaptor.getAllValues();
        List<Integer> actualGroupIndexes = numberCaptor.getAllValues();

        for (int i = 0; i < monthLength; i++) {
            assertEquals(expectedDays.get(i), actualDays.get(i));
            assertSame(groupId, actualGroupIndexes.get(i));
        }
    }

    @Test
    void shouldGetWeekLessonsForLecturer() {
        int lecturerId = 3;
        Lesson lesson1 = new Lesson();
        lesson1.setId(1);
        Lesson lesson2 = new Lesson();
        lesson2.setId(2);
        when(lessonRepository.findByLecturerIdAndDay(lecturerId, DayOfWeek.FRIDAY))
                .thenReturn(new ArrayList<Lesson>(Arrays.asList(lesson1, lesson2)));
        lessonService.getLecturerWeekLessons(lecturerId);
        for (int i = 1; i <= DayOfWeek.values().length; i++) {
            verify(lessonRepository).findByLecturerIdAndDay(lecturerId, DayOfWeek.of(i));
        }
    }

    @Test
    void shouldGetMonthLessonsForLecturer() {
        int lecturerId = 3;
        YearMonth month = YearMonth.of(2020, Month.DECEMBER);
        int monthLength = 31;
        List<DayOfWeek> expectedDays = new ArrayList<>();
        expectedDays.addAll(Arrays.asList(DayOfWeek.of(2), DayOfWeek.of(3), DayOfWeek.of(4), DayOfWeek.of(5),
                DayOfWeek.of(6), DayOfWeek.of(7)));
        expectedDays.addAll(Arrays.asList(DayOfWeek.values()));
        expectedDays.addAll(Arrays.asList(DayOfWeek.values()));
        expectedDays.addAll(Arrays.asList(DayOfWeek.values()));
        expectedDays.addAll(Arrays.asList(DayOfWeek.of(1), DayOfWeek.of(2), DayOfWeek.of(3), DayOfWeek.of(4)));
        lessonService.getLecturerMonthLessons(lecturerId, month);
        verify(lessonRepository, times(monthLength)).findByLecturerIdAndDay(numberCaptor.capture(), dayCaptor.capture());

        List<DayOfWeek> actualDays = dayCaptor.getAllValues();
        List<Integer> actualLecturerIndexes = numberCaptor.getAllValues();

        for (int i = 0; i < monthLength; i++) {
            assertEquals(expectedDays.get(i), actualDays.get(i));
            assertSame(lecturerId, actualLecturerIndexes.get(i));
        }
    }

    @Test
    void shouldThrowServiceExceptionWhenLessonIsNullWhileCreate() {
        Lesson lesson = null;
        assertThrows(ServiceException.class, () -> lessonService.create(lesson));
    }

    @Test
    void shouldThrowServiceExceptionWhenLessonIdIsNotZeroWhileCreate() {
        Lesson lesson = new Lesson();
        lesson.setId(4);
        lesson.setName("Lesson-4");
        lesson.setAudience("105");
        lesson.setLecturer(lecturer1);
        lesson.setGroup(group1);
        lesson.setDay(DayOfWeek.THURSDAY);
        lesson.setLessonTime(lessonTime1);

        assertThrows(ServiceException.class, () -> lessonService.create(lesson));
    }

    @Test
    void shouldThrowServiceExceptionWhenLessonNameIsNullWhileCreate() {
        Lesson lesson = new Lesson();
        lesson.setAudience("106");
        lesson.setLecturer(lecturer2);
        lesson.setGroup(group2);
        lesson.setDay(DayOfWeek.WEDNESDAY);
        lesson.setLessonTime(lessonTime2);

        assertThrows(ServiceException.class, () -> lessonService.create(lesson));
    }

    @Test
    void shouldThrowServiceExceptionWhenLessonNameIsShortWhileCreate() {
        Lesson lesson = new Lesson();
        lesson.setName("i       ");
        lesson.setAudience("104");
        lesson.setLecturer(lecturer2);
        lesson.setGroup(group1);
        lesson.setDay(DayOfWeek.MONDAY);
        lesson.setLessonTime(lessonTime1);

        assertThrows(ServiceException.class, () -> lessonService.create(lesson));
    }

    @Test
    void shouldThrowServiceExceptionWhenLessonNameStartsWithWhiteSpaceWhileCreate() {
        Lesson lesson = new Lesson();
        lesson.setName(" Lesson-2");
        lesson.setAudience("103");
        lesson.setLecturer(lecturer1);
        lesson.setGroup(group2);
        lesson.setDay(DayOfWeek.TUESDAY);
        lesson.setLessonTime(lessonTime1);

        assertThrows(ServiceException.class, () -> lessonService.create(lesson));
    }

    @Test
    void shouldThrowServiceExceptionWhenLessonLecturerIsNullWhileCreate() {
        Lesson lesson = new Lesson();
        lesson.setName("Lesson-5");
        lesson.setAudience("103");
        lesson.setGroup(group1);
        lesson.setDay(DayOfWeek.THURSDAY);
        lesson.setLessonTime(lessonTime1);

        assertThrows(ServiceException.class, () -> lessonService.create(lesson));
    }

    @Test
    void shouldThrowServiceExceptionWhenLessonLecturerIsInvalidWhileCreate() {
        Lesson lesson = new Lesson();
        lesson.setName("Lesson-5");
        lesson.setAudience("103");
        lesson.setGroup(group1);
        lesson.setDay(DayOfWeek.THURSDAY);
        lesson.setLessonTime(lessonTime1);

        lecturer1.setId(-4);
        lesson.setLecturer(lecturer1);

        assertThrows(ServiceException.class, () -> lessonService.create(lesson));
    }

    @Test
    void shouldThrowServiceExceptionWhenLessonGroupIsNullWhileCreate() {
        Lesson lesson = new Lesson();
        lesson.setName("Lesson-45");
        lesson.setAudience("1001");
        lesson.setLecturer(lecturer1);
        lesson.setDay(DayOfWeek.WEDNESDAY);
        lesson.setLessonTime(lessonTime1);

        assertThrows(ServiceException.class, () -> lessonService.create(lesson));
    }

    @Test
    void shouldThrowServiceExceptionWhenLessonGroupIsInvalidWhileCreate() {
        Lesson lesson = new Lesson();
        lesson.setName("Lesson-5");
        lesson.setAudience("103");
        lesson.setLecturer(lecturer1);
        lesson.setDay(DayOfWeek.THURSDAY);
        lesson.setLessonTime(lessonTime1);

        group2.setId(-1);
        lesson.setGroup(group2);

        assertThrows(ServiceException.class, () -> lessonService.create(lesson));
    }

    @Test
    void shouldThrowServiceExceptionWhenLessonAudienceIsNullWhileCreate() {
        Lesson lesson = new Lesson();
        lesson.setName("Lesson-8");
        lesson.setLecturer(lecturer1);
        lesson.setGroup(group2);
        lesson.setDay(DayOfWeek.MONDAY);
        lesson.setLessonTime(lessonTime1);

        assertThrows(ServiceException.class, () -> lessonService.create(lesson));
    }

    @Test
    void shouldThrowServiceExceptionWhenLessonAudienceIsShortWhileCreate() {
        Lesson lesson = new Lesson();
        lesson.setName("Lesson-2");
        lesson.setAudience("1  ");
        lesson.setLecturer(lecturer1);
        lesson.setGroup(group1);
        lesson.setDay(DayOfWeek.TUESDAY);
        lesson.setLessonTime(lessonTime1);

        assertThrows(ServiceException.class, () -> lessonService.create(lesson));
    }

    @Test
    void shouldThrowServiceExceptionWhenLessonAudienceStartsWithWhiteSpaceWhileCreate() {
        Lesson lesson = new Lesson();
        lesson.setName("Lesson-9");
        lesson.setAudience(" 101");
        lesson.setLecturer(lecturer2);
        lesson.setGroup(group2);
        lesson.setDay(DayOfWeek.FRIDAY);
        lesson.setLessonTime(lessonTime2);

        assertThrows(ServiceException.class, () -> lessonService.create(lesson));
    }

    @Test
    void shouldThrowServiceExceptionWhenLessonDayIsNullWhileCreate() {
        Lesson lesson = new Lesson();
        lesson.setName("Lesson-15");
        lesson.setAudience("204");
        lesson.setLecturer(lecturer2);
        lesson.setGroup(group1);
        lesson.setLessonTime(lessonTime2);

        assertThrows(ServiceException.class, () -> lessonService.create(lesson));
    }

    @Test
    void shouldThrowServiceExceptionWhenLessonTimeIsNullWhileCreate() {
        Lesson lesson = new Lesson();
        lesson.setName("Lesson-12");
        lesson.setAudience("303");
        lesson.setLecturer(lecturer2);
        lesson.setGroup(group2);
        lesson.setDay(DayOfWeek.WEDNESDAY);

        assertThrows(ServiceException.class, () -> lessonService.create(lesson));
    }

    @Test
    void shouldThrowServiceExceptionWhenLessonTimeIsInvalidWhileCreate() {
        Lesson lesson = new Lesson();
        lesson.setName("Lesson-13");
        lesson.setAudience("402");
        lesson.setLecturer(lecturer2);
        lesson.setGroup(group1);
        lesson.setDay(DayOfWeek.THURSDAY);

        lessonTime1.setEndTime(LocalTime.of(8, 0));
        lesson.setLessonTime(lessonTime1);

        assertThrows(ServiceException.class, () -> lessonService.create(lesson));
    }

    @Test
    void shouldThrowServiceExceptionWhenRepositoryExceptionWhileCreate() {
        Lesson lesson = new Lesson();
        lesson.setName("Lesson-18");
        lesson.setAudience("106");
        lesson.setLecturer(lecturer1);
        lesson.setGroup(group1);
        lesson.setDay(DayOfWeek.THURSDAY);
        lesson.setLessonTime(lessonTime2);

        doThrow(RepositoryException.class).when(lessonRepository).save(lesson);

        assertThrows(ServiceException.class, () -> lessonService.create(lesson));
    }

    @Test
    void shouldThrowServiceExceptionWhenRepositoryExceptionWhileGetAll() {
        when(lessonRepository.findAll()).thenThrow(RepositoryException.class);
        assertThrows(ServiceException.class, () -> lessonService.getAll());
    }

    @Test
    void shouldThrowServiceExceptionWhenLessonIdIsZeroWhileGetById() {
        int testId = 0;
        assertThrows(ServiceException.class, () -> lessonService.getById(testId));
    }

    @Test
    void shouldThrowServiceExceptionWhenRepositoryExceptionWhileGetById() {
        int testId = 4;
        when(lessonRepository.findById(testId)).thenThrow(RepositoryException.class);
        assertThrows(ServiceException.class, () -> lessonService.getById(testId));
    }

    @Test
    void shouldThrowServiceExceptionWhenLessonIsNullWhileUpdate() {
        Lesson lesson = null;
        assertThrows(ServiceException.class, () -> lessonService.update(lesson));
    }

    @Test
    void shouldThrowServiceExceptionWhenLessonIsInvalidWhileUpdate() {
        Lesson lesson = new Lesson();
        lesson.setId(3);
        lesson.setName("Lesson-3");
        lesson.setAudience(" 104");
        lesson.setLecturer(lecturer1);
        lesson.setGroup(group2);
        lesson.setDay(DayOfWeek.MONDAY);
        lesson.setLessonTime(lessonTime2);

        assertThrows(ServiceException.class, () -> lessonService.update(lesson));
    }

    @Test
    void shouldThrowServiceExceptionWhenRepositoryExceptionWhileUpdate() {
        Lesson lesson = new Lesson();
        lesson.setId(45);
        lesson.setName("Lesson-1");
        lesson.setAudience(" 114");
        lesson.setLecturer(lecturer2);
        lesson.setGroup(group1);
        lesson.setDay(DayOfWeek.MONDAY);
        lesson.setLessonTime(lessonTime2);

        doThrow(RepositoryException.class).when(lessonRepository).save(lesson);

        assertThrows(ServiceException.class, () -> lessonService.update(lesson));
    }

    @Test
    void shouldThrowServiceExceptionWhenLessonIdIsZeroWhileDeleteById() {
        int testId = 0;
        assertThrows(ServiceException.class, () -> lessonService.deleteById(testId));
    }

    @Test
    void shouldThrowServiceExceptioinWhenRepositoryExceptionWhileDeleteById() {
        int testId = 74;
        doThrow(RepositoryException.class).when(lessonRepository).deleteById(testId);
        assertThrows(ServiceException.class, () -> lessonService.deleteById(testId));
    }

    @Test
    void shouldThrowServiceExceptionWhenGroupIdIsZeroWhileGetGroupWeekLessons() {
        int testId = 0;
        assertThrows(ServiceException.class, () -> lessonService.getGroupWeekLessons(testId));
    }

    @Test
    void shouldThrowServiceExceptioinWhenRepositoryExceptionWhileGetGroupWeekLessons() {
        int testId = 74;
        DayOfWeek testDay = DayOfWeek.MONDAY;
        when(lessonRepository.findByGroupIdAndDay(testId, testDay)).thenThrow(RepositoryException.class);
        assertThrows(ServiceException.class, () -> lessonService.getGroupWeekLessons(testId));
    }

    @Test
    void shouldThrowServiceExceptionWhenGroupIdIsZeroWhileGetGroupMonthLessons() {
        int testId = 0;
        YearMonth testMonth = YearMonth.of(2021, 5);
        assertThrows(ServiceException.class, () -> lessonService.getGroupMonthLessons(testId, testMonth));
    }

    @Test
    void shouldThrowServiceExceptionWhenRepositoryExceptionWhileGetGroupMonthLessons() {
        int testId = 14;
        YearMonth testMonth = YearMonth.of(2021, 4);
        DayOfWeek testDay = DayOfWeek.THURSDAY;
        when(lessonRepository.findByGroupIdAndDay(testId, testDay)).thenThrow(RepositoryException.class);
        assertThrows(ServiceException.class, () -> lessonService.getGroupMonthLessons(testId, testMonth));
    }

    @Test
    void shouldThrowServiceExceptionWhenLecturerIdIsZeroWhileGetLecturerWeekLessons() {
        int testId = 0;
        assertThrows(ServiceException.class, () -> lessonService.getLecturerWeekLessons(testId));
    }

    @Test
    void shouldThrowServiceExceptioinWhenRepositoryExceptionWhileGetLecturerWeekLessons() {
        int testId = 12;
        DayOfWeek testDay = DayOfWeek.THURSDAY;
        when(lessonRepository.findByLecturerIdAndDay(testId, testDay)).thenThrow(RepositoryException.class);
        assertThrows(ServiceException.class, () -> lessonService.getLecturerWeekLessons(testId));
    }

    @Test
    void shouldThrowServiceExceptionWhenLecturerIdIsZeroWhileGetLecturerMonthLessons() {
        int testId = 0;
        YearMonth testMonth = YearMonth.of(2021, 3);
        assertThrows(ServiceException.class, () -> lessonService.getLecturerMonthLessons(testId, testMonth));
    }

    @Test
    void shouldThrowServiceExceptionWhenRepositoryExceptionWhileGetLecturerMonthLessons() {
        int testId = 19;
        YearMonth testMonth = YearMonth.of(2021, 2);
        DayOfWeek testDay = DayOfWeek.FRIDAY;
        when(lessonRepository.findByLecturerIdAndDay(testId, testDay)).thenThrow(RepositoryException.class);
        assertThrows(ServiceException.class, () -> lessonService.getLecturerMonthLessons(testId, testMonth));
    }

    @Test
    void shouldGenerateLogsWhenLessonIsNullWhileCreate() {
        Lesson lesson = null;
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to create a new lesson: " + lesson + ".",
                "A lesson " + lesson + " can't be null when create."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonService.create(lesson);
        } catch (ServiceException serviceException) {
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
    void shouldGenerateLogsWhenLessonIdIsNotZeroWhileCreate() {
        int testId = 6;

        Lesson lesson = new Lesson();
        lesson.setId(testId);
        lesson.setName("Lesson-6");
        lesson.setAudience("512");
        lesson.setLecturer(lecturer2);
        lesson.setGroup(group1);
        lesson.setDay(DayOfWeek.FRIDAY);
        lesson.setLessonTime(lessonTime1);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to create a new lesson: " + lesson + ".",
                "A lesson " + lesson + " has wrong id " + testId + " which is not equal zero when create."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonService.create(lesson);
        } catch (ServiceException serviceException) {
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
    void shouldGenerateLogsWhenLessonIsInvalidWhileCreate() {
        Lesson lesson = new Lesson();
        lesson.setName(" Lesson");
        lesson.setAudience("954");
        lesson.setLecturer(lecturer2);
        lesson.setGroup(group1);
        lesson.setDay(DayOfWeek.THURSDAY);
        lesson.setLessonTime(lessonTime1);

        String violationMessage = "Lesson name must have at least two symbols and start with non-white space";

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to create a new lesson: " + lesson + ".",
                "The lesson " + lesson + " is not valid when create. There are errors: " + violationMessage + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonService.create(lesson);
        } catch (ServiceException serviceException) {
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
    void shouldGenerateLogsWhenRepositoryExceptionWhileCreate() {
        Lesson lesson = new Lesson();
        lesson.setName("Lesson");
        lesson.setAudience("103");
        lesson.setLecturer(lecturer1);
        lesson.setGroup(group1);
        lesson.setDay(DayOfWeek.MONDAY);
        lesson.setLessonTime(lessonTime1);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to create a new lesson: " + lesson + ".",
                "There is some error in repositories layer when create an object " + lesson + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        doThrow(RepositoryException.class).when(lessonRepository).save(lesson);

        try {
            lessonService.create(lesson);
        } catch (ServiceException serviceException) {
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
    void shouldGenerateLogsWhenCreate() {
        Lesson lesson = new Lesson();
        lesson.setName("Lesson1");
        lesson.setAudience("106");
        lesson.setLecturer(lecturer2);
        lesson.setGroup(group2);
        lesson.setDay(DayOfWeek.TUESDAY);
        lesson.setLessonTime(lessonTime2);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(
                Arrays.asList("Try to create a new lesson: " + lesson + ".", "The object " + lesson + " was created."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonService.create(lesson);

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenResultIsEmptyWhileGetAll() {
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.WARN));
        List<String> expectedMessages = new ArrayList<>(
                Arrays.asList("Try to get all objects.", "There are not any objects in the result when getAll."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonService.getAll();

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenRepositoryExceptionWhileGetAll() {
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(
                Arrays.asList("Try to get all objects.", "There is some error in repositories layer when getAll."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        when(lessonRepository.findAll()).thenThrow(RepositoryException.class);

        try {
            lessonService.getAll();
        } catch (ServiceException serviceException) {
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
    void shouldGenerateLogsWhenGetAll() {
        List<Lesson> expectedLessons = new ArrayList<>(Arrays.asList(new Lesson(), new Lesson(), new Lesson()));
        List<String> names = new ArrayList<>(Arrays.asList("Lesson-1", "Lesson-2", "Lesson-3"));
        List<String> audiences = new ArrayList<>(Arrays.asList("101", "102", "103"));
        List<Lecturer> lecturers = new ArrayList<>(Arrays.asList(lecturer1, lecturer2, lecturer1));
        List<Group> groups = new ArrayList<>(Arrays.asList(group1, group1, group2));
        List<DayOfWeek> days = new ArrayList<>(
                Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.THURSDAY, DayOfWeek.WEDNESDAY));
        List<LessonTime> lessonTimes = new ArrayList<>(Arrays.asList(lessonTime2, lessonTime2, lessonTime1));

        for (int i = 0; i < expectedLessons.size(); i++) {
            int index = i + 1;
            expectedLessons.get(i).setId(index);
            expectedLessons.get(i).setName(names.get(i));
            expectedLessons.get(i).setAudience(audiences.get(i));
            expectedLessons.get(i).setLecturer(lecturers.get(i));
            expectedLessons.get(i).setGroup(groups.get(i));
            expectedLessons.get(i).setDay(days.get(i));
            expectedLessons.get(i).setLessonTime(lessonTimes.get(i));
        }

        when(lessonRepository.findAll()).thenReturn(expectedLessons);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(
                Arrays.asList("Try to get all objects.", "The result is: " + expectedLessons + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonService.getAll();

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenLessonIdIsNegativeWhileGetById() {
        int negativeId = -4;

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to get an object by id: " + negativeId + ".",
                "A given id " + negativeId + " is less than 1 when getById."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonService.getById(negativeId);
        } catch (ServiceException serviceException) {
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
    void shouldGenerateLogsWhenEntityIsNotFoundInDatabaseWhileGetById() {
        int testId = 2;

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to get an object by id: " + testId + ".",
                "The entity is not found when get object by id " + testId + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        RepositoryException repositoryException = new RepositoryException("The result is empty", new NullPointerException());
        when(lessonRepository.findById(testId)).thenThrow(repositoryException);

        try {
            lessonService.getById(testId);
        } catch (ServiceException serviceException) {
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
    void shouldGenerateLogsWhenRepositoryExceptionWhileGetById() {
        int testId = 1;

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to get an object by id: " + testId + ".",
                "There is some error in repositories layer when get object by id " + testId + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        when(lessonRepository.findById(testId)).thenThrow(RepositoryException.class);

        try {
            lessonService.getById(testId);
        } catch (ServiceException serviceException) {
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
    void shouldGenerateLogsWhenGetById() {
        int testId = 3;

        Lesson lesson = new Lesson();
        lesson.setId(testId);
        lesson.setName("Lesson-3");
        lesson.setAudience("451");
        lesson.setLecturer(lecturer1);
        lesson.setGroup(group2);
        lesson.setDay(DayOfWeek.TUESDAY);
        lesson.setLessonTime(lessonTime1);

        when(lessonRepository.findById(testId)).thenReturn(Optional.of(lesson));
        
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to get an object by id: " + testId + ".",
                "The result object with id " + testId + " is " + lesson + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonService.getById(testId);

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenLessonIdIsZeroWhileUpdate() {
        Lesson lesson = new Lesson();
        lesson.setName("Lesson");
        lesson.setAudience("100");
        lesson.setLecturer(lecturer1);
        lesson.setGroup(group1);
        lesson.setDay(DayOfWeek.TUESDAY);
        lesson.setLessonTime(lessonTime2);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to update a lesson: " + lesson + ".",
                "An updated lesson " + lesson + " has wrong id " + lesson.getId() + " which is not positive."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonService.update(lesson);
        } catch (ServiceException serviceException) {
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
    void shouldGenerateLogsWhenLessonIsInvalidWhileUpdate() {
        Lesson lesson = new Lesson();
        lesson.setId(5);
        lesson.setName("Lesson");
        lesson.setAudience("456");
        lesson.setLecturer(lecturer1);
        lesson.setDay(DayOfWeek.FRIDAY);
        lesson.setLessonTime(lessonTime2);

        String violationMessage = "Lesson group can't be null";

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to update a lesson: " + lesson + ".",
                "The lesson " + lesson + " is not valid when update. There are errors: " + violationMessage + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonService.update(lesson);
        } catch (ServiceException serviceException) {
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
    void shouldGenerateLogsWhenRepositoryExceptionWhileUpdate() {
        Lesson lesson = new Lesson();
        lesson.setId(9);
        lesson.setName("Lesson-9");
        lesson.setAudience("951");
        lesson.setLecturer(lecturer2);
        lesson.setGroup(group1);
        lesson.setDay(DayOfWeek.MONDAY);
        lesson.setLessonTime(lessonTime1);

        doThrow(RepositoryException.class).when(lessonRepository).save(lesson);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to update a lesson: " + lesson + ".",
                "There is some error in repositories layer when update an object " + lesson + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonService.update(lesson);
        } catch (ServiceException serviceException) {
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
    void shouldGenerateLogsWhenUpdate() {
        Lesson lesson = new Lesson();
        lesson.setId(4);
        lesson.setName("Lesson-4");
        lesson.setAudience("410");
        lesson.setLecturer(lecturer1);
        lesson.setGroup(group1);
        lesson.setDay(DayOfWeek.FRIDAY);
        lesson.setLessonTime(lessonTime2);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(
                Arrays.asList("Try to update a lesson: " + lesson + ".", "The object " + lesson + " was updated."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonService.update(lesson);

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenLessonIdIsNegativeWhileDeleteById() {
        int testId = -4;

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to delete an object by id: " + testId + ".",
                "A given id " + testId + " is less than 1 when deleteById."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonService.deleteById(testId);
        } catch (ServiceException serviceException) {
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
    void shouldGenerateLogsWhenRepositoryExceptionWhileDeleteById() {
        int testId = 2;

        doThrow(RepositoryException.class).when(lessonRepository).deleteById(testId);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to delete an object by id: " + testId + ".",
                "There is some error in repositories layer when delete an object by id " + testId + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonService.deleteById(testId);
        } catch (ServiceException serviceException) {
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
    void shouldGenerateLogsWhenDeleteById() {
        int testId = 6;

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to delete an object by id: " + testId + ".",
                "An object was deleted by id " + testId + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonService.deleteById(testId);

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenGroupIdIsNegativeWhileGetGroupWeekLessons() {
        int testId = -9;

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(
                Arrays.asList("Try to get week lessons for a group with id: " + testId + ".",
                        "A group id " + testId + " is not positive when get week lessons for a group."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonService.getGroupWeekLessons(testId);
        } catch (ServiceException serviceException) {
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
    void shouldGenerateLogsWhenResultIsEmptyWhileGetGroupWeekLessons() {
        int testId = 2;
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.WARN));
        List<String> expectedMessages = new ArrayList<>(
                Arrays.asList("Try to get week lessons for a group with id: " + testId + ".",
                        "There are not any week lessons for a group with id " + testId + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonService.getGroupWeekLessons(testId);

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenRepositoryExceptionWhileGetGroupWeekLessons() {
        int testId = 4;

        when(lessonRepository.findByGroupIdAndDay(anyInt(), any(DayOfWeek.class))).thenThrow(RepositoryException.class);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(
                Arrays.asList("Try to get week lessons for a group with id: " + testId + ".",
                        "There is some error in repositories layer when get week lessons for a group with id " + testId + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonService.getGroupWeekLessons(testId);
        } catch (ServiceException serviceException) {
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
    void shouldGenerateLogsWhenGetGroupWeekLessons() {
        int groupId = 1;
        List<Lesson> lessons = new ArrayList<>(Arrays.asList(new Lesson(), new Lesson(), new Lesson()));
        List<String> names = new ArrayList<>(Arrays.asList("Lesson-10", "Lesson-20", "Lesson-33"));
        List<String> audiences = new ArrayList<>(Arrays.asList("201", "202", "203"));
        List<Lecturer> lecturers = new ArrayList<>(Arrays.asList(lecturer2, lecturer1, lecturer2));
        List<Group> groups = new ArrayList<>(Arrays.asList(group1, group2, group1));
        List<DayOfWeek> days = new ArrayList<>(Arrays.asList(DayOfWeek.FRIDAY, DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY));
        List<LessonTime> lessonTimes = new ArrayList<>(Arrays.asList(lessonTime1, lessonTime1, lessonTime2));

        for (int i = 0; i < lessons.size(); i++) {
            int index = i + 1;
            lessons.get(i).setId(index);
            lessons.get(i).setName(names.get(i));
            lessons.get(i).setAudience(audiences.get(i));
            lessons.get(i).setLecturer(lecturers.get(i));
            lessons.get(i).setGroup(groups.get(i));
            lessons.get(i).setDay(days.get(i));
            lessons.get(i).setLessonTime(lessonTimes.get(i));
        }

        when(lessonRepository.findByGroupIdAndDay(groupId, DayOfWeek.FRIDAY)).thenReturn(lessons.subList(0, 1));
        when(lessonRepository.findByGroupIdAndDay(groupId, DayOfWeek.WEDNESDAY)).thenReturn(lessons.subList(2, 3));

        Map<DayOfWeek, List<Lesson>> expectedLessons = new TreeMap<>();
        expectedLessons.put(DayOfWeek.SUNDAY, new ArrayList<>());
        expectedLessons.put(DayOfWeek.MONDAY, new ArrayList<>());
        expectedLessons.put(DayOfWeek.TUESDAY, new ArrayList<>());
        expectedLessons.put(DayOfWeek.WEDNESDAY, new ArrayList<>(Arrays.asList(lessons.get(2))));
        expectedLessons.put(DayOfWeek.THURSDAY, new ArrayList<>());
        expectedLessons.put(DayOfWeek.FRIDAY, new ArrayList<>(Arrays.asList(lessons.get(0))));
        expectedLessons.put(DayOfWeek.SATURDAY, new ArrayList<>());

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to get week lessons for a group with id: " + groupId + ".",
                "When get week lessons for a group with id " + groupId + " the result is: " + expectedLessons + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonService.getGroupWeekLessons(groupId);

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenGroupIdIsNegativeWhileGetGroupMonthLessons() {
        int testId = -12;
        YearMonth testMonth = YearMonth.of(2021, 1);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to get " + testMonth.getMonth() + " month of " + testMonth.getYear()
                        + " year lessons for a group with id: " + testId + ".",
                "A group id " + testId + " is not positive when get " + testMonth.getMonth() + " month of "
                        + testMonth.getYear() + " year lessons for a group."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonService.getGroupMonthLessons(testId, testMonth);
        } catch (ServiceException serviceException) {
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
    void shouldGenerateLogsWhenResultIsEmptyWhileGetGroupMonthLessons() {
        int testId = 5;
        YearMonth testMonth = YearMonth.of(2021, 2);
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.WARN));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to get " + testMonth.getMonth() + " month of " + testMonth.getYear()
                        + " year lessons for a group with id: " + testId + ".",
                "There are not any " + testMonth.getMonth() + " month of " + testMonth.getYear()
                        + " year lessons for a group with id " + testId + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonService.getGroupMonthLessons(testId, testMonth);

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenRepositoryExceptionWhileGetGroupMonthLessons() {
        int testId = 1;
        YearMonth testMonth = YearMonth.of(2020, 12);

        when(lessonRepository.findByGroupIdAndDay(anyInt(), any(DayOfWeek.class))).thenThrow(RepositoryException.class);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to get " + testMonth.getMonth() + " month of " + testMonth.getYear()
                        + " year lessons for a group with id: " + testId + ".",
                "There is some error in repositories layer when get " + testMonth.getMonth() + " month of " + testMonth.getYear()
                        + " year lessons for a group with id " + testId + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonService.getGroupMonthLessons(testId, testMonth);
        } catch (ServiceException serviceException) {
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
    void shouldGenerateLogsWhenGetGroupMonthLessons() {
        int groupId = 1;
        YearMonth month = YearMonth.of(2020, 12);
        List<Lesson> lessons = new ArrayList<>(Arrays.asList(new Lesson(), new Lesson(), new Lesson()));
        List<String> names = new ArrayList<>(Arrays.asList("Lesson-1", "Lesson-2", "Lesson-3"));
        List<String> audiences = new ArrayList<>(Arrays.asList("321", "333", "345"));
        List<Lecturer> lecturers = new ArrayList<>(Arrays.asList(lecturer1, lecturer2, lecturer2));
        List<Group> groups = new ArrayList<>(Arrays.asList(group1, group1, group1));
        List<DayOfWeek> days = new ArrayList<>(
                Arrays.asList(DayOfWeek.THURSDAY, DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY));
        List<LessonTime> lessonTimes = new ArrayList<>(Arrays.asList(lessonTime1, lessonTime1, lessonTime2));

        for (int i = 0; i < lessons.size(); i++) {
            int index = i + 1;
            lessons.get(i).setId(index);
            lessons.get(i).setName(names.get(i));
            lessons.get(i).setAudience(audiences.get(i));
            lessons.get(i).setLecturer(lecturers.get(i));
            lessons.get(i).setGroup(groups.get(i));
            lessons.get(i).setDay(days.get(i));
            lessons.get(i).setLessonTime(lessonTimes.get(i));
        }

        when(lessonRepository.findByGroupIdAndDay(groupId, DayOfWeek.THURSDAY)).thenReturn(lessons.subList(0, 1));
        when(lessonRepository.findByGroupIdAndDay(groupId, DayOfWeek.MONDAY)).thenReturn(lessons.subList(1, 2));
        when(lessonRepository.findByGroupIdAndDay(groupId, DayOfWeek.WEDNESDAY)).thenReturn(lessons.subList(2, 3));

        Map<LocalDate, List<Lesson>> expectedLessons = new TreeMap<>();

        for (int i = 1; i <= month.lengthOfMonth(); i++) {
            LocalDate day = month.atDay(i);

            if (i % 7 == 3) {
                expectedLessons.put(day, new ArrayList<Lesson>(Arrays.asList(lessons.get(0))));
            } else if (i % 7 == 0) {
                expectedLessons.put(day, new ArrayList<Lesson>(Arrays.asList(lessons.get(1))));
            } else if (i % 7 == 2) {
                expectedLessons.put(day, new ArrayList<Lesson>(Arrays.asList(lessons.get(2))));
            } else {
                expectedLessons.put(day, new ArrayList<>());
            }
        }

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to get " + month.getMonth() + " month of " + month.getYear()
                        + " year lessons for a group with id: " + groupId + ".",
                "When get " + month.getMonth() + " month of " + month.getYear() + " year lessons for a group with id "
                        + groupId + " the result is: " + expectedLessons + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonService.getGroupMonthLessons(groupId, month);

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenLecturerIdIsNegativeWhileGetLecturerWeekLessons() {
        int testId = -11;

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(
                Arrays.asList("Try to get week lessons for a lecturer with id: " + testId + ".",
                        "A lecturer id " + testId + " is not positive when get week lessons for a lecturer."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonService.getLecturerWeekLessons(testId);
        } catch (ServiceException serviceException) {
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
    void shouldGenerateLogsWhenResultIsEmptyWhileGetLecturerWeekLessons() {
        int testId = 6;
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.WARN));
        List<String> expectedMessages = new ArrayList<>(
                Arrays.asList("Try to get week lessons for a lecturer with id: " + testId + ".",
                        "There are not any week lessons for a lecturer with id " + testId + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonService.getLecturerWeekLessons(testId);

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenRepositoryExceptionWhileGetLecturerWeekLessons() {
        int testId = 10;

        when(lessonRepository.findByLecturerIdAndDay(anyInt(), any(DayOfWeek.class))).thenThrow(RepositoryException.class);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to get week lessons for a lecturer with id: " + testId + ".",
                "There is some error in repositories layer when get week lessons for a lecturer with id " + testId + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonService.getLecturerWeekLessons(testId);
        } catch (ServiceException serviceException) {
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
    void shouldGenerateLogsWhenGetLecturerWeekLessons() {
        int lecturerId = 2;
        List<Lesson> lessons = new ArrayList<>(Arrays.asList(new Lesson(), new Lesson(), new Lesson()));
        List<String> names = new ArrayList<>(Arrays.asList("Lesson-109", "Lesson-208", "Lesson-337"));
        List<String> audiences = new ArrayList<>(Arrays.asList("203", "204", "201"));
        List<Lecturer> lecturers = new ArrayList<>(Arrays.asList(lecturer2, lecturer2, lecturer2));
        List<Group> groups = new ArrayList<>(Arrays.asList(group1, group2, group1));
        List<DayOfWeek> days = new ArrayList<>(Arrays.asList(DayOfWeek.TUESDAY, DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY));
        List<LessonTime> lessonTimes = new ArrayList<>(Arrays.asList(lessonTime2, lessonTime1, lessonTime2));

        for (int i = 0; i < lessons.size(); i++) {
            int index = i + 1;
            lessons.get(i).setId(index);
            lessons.get(i).setName(names.get(i));
            lessons.get(i).setAudience(audiences.get(i));
            lessons.get(i).setLecturer(lecturers.get(i));
            lessons.get(i).setGroup(groups.get(i));
            lessons.get(i).setDay(days.get(i));
            lessons.get(i).setLessonTime(lessonTimes.get(i));
        }

        when(lessonRepository.findByLecturerIdAndDay(lecturerId, DayOfWeek.TUESDAY)).thenReturn(lessons.subList(0, 1));
        when(lessonRepository.findByLecturerIdAndDay(lecturerId, DayOfWeek.MONDAY)).thenReturn(lessons.subList(1, 2));
        when(lessonRepository.findByLecturerIdAndDay(lecturerId, DayOfWeek.WEDNESDAY)).thenReturn(lessons.subList(2, 3));

        Map<DayOfWeek, List<Lesson>> expectedLessons = new TreeMap<>();
        expectedLessons.put(DayOfWeek.SUNDAY, new ArrayList<>());
        expectedLessons.put(DayOfWeek.MONDAY, new ArrayList<>(Arrays.asList(lessons.get(1))));
        expectedLessons.put(DayOfWeek.TUESDAY, new ArrayList<>(Arrays.asList(lessons.get(0))));
        expectedLessons.put(DayOfWeek.WEDNESDAY, new ArrayList<>(Arrays.asList(lessons.get(2))));
        expectedLessons.put(DayOfWeek.THURSDAY, new ArrayList<>());
        expectedLessons.put(DayOfWeek.FRIDAY, new ArrayList<>());
        expectedLessons.put(DayOfWeek.SATURDAY, new ArrayList<>());

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(
                Arrays.asList("Try to get week lessons for a lecturer with id: " + lecturerId + ".",
                        "When get week lessons for a lecturer with id " + lecturerId + " the result is: "
                                + expectedLessons + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonService.getLecturerWeekLessons(lecturerId);

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenLecturerIdIsNegativeWhileGetLecturerMonthLessons() {
        int testId = -102;
        YearMonth testMonth = YearMonth.of(2021, 4);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to get " + testMonth.getMonth() + " month of " + testMonth.getYear()
                        + " year lessons for a lecturer with id: " + testId + ".",
                "A lecturer id " + testId + " is not positive when get " + testMonth.getMonth() + " month of "
                        + testMonth.getYear() + " year lessons for a lecturer."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonService.getLecturerMonthLessons(testId, testMonth);
        } catch (ServiceException serviceException) {
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
    void shouldGenerateLogsWhenResultIsEmptyWhileGetLecturerMonthLessons() {
        int testId = 3;
        YearMonth testMonth = YearMonth.of(2021, 5);
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.WARN));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to get " + testMonth.getMonth() + " month of " + testMonth.getYear()
                        + " year lessons for a lecturer with id: " + testId + ".",
                "There are not any " + testMonth.getMonth() + " month of " + testMonth.getYear()
                        + " year lessons for a lecturer with id " + testId + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonService.getLecturerMonthLessons(testId, testMonth);

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenRepositoryExceptionWhileGetLecturerMonthLessons() {
        int testId = 6;
        YearMonth testMonth = YearMonth.of(2020, 9);

        when(lessonRepository.findByLecturerIdAndDay(anyInt(), any(DayOfWeek.class))).thenThrow(RepositoryException.class);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to get " + testMonth.getMonth() + " month of " + testMonth.getYear()
                        + " year lessons for a lecturer with id: " + testId + ".",
                "There is some error in repositories layer when get " + testMonth.getMonth() + " month of " + testMonth.getYear()
                        + " year lessons for a lecturer with id " + testId + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonService.getLecturerMonthLessons(testId, testMonth);
        } catch (ServiceException serviceException) {
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
    void shouldGenerateLogsWhenGetLecturerMonthLessons() {
        int lecturerId = 2;
        YearMonth month = YearMonth.of(2020, 10);
        List<Lesson> lessons = new ArrayList<>(Arrays.asList(new Lesson(), new Lesson(), new Lesson()));
        List<String> names = new ArrayList<>(Arrays.asList("Lesson-101", "Lesson-102", "Lesson-103"));
        List<String> audiences = new ArrayList<>(Arrays.asList("101", "102", "103"));
        List<Lecturer> lecturers = new ArrayList<>(Arrays.asList(lecturer2, lecturer2, lecturer2));
        List<Group> groups = new ArrayList<>(Arrays.asList(group2, group2, group1));
        List<DayOfWeek> days = new ArrayList<>(Arrays.asList(DayOfWeek.THURSDAY, DayOfWeek.MONDAY, DayOfWeek.FRIDAY));
        List<LessonTime> lessonTimes = new ArrayList<>(Arrays.asList(lessonTime2, lessonTime1, lessonTime2));

        for (int i = 0; i < lessons.size(); i++) {
            int index = i + 1;
            lessons.get(i).setId(index);
            lessons.get(i).setName(names.get(i));
            lessons.get(i).setAudience(audiences.get(i));
            lessons.get(i).setLecturer(lecturers.get(i));
            lessons.get(i).setGroup(groups.get(i));
            lessons.get(i).setDay(days.get(i));
            lessons.get(i).setLessonTime(lessonTimes.get(i));
        }

        when(lessonRepository.findByLecturerIdAndDay(lecturerId, DayOfWeek.THURSDAY)).thenReturn(lessons.subList(0, 1));
        when(lessonRepository.findByLecturerIdAndDay(lecturerId, DayOfWeek.MONDAY)).thenReturn(lessons.subList(1, 2));
        when(lessonRepository.findByLecturerIdAndDay(lecturerId, DayOfWeek.FRIDAY)).thenReturn(lessons.subList(2, 3));

        Map<LocalDate, List<Lesson>> expectedLessons = new TreeMap<>();

        for (int i = 1; i <= month.lengthOfMonth(); i++) {
            if (i % 7 == 1) {
                expectedLessons.put(month.atDay(i), new ArrayList<Lesson>(Arrays.asList(lessons.get(0))));
            } else if (i % 7 == 5) {
                expectedLessons.put(month.atDay(i), new ArrayList<Lesson>(Arrays.asList(lessons.get(1))));
            } else if (i % 7 == 2) {
                expectedLessons.put(month.atDay(i), new ArrayList<Lesson>(Arrays.asList(lessons.get(2))));
            } else {
                expectedLessons.put(month.atDay(i), new ArrayList<>());
            }
        }

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to get " + month.getMonth() + " month of " + month.getYear()
                        + " year lessons for a lecturer with id: " + lecturerId + ".",
                "When get " + month.getMonth() + " month of " + month.getYear()
                        + " year lessons for a lecturer with id " + lecturerId + " the result is: " + expectedLessons
                        + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonService.getLecturerMonthLessons(lecturerId, month);

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }
}