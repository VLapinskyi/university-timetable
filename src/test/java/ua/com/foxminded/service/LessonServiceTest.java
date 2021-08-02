package ua.com.foxminded.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doAnswer;
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
import java.util.TreeMap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ua.com.foxminded.dao.LessonDAO;
import ua.com.foxminded.dao.exceptions.DAOException;
import ua.com.foxminded.domain.Faculty;
import ua.com.foxminded.domain.Gender;
import ua.com.foxminded.domain.Group;
import ua.com.foxminded.domain.Lecturer;
import ua.com.foxminded.domain.Lesson;
import ua.com.foxminded.domain.LessonTime;
import ua.com.foxminded.service.exceptions.ServiceException;
import ua.com.foxminded.settings.SpringConfiguration;
import ua.com.foxminded.settings.TestAppender;

@ContextConfiguration(classes = {SpringConfiguration.class})
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
class LessonServiceTest {
    private TestAppender testAppender = new TestAppender();
    
    @Autowired
    private LessonService lessonService;   

    @Mock
    private LessonDAO lessonDAO;
    
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
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(lessonService, "lessonDAO", lessonDAO);
        
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
        testAppender.cleanEventList();
    }

    @Test
    void shouldCreateLesson() {
        int newId = 2;
        Lesson savedLesson = new Lesson();
        savedLesson.setId(1);
        savedLesson.setName("Lesson-1");
        savedLesson.setAudience("101");
        savedLesson.setLecturer(lecturer1);
        savedLesson.setGroup(group2);
        savedLesson.setDay(DayOfWeek.MONDAY);
        savedLesson.setLessonTime(lessonTime1);

        Lesson creatingLesson = new Lesson();
        creatingLesson.setName("Lesson-2");
        creatingLesson.setAudience("103");
        creatingLesson.setLecturer(lecturer2);
        creatingLesson.setGroup(group1);
        creatingLesson.setDay(DayOfWeek.WEDNESDAY);
        creatingLesson.setLessonTime(lessonTime2);
        
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                Lesson lesson = (Lesson) invocation.getArguments()[0];
                lesson.setId(newId);
                return null;
            }
        }).when(lessonDAO).create(creatingLesson);
        
        when(lessonDAO.findAll()).thenReturn(new ArrayList<Lesson>(Arrays.asList(savedLesson, creatingLesson)));
        lessonService.create(creatingLesson);
        verify(lessonDAO).create(creatingLesson);
        verify(lessonDAO).setLessonLecturer(creatingLesson.getLecturer().getId(), creatingLesson.getId());
        verify(lessonDAO).setLessonGroup(creatingLesson.getGroup().getId(), creatingLesson.getId());
        verify(lessonDAO).setLessonTime(creatingLesson.getLessonTime().getId(), creatingLesson.getId());
    }

    @Test
    void shouldGetAllLessons() {
        List<Integer> lessonIndexes = new ArrayList<>(Arrays.asList(
                1, 2, 3));
        List<Lesson> lessons = new ArrayList<>(Arrays.asList(
                new Lesson(), new Lesson(), new Lesson()));
        List<String> names = new ArrayList<>(Arrays.asList(
                "Lesson-1", "Lesson-2", "Lesson-3"));
        List<String> audiences = new ArrayList<>(Arrays.asList(
                "101", "102", "103"));
        List<Lecturer> lecturers = new ArrayList<>(Arrays.asList(
                lecturer1, lecturer2, lecturer2));
        List<Group> groups = new ArrayList<>(Arrays.asList(
                group2, group1, group2));
        List<DayOfWeek> days = new ArrayList<>(Arrays.asList(
                DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY));
        List<LessonTime> lessonTimes = new ArrayList<>(Arrays.asList(
                lessonTime2, lessonTime1, lessonTime1));
        for(int i = 0; i < lessons.size(); i++) {
            lessons.get(i).setId(lessonIndexes.get(i));
            lessons.get(i).setName(names.get(i));
            lessons.get(i).setAudience(audiences.get(i));
            lessons.get(i).setLecturer(lecturers.get(i));
            lessons.get(i).setGroup(groups.get(i));
            lessons.get(i).setDay(days.get(i));
            lessons.get(i).setLessonTime(lessonTimes.get(i));
        }

        when(lessonDAO.findAll()).thenReturn(lessons);
        lessonService.getAll();
        verify(lessonDAO).findAll();

        verify(lessonDAO, times(3)).getLessonGroup(anyInt());
        verify(lessonDAO, times(3)).getLessonLecturer(anyInt());
        verify(lessonDAO, times(3)).getLessonTime(anyInt());
        for (Integer index : lessonIndexes) {
            verify(lessonDAO).getLessonGroup(index);
            verify(lessonDAO).getLessonLecturer(index);
            verify(lessonDAO).getLessonTime(index);
        }
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
        when(lessonDAO.findById(lessonId)).thenReturn(lesson);
        lessonService.getById(lessonId);
        verify(lessonDAO).findById(lessonId);
        verify(lessonDAO).getLessonGroup(lessonId);
        verify(lessonDAO).getLessonLecturer(lessonId);
        verify(lessonDAO).getLessonTime(lessonId);
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

        verify(lessonDAO).update(lessonId, lesson);
        verify(lessonDAO).setLessonLecturer(lesson.getLecturer().getId(), lessonId);
        verify(lessonDAO).setLessonGroup(lesson.getGroup().getId(), lessonId);
        verify(lessonDAO).setLessonTime(lesson.getLessonTime().getId(), lessonId);
    }
    
    @Test
    void shouldDeleteLessonById() {
        int lessonId = 456;
        lessonService.deleteById(lessonId);
        verify(lessonDAO).deleteById(lessonId);
    }

    @Test
    void shouldGetWeekLessonsForGroup() {
        int groupId = 2;
        Lesson lesson1 = new Lesson();
        lesson1.setId(1);
        Lesson lesson2 = new Lesson();
        lesson2.setId(2);
        when(lessonDAO.getGroupDayLessons(groupId, DayOfWeek.TUESDAY)).thenReturn(new ArrayList<Lesson>(Arrays.asList(lesson1, lesson2)));
        lessonService.getGroupWeekLessons(groupId);
        for (int i = 1; i <= DayOfWeek.values().length; i++) {
            verify(lessonDAO).getGroupDayLessons(groupId, DayOfWeek.of(i));
        }
        verify(lessonDAO).getLessonGroup(lesson1.getId());
        verify(lessonDAO).getLessonGroup(lesson2.getId());
        verify(lessonDAO).getLessonLecturer(lesson1.getId());
        verify(lessonDAO).getLessonLecturer(lesson2.getId());
        verify(lessonDAO).getLessonTime(lesson1.getId());
        verify(lessonDAO).getLessonTime(lesson2.getId());
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
        verify(lessonDAO, times(monthLength)).getGroupDayLessons(numberCaptor.capture(), dayCaptor.capture());

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
        when(lessonDAO.getLecturerDayLessons(lecturerId, DayOfWeek.FRIDAY)).thenReturn(new ArrayList<Lesson>(Arrays.asList(lesson1, lesson2)));
        lessonService.getLecturerWeekLessons(lecturerId);
        for (int i = 1; i <= DayOfWeek.values().length; i++) {
            verify(lessonDAO).getLecturerDayLessons(lecturerId, DayOfWeek.of(i));
        }
        verify(lessonDAO).getLessonGroup(lesson1.getId());
        verify(lessonDAO).getLessonGroup(lesson2.getId());
        verify(lessonDAO).getLessonLecturer(lesson1.getId());
        verify(lessonDAO).getLessonLecturer(lesson2.getId());
        verify(lessonDAO).getLessonTime(lesson1.getId());
        verify(lessonDAO).getLessonTime(lesson2.getId());
    }

    @Test
    void shouldGetMonthLessonsForLecturer() {
        int lecturerId = 3;
        YearMonth month = YearMonth.of(2020, Month.DECEMBER);
        int monthLength = 31;
        List<DayOfWeek> expectedDays = new ArrayList<>();
        expectedDays.addAll(Arrays.asList(DayOfWeek.of(2), DayOfWeek.of(3), DayOfWeek.of(4),
                DayOfWeek.of(5), DayOfWeek.of(6), DayOfWeek.of(7)));
        expectedDays.addAll(Arrays.asList(DayOfWeek.values()));
        expectedDays.addAll(Arrays.asList(DayOfWeek.values()));
        expectedDays.addAll(Arrays.asList(DayOfWeek.values()));
        expectedDays.addAll(Arrays.asList(DayOfWeek.of(1), DayOfWeek.of(2), DayOfWeek.of(3),
                DayOfWeek.of(4)));
        lessonService.getLecturerMonthLessons(lecturerId, month);
        verify(lessonDAO, times(monthLength)).getLecturerDayLessons(numberCaptor.capture(), dayCaptor.capture());

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
    void shouldThrowServiceExceptionWhenDAOExceptionWhileCreate() {
        Lesson lesson = new Lesson();
        lesson.setName("Lesson-18");
        lesson.setAudience("106");
        lesson.setLecturer(lecturer1);
        lesson.setGroup(group1);
        lesson.setDay(DayOfWeek.THURSDAY);
        lesson.setLessonTime(lessonTime2);
        
        doThrow(DAOException.class).when(lessonDAO).create(lesson);
        
        assertThrows(ServiceException.class, () -> lessonService.create(lesson));
    }
    
    @Test
    void shouldThrowServiceExceptionWhenDAOExceptionWhileGetAll() {
        when(lessonDAO.findAll()).thenThrow(DAOException.class);
        assertThrows(ServiceException.class, () -> lessonService.getAll());
    }
    
    @Test
    void shouldThrowServiceExceptionWhenLessonIdIsZeroWhileGetById() {
        int testId = 0;
        assertThrows(ServiceException.class, () -> lessonService.getById(testId));
    }
    
    @Test
    void shouldThrowServiceExceptionWhenDAOExceptionWhileGetById() {
        int testId = 4;
        when(lessonDAO.findById(testId)).thenThrow(DAOException.class);
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
    void shouldThrowServiceExceptionWhenDAOExceptionWhileUpdate() {
        Lesson lesson = new Lesson();
        lesson.setId(45);
        lesson.setName("Lesson-1");
        lesson.setAudience(" 114");
        lesson.setLecturer(lecturer2);
        lesson.setGroup(group1);
        lesson.setDay(DayOfWeek.MONDAY);
        lesson.setLessonTime(lessonTime2);
        
        doThrow(DAOException.class).when(lessonDAO).update(lesson.getId(), lesson);
        
        assertThrows(ServiceException.class, () -> lessonService.update(lesson));
    }
    
    @Test
    void shouldThrowServiceExceptionWhenLessonIdIsZeroWhileDeleteById() {
        int testId = 0;
        assertThrows(ServiceException.class, () -> lessonService.deleteById(testId));
    }
    
    @Test
    void shouldThrowServiceExceptioinWhenDAOExceptionWhileDeleteById() {
        int testId = 74;
        doThrow(DAOException.class).when(lessonDAO).deleteById(testId);
        assertThrows(ServiceException.class, () -> lessonService.deleteById(testId));
    }
    
    @Test
    void shouldThrowServiceExceptionWhenGroupIdIsZeroWhileGetGroupWeekLessons() {
        int testId = 0;
        assertThrows(ServiceException.class, () -> lessonService.getGroupWeekLessons(testId));
    }
    
    @Test
    void shouldThrowServiceExceptioinWhenDAOExceptionWhileGetGroupWeekLessons() {
        int testId = 74;
        DayOfWeek testDay = DayOfWeek.MONDAY;
        when(lessonDAO.getGroupDayLessons(testId, testDay)).thenThrow(DAOException.class);
        assertThrows(ServiceException.class, () -> lessonService.getGroupWeekLessons(testId));
    }
    
    @Test
    void shouldThrowServiceExceptionWhenGroupIdIsZeroWhileGetGroupMonthLessons() {
        int testId = 0;
        YearMonth testMonth = YearMonth.of(2021, 5);
        assertThrows(ServiceException.class, () -> lessonService.getGroupMonthLessons(testId, testMonth));
    }
    
    @Test
    void shouldThrowServiceExceptionWhenDAOExceptionWhileGetGroupMonthLessons() {
        int testId = 14;
        YearMonth testMonth = YearMonth.of(2021, 4);
        DayOfWeek  testDay = DayOfWeek.THURSDAY;
        when(lessonDAO.getGroupDayLessons(testId, testDay)).thenThrow(DAOException.class);
        assertThrows(ServiceException.class, () -> lessonService.getGroupMonthLessons(testId, testMonth));
    }
    
    @Test
    void shouldThrowServiceExceptionWhenLecturerIdIsZeroWhileGetLecturerWeekLessons() {
        int testId = 0;
        assertThrows(ServiceException.class, () -> lessonService.getLecturerWeekLessons(testId));
    }
    
    @Test
    void shouldThrowServiceExceptioinWhenDAOExceptionWhileGetLecturerWeekLessons() {
        int testId = 12;
        DayOfWeek testDay = DayOfWeek.THURSDAY;
        when(lessonDAO.getLecturerDayLessons(testId, testDay)).thenThrow(DAOException.class);
        assertThrows(ServiceException.class, () -> lessonService.getLecturerWeekLessons(testId));
    }
    
    @Test
    void shouldThrowServiceExceptionWhenLecturerIdIsZeroWhileGetLecturerMonthLessons() {
        int testId = 0;
        YearMonth testMonth = YearMonth.of(2021, 3);
        assertThrows(ServiceException.class, () -> lessonService.getLecturerMonthLessons(testId, testMonth));
    }
    
    @Test
    void shouldThrowServiceExceptionWhenDAOExceptionWhileGetLecturerMonthLessons() {
        int testId = 19;
        YearMonth testMonth = YearMonth.of(2021, 2);
        DayOfWeek  testDay = DayOfWeek.FRIDAY;
        when(lessonDAO.getLecturerDayLessons(testId, testDay)).thenThrow(DAOException.class);
        assertThrows(ServiceException.class, () -> lessonService.getLecturerMonthLessons(testId, testMonth));
    }

    @Test
    void shouldGenerateLogsWhenLessonIsNullWhileCreate() {
        Lesson lesson = null;
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to create a new lesson: " + lesson + ".",
                "A lesson " + lesson + " can't be null when create."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonService.create(lesson);
        } catch (ServiceException serviceException) {
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
        
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to create a new lesson: " + lesson + ".",
                "A lesson " + lesson + " has wrong id " + testId + " which is not equal zero when create."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonService.create(lesson);
        } catch (ServiceException serviceException) {
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
    void shouldGenerateLogsWhenLessonIsInvalidWhileCreate() {
        Lesson lesson = new Lesson();
        lesson.setName(" Lesson");
        lesson.setAudience("954");
        lesson.setLecturer(lecturer2);
        lesson.setGroup(group1);
        lesson.setDay(DayOfWeek.THURSDAY);
        lesson.setLessonTime(lessonTime1);
        
        String violationMessage = "Lesson name must have at least two symbols and start with non-white space";
        
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to create a new lesson: " + lesson + ".",
                "The lesson " + lesson + " is not valid when create. There are errors: " + violationMessage + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonService.create(lesson);
        } catch (ServiceException serviceException) {
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
    void shouldGenerateLogsWhenDAOExceptionWhileCreate() {
        Lesson lesson = new Lesson();
        lesson.setName("Lesson");
        lesson.setAudience("103");
        lesson.setLecturer(lecturer1);
        lesson.setGroup(group1);
        lesson.setDay(DayOfWeek.MONDAY);
        lesson.setLessonTime(lessonTime1);
        
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to create a new lesson: " + lesson + ".",
                "There is some error in dao layer when create an object " + lesson + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }
        
        doThrow(DAOException.class).when(lessonDAO).create(lesson);

        try {
            lessonService.create(lesson);
        } catch (ServiceException serviceException) {
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
    void shouldGenerateLogsWhenCreate() {
        Lesson lesson = new Lesson();
        lesson.setName("Lesson1");
        lesson.setAudience("106");
        lesson.setLecturer(lecturer2);
        lesson.setGroup(group2);
        lesson.setDay(DayOfWeek.TUESDAY);
        lesson.setLessonTime(lessonTime2);
        
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to create a new lesson: " + lesson + ".",
                "The object " + lesson + " was created."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

            lessonService.create(lesson);

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }    
    }
    
    @Test
    void shouldGenerateLogsWhenResultIsEmptyWhileGetAll() {
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.WARN));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to get all objects.",
                "There are not any objects in the result when getAll."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonService.getAll();

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }
    
    @Test
    void shouldGenerateLogsWhenDAOExceptionWhileGetAll() {
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to get all objects.",
                "There is some error in dao layer when getAll."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        when(lessonDAO.findAll()).thenThrow(DAOException.class);

        try {
            lessonService.getAll();
        } catch (ServiceException serviceException) {
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
    void shouldGenerateLogsWhenGetAll() {
        List<Lesson> expectedLessons = new ArrayList<>(Arrays.asList(
                new Lesson(), new Lesson(), new Lesson()));
        List<String> names = new ArrayList<>(Arrays.asList(
                "Lesson-1", "Lesson-2", "Lesson-3"));
        List<String> audiences = new ArrayList<>(Arrays.asList(
                "101", "102", "103"));
        List<Lecturer> lecturers = new ArrayList<>(Arrays.asList(
                lecturer1, lecturer2, lecturer1));
        List<Group> groups = new ArrayList<>(Arrays.asList(
                group1, group1, group2));
        List<DayOfWeek> days = new ArrayList<>(Arrays.asList(
                DayOfWeek.MONDAY, DayOfWeek.THURSDAY, DayOfWeek.WEDNESDAY));
        List<LessonTime> lessonTimes = new ArrayList<>(Arrays.asList(
                lessonTime2, lessonTime2, lessonTime1));
        
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

        when(lessonDAO.findAll()).thenReturn(expectedLessons);
        when(lessonDAO.getLessonGroup(1)).thenReturn(group1);
        when(lessonDAO.getLessonGroup(2)).thenReturn(group1);
        when(lessonDAO.getLessonGroup(3)).thenReturn(group2);
        
        when(lessonDAO.getLessonLecturer(1)).thenReturn(lecturer1);
        when(lessonDAO.getLessonLecturer(2)).thenReturn(lecturer2);
        when(lessonDAO.getLessonLecturer(3)).thenReturn(lecturer1);
        
        when(lessonDAO.getLessonTime(1)).thenReturn(lessonTime2);
        when(lessonDAO.getLessonTime(2)).thenReturn(lessonTime2);
        when(lessonDAO.getLessonTime(3)).thenReturn(lessonTime1);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to get all objects.",
                "The result is: " + expectedLessons + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonService.getAll();

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }
    
    @Test
    void shouldGenerateLogsWhenLessonIdIsNegativeWhileGetById() {
        int negativeId = -4;

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to get an object by id: " + negativeId  + ".",
                "A given id " + negativeId + " is less than 1 when getById."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonService.getById(negativeId);
        } catch (ServiceException serviceException) {
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
    void shouldGenerateLogsWhenDAOExceptionWhileGetById() {
        int testId = 1;

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to get an object by id: " + testId  + ".",
                "There is some error in dao layer when get object by id " + testId + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        when(lessonDAO.findById(testId)).thenThrow(DAOException.class);

        try {
            lessonService.getById(testId);
        } catch (ServiceException serviceException) {
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
        
        when(lessonDAO.findById(testId)).thenReturn(lesson);
        when(lessonDAO.getLessonGroup(testId)).thenReturn(group2);
        when(lessonDAO.getLessonLecturer(testId)).thenReturn(lecturer1);
        when(lessonDAO.getLessonTime(testId)).thenReturn(lessonTime1);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to get an object by id: " + testId  + ".",
                "The result object with id " + testId + " is " + lesson + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonService.getById(testId);

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

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

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to update a lesson: " + lesson + ".",
                "An updated lesson " + lesson + " has wrong id " + lesson.getId() + " which is not positive."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonService.update(lesson);
        } catch (ServiceException serviceException) {
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
    void shouldGenerateLogsWhenLessonIsInvalidWhileUpdate() {
        Lesson lesson = new Lesson();
        lesson.setId(5);
        lesson.setName("Lesson");
        lesson.setAudience("456");
        lesson.setLecturer(lecturer1);
        lesson.setDay(DayOfWeek.FRIDAY);
        lesson.setLessonTime(lessonTime2);
        
        String violationMessage = "Lesson group can't be null";

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to update a lesson: " + lesson + ".",
                "The lesson " + lesson + " is not valid when update. There are errors: " + violationMessage + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonService.update(lesson);
        } catch (ServiceException serviceException) {
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
    void shouldGenerateLogsWhenDAOExceptionWhileUpdate() {
        Lesson lesson = new Lesson();
        lesson.setId(9);
        lesson.setName("Lesson-9");
        lesson.setAudience("951");
        lesson.setLecturer(lecturer2);
        lesson.setGroup(group1);
        lesson.setDay(DayOfWeek.MONDAY);
        lesson.setLessonTime(lessonTime1);

        doThrow(DAOException.class).when(lessonDAO).update(lesson.getId(), lesson);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to update a lesson: " + lesson + ".",
                "There is some error in dao layer when update an object " + lesson + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonService.update(lesson);
        } catch (ServiceException serviceException) {
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
    void shouldGenerateLogsWhenUpdate() {
        Lesson lesson = new Lesson();
        lesson.setId(4);
        lesson.setName("Lesson-4");
        lesson.setAudience("410");
        lesson.setLecturer(lecturer1);
        lesson.setGroup(group1);
        lesson.setDay(DayOfWeek.FRIDAY);
        lesson.setLessonTime(lessonTime2);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to update a lesson: " + lesson + ".",
                "The object " + lesson + " was updated."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonService.update(lesson);

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }
    
    @Test
    void shouldGenerateLogsWhenLessonIdIsNegativeWhileDeleteById() {
        int testId = -4;

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to delete an object by id: " + testId + ".",
                "A given id " + testId + " is less than 1 when deleteById."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonService.deleteById(testId);
        } catch (ServiceException serviceException) {
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
    void shouldGenerateLogsWhenDAOExceptionWhileDeleteById() {
        int testId = 2;

        doThrow(DAOException.class).when(lessonDAO).deleteById(testId);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to delete an object by id: " + testId + ".",
                "There is some error in dao layer when delete an object by id " + testId + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonService.deleteById(testId);
        } catch (ServiceException serviceException) {
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
        int testId = 6;

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to delete an object by id: " + testId + ".",
                "An object was deleted by id " + testId + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonService.deleteById(testId);

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }
    
    @Test
    void shouldGenerateLogsWhenGroupIdIsNegativeWhileGetGroupWeekLessons() {
        int testId = -9;

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to get week lessons for a group with id: " + testId + ".",
                "A group id " + testId + " is not positive when get week lessons for a group."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonService.getGroupWeekLessons(testId);
        } catch (ServiceException serviceException) {
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
    void shouldGenerateLogsWhenResultIsEmptyWhileGetGroupWeekLessons() {
        int testId = 2;
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.WARN));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to get week lessons for a group with id: " + testId + ".",
                "There are not any week lessons for a group with id " + testId + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonService.getGroupWeekLessons(testId);

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }
    
    @Test
    void shouldGenerateLogsWhenDAOExceptionWhileGetGroupWeekLessons() {
        int testId = 4;

        when(lessonDAO.getGroupDayLessons(anyInt(),any(DayOfWeek.class))).thenThrow(DAOException.class);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to get week lessons for a group with id: " + testId + ".",
                "There is some error in dao layer when get week lessons for a group with id " + testId + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonService.getGroupWeekLessons(testId);
        } catch (ServiceException serviceException) {
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
    void shouldGenerateLogsWhenGetGroupWeekLessons() {
        int groupId = 1;
        List<Lesson> lessons = new ArrayList<>(Arrays.asList(
                new Lesson(), new Lesson(), new Lesson()));
        List<String> names = new ArrayList<>(Arrays.asList(
                "Lesson-10", "Lesson-20", "Lesson-33"));
        List<String> audiences = new ArrayList<>(Arrays.asList(
                "201", "202", "203"));
        List<Lecturer> lecturers = new ArrayList<>(Arrays.asList(
                lecturer2, lecturer1, lecturer2));
        List<Group> groups = new ArrayList<>(Arrays.asList(
                group1, group2, group1));
        List<DayOfWeek> days = new ArrayList<>(Arrays.asList(
                DayOfWeek.FRIDAY, DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY));
        List<LessonTime> lessonTimes = new ArrayList<>(Arrays.asList(
                lessonTime1, lessonTime1, lessonTime2));
        
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

        when(lessonDAO.getGroupDayLessons(groupId, DayOfWeek.FRIDAY)).thenReturn(lessons.subList(0, 1));
        when(lessonDAO.getGroupDayLessons(groupId, DayOfWeek.WEDNESDAY)).thenReturn(lessons.subList(2, 3));
        
        when(lessonDAO.getLessonGroup(lessons.get(0).getId())).thenReturn(lessons.get(0).getGroup());
        when(lessonDAO.getLessonLecturer(lessons.get(0).getId())).thenReturn(lessons.get(0).getLecturer());
        when(lessonDAO.getLessonTime(lessons.get(0).getId())).thenReturn(lessons.get(0).getLessonTime());
        
        when(lessonDAO.getLessonGroup(lessons.get(2).getId())).thenReturn(lessons.get(2).getGroup());
        when(lessonDAO.getLessonLecturer(lessons.get(2).getId())).thenReturn(lessons.get(2).getLecturer());
        when(lessonDAO.getLessonTime(lessons.get(2).getId())).thenReturn(lessons.get(2).getLessonTime());
        
        Map<DayOfWeek, List<Lesson>> expectedLessons = new TreeMap<>();
        expectedLessons.put(DayOfWeek.SUNDAY, new ArrayList<>());
        expectedLessons.put(DayOfWeek.MONDAY, new ArrayList<>());
        expectedLessons.put(DayOfWeek.TUESDAY, new ArrayList<>());
        expectedLessons.put(DayOfWeek.WEDNESDAY, new ArrayList<>(Arrays.asList(lessons.get(2))));
        expectedLessons.put(DayOfWeek.THURSDAY, new ArrayList<>());
        expectedLessons.put(DayOfWeek.FRIDAY, new ArrayList<>(Arrays.asList(lessons.get(0))));
        expectedLessons.put(DayOfWeek.SATURDAY, new ArrayList<>());

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to get week lessons for a group with id: " + groupId + ".",
                "When get week lessons for a group with id " + groupId + " the result is: " + expectedLessons + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonService.getGroupWeekLessons(groupId);

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

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

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to get " + testMonth.getMonth() + " month of " + testMonth.getYear() + " year lessons for a group with id: " + testId + ".",
                "A group id " + testId + " is not positive when get " + testMonth.getMonth() + " month of " + testMonth.getYear() + " year lessons for a group."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonService.getGroupMonthLessons(testId, testMonth);
        } catch (ServiceException serviceException) {
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
    void shouldGenerateLogsWhenResultIsEmptyWhileGetGroupMonthLessons() {
        int testId = 5;
        YearMonth testMonth = YearMonth.of(2021, 2);
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.WARN));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to get " + testMonth.getMonth() + " month of " + testMonth.getYear() + " year lessons for a group with id: " + testId + ".",
                "There are not any " + testMonth.getMonth() + " month of " + testMonth.getYear() + " year lessons for a group with id " + testId + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonService.getGroupMonthLessons(testId, testMonth);

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }
    
    @Test
    void shouldGenerateLogsWhenDAOExceptionWhileGetGroupMonthLessons() {
        int testId = 1;
        YearMonth testMonth = YearMonth.of(2020, 12);

        when(lessonDAO.getGroupDayLessons(anyInt(),any(DayOfWeek.class))).thenThrow(DAOException.class);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to get " + testMonth.getMonth() + " month of " + testMonth.getYear() + " year lessons for a group with id: " + testId + ".",
                "There is some error in dao layer when get " + testMonth.getMonth() + " month of " + testMonth.getYear() + " year lessons for a group with id " + testId + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonService.getGroupMonthLessons(testId, testMonth);
        } catch (ServiceException serviceException) {
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
    void shouldGenerateLogsWhenGetGroupMonthLessons() {
        int groupId = 1;
        YearMonth month = YearMonth.of(2020, 12);
        List<Lesson> lessons = new ArrayList<>(Arrays.asList(
                new Lesson(), new Lesson(), new Lesson()));
        List<String> names = new ArrayList<>(Arrays.asList(
                "Lesson-1", "Lesson-2", "Lesson-3"));
        List<String> audiences = new ArrayList<>(Arrays.asList(
                "321", "333", "345"));
        List<Lecturer> lecturers = new ArrayList<>(Arrays.asList(
                lecturer1, lecturer2, lecturer2));
        List<Group> groups = new ArrayList<>(Arrays.asList(
                group1, group1, group1));
        List<DayOfWeek> days = new ArrayList<>(Arrays.asList(
                DayOfWeek.THURSDAY, DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY));
        List<LessonTime> lessonTimes = new ArrayList<>(Arrays.asList(
                lessonTime1, lessonTime1, lessonTime2));
        
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

        when(lessonDAO.getGroupDayLessons(groupId, DayOfWeek.THURSDAY)).thenReturn(lessons.subList(0, 1));
        when(lessonDAO.getGroupDayLessons(groupId, DayOfWeek.MONDAY)).thenReturn(lessons.subList(1, 2));
        when(lessonDAO.getGroupDayLessons(groupId, DayOfWeek.WEDNESDAY)).thenReturn(lessons.subList(2, 3));
        
        when(lessonDAO.getLessonGroup(lessons.get(0).getId())).thenReturn(lessons.get(0).getGroup());
        when(lessonDAO.getLessonLecturer(lessons.get(0).getId())).thenReturn(lessons.get(0).getLecturer());
        when(lessonDAO.getLessonTime(lessons.get(0).getId())).thenReturn(lessons.get(0).getLessonTime());
        
        when(lessonDAO.getLessonGroup(lessons.get(1).getId())).thenReturn(lessons.get(1).getGroup());
        when(lessonDAO.getLessonLecturer(lessons.get(1).getId())).thenReturn(lessons.get(1).getLecturer());
        when(lessonDAO.getLessonTime(lessons.get(1).getId())).thenReturn(lessons.get(1).getLessonTime());
        
        when(lessonDAO.getLessonGroup(lessons.get(2).getId())).thenReturn(lessons.get(2).getGroup());
        when(lessonDAO.getLessonLecturer(lessons.get(2).getId())).thenReturn(lessons.get(2).getLecturer());
        when(lessonDAO.getLessonTime(lessons.get(2).getId())).thenReturn(lessons.get(2).getLessonTime());
        
        Map<LocalDate, List<Lesson>> expectedLessons = new TreeMap<>();
        
        for (int i = 1; i <= month.lengthOfMonth(); i++) {
            LocalDate day = month.atDay(i);
            
            if(i % 7 == 3) {
                expectedLessons.put(day, new ArrayList<Lesson>(Arrays.asList(lessons.get(0))));
            } else if(i % 7 == 0) {
                expectedLessons.put(day, new ArrayList<Lesson>(Arrays.asList(lessons.get(1))));
            } else if(i % 7 == 2) {
                expectedLessons.put(day, new ArrayList<Lesson>(Arrays.asList(lessons.get(2))));
            } else {
                expectedLessons.put(day, new ArrayList<>());
            }
        }

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to get " + month.getMonth() + " month of " + month.getYear() + " year lessons for a group with id: " + groupId + ".",
                "When get " + month.getMonth() + " month of " + month.getYear() + " year lessons for a group with id " + groupId + " the result is: " + expectedLessons + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonService.getGroupMonthLessons(groupId, month);

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }
    
    @Test
    void shouldGenerateLogsWhenLecturerIdIsNegativeWhileGetLecturerWeekLessons() {
        int testId = -11;

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to get week lessons for a lecturer with id: " + testId + ".",
                "A lecturer id " + testId + " is not positive when get week lessons for a lecturer."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonService.getLecturerWeekLessons(testId);
        } catch (ServiceException serviceException) {
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
    void shouldGenerateLogsWhenResultIsEmptyWhileGetLecturerWeekLessons() {
        int testId = 6;
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.WARN));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to get week lessons for a lecturer with id: " + testId + ".",
                "There are not any week lessons for a lecturer with id " + testId + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonService.getLecturerWeekLessons(testId);

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }
    
    @Test
    void shouldGenerateLogsWhenDAOExceptionWhileGetLecturerWeekLessons() {
        int testId = 10;

        when(lessonDAO.getLecturerDayLessons(anyInt(),any(DayOfWeek.class))).thenThrow(DAOException.class);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to get week lessons for a lecturer with id: " + testId + ".",
                "There is some error in dao layer when get week lessons for a lecturer with id " + testId + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonService.getLecturerWeekLessons(testId);
        } catch (ServiceException serviceException) {
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
    void shouldGenerateLogsWhenGetLecturerWeekLessons() {
        int lecturerId = 2;
        List<Lesson> lessons = new ArrayList<>(Arrays.asList(
                new Lesson(), new Lesson(), new Lesson()));
        List<String> names = new ArrayList<>(Arrays.asList(
                "Lesson-109", "Lesson-208", "Lesson-337"));
        List<String> audiences = new ArrayList<>(Arrays.asList(
                "203", "204", "201"));
        List<Lecturer> lecturers = new ArrayList<>(Arrays.asList(
                lecturer2, lecturer2, lecturer2));
        List<Group> groups = new ArrayList<>(Arrays.asList(
                group1, group2, group1));
        List<DayOfWeek> days = new ArrayList<>(Arrays.asList(
                DayOfWeek.TUESDAY, DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY));
        List<LessonTime> lessonTimes = new ArrayList<>(Arrays.asList(
                lessonTime2, lessonTime1, lessonTime2));
        
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

        when(lessonDAO.getLecturerDayLessons(lecturerId, DayOfWeek.TUESDAY)).thenReturn(lessons.subList(0, 1));
        when(lessonDAO.getLecturerDayLessons(lecturerId, DayOfWeek.MONDAY)).thenReturn(lessons.subList(1, 2));
        when(lessonDAO.getLecturerDayLessons(lecturerId, DayOfWeek.WEDNESDAY)).thenReturn(lessons.subList(2, 3));
        
        when(lessonDAO.getLessonGroup(lessons.get(0).getId())).thenReturn(lessons.get(0).getGroup());
        when(lessonDAO.getLessonLecturer(lessons.get(0).getId())).thenReturn(lessons.get(0).getLecturer());
        when(lessonDAO.getLessonTime(lessons.get(0).getId())).thenReturn(lessons.get(0).getLessonTime());
        
        when(lessonDAO.getLessonGroup(lessons.get(1).getId())).thenReturn(lessons.get(1).getGroup());
        when(lessonDAO.getLessonLecturer(lessons.get(1).getId())).thenReturn(lessons.get(1).getLecturer());
        when(lessonDAO.getLessonTime(lessons.get(1).getId())).thenReturn(lessons.get(1).getLessonTime());
        
        when(lessonDAO.getLessonGroup(lessons.get(2).getId())).thenReturn(lessons.get(2).getGroup());
        when(lessonDAO.getLessonLecturer(lessons.get(2).getId())).thenReturn(lessons.get(2).getLecturer());
        when(lessonDAO.getLessonTime(lessons.get(2).getId())).thenReturn(lessons.get(2).getLessonTime());
        
        Map<DayOfWeek, List<Lesson>> expectedLessons = new TreeMap<>();
        expectedLessons.put(DayOfWeek.SUNDAY, new ArrayList<>());
        expectedLessons.put(DayOfWeek.MONDAY, new ArrayList<>(Arrays.asList(lessons.get(1))));
        expectedLessons.put(DayOfWeek.TUESDAY, new ArrayList<>(Arrays.asList(lessons.get(0))));
        expectedLessons.put(DayOfWeek.WEDNESDAY, new ArrayList<>(Arrays.asList(lessons.get(2))));
        expectedLessons.put(DayOfWeek.THURSDAY, new ArrayList<>());
        expectedLessons.put(DayOfWeek.FRIDAY, new ArrayList<>());
        expectedLessons.put(DayOfWeek.SATURDAY, new ArrayList<>());

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to get week lessons for a lecturer with id: " + lecturerId + ".",
                "When get week lessons for a lecturer with id " + lecturerId + " the result is: " + expectedLessons + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonService.getLecturerWeekLessons(lecturerId);

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

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

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to get " + testMonth.getMonth() + " month of " + testMonth.getYear() + " year lessons for a lecturer with id: " + testId + ".",
                "A lecturer id " + testId + " is not positive when get " + testMonth.getMonth() + " month of " + testMonth.getYear() + " year lessons for a lecturer."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonService.getLecturerMonthLessons(testId, testMonth);
        } catch (ServiceException serviceException) {
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
    void shouldGenerateLogsWhenResultIsEmptyWhileGetLecturerMonthLessons() {
        int testId = 3;
        YearMonth testMonth = YearMonth.of(2021, 5);
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.WARN));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to get " + testMonth.getMonth() + " month of " + testMonth.getYear() + " year lessons for a lecturer with id: " + testId + ".",
                "There are not any " + testMonth.getMonth() + " month of " + testMonth.getYear() + " year lessons for a lecturer with id " + testId + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonService.getLecturerMonthLessons(testId, testMonth);

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }
    
    @Test
    void shouldGenerateLogsWhenDAOExceptionWhileGetLecturerMonthLessons() {
        int testId = 6;
        YearMonth testMonth = YearMonth.of(2020, 9);

        when(lessonDAO.getLecturerDayLessons(anyInt(),any(DayOfWeek.class))).thenThrow(DAOException.class);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to get " + testMonth.getMonth() + " month of " + testMonth.getYear() + " year lessons for a lecturer with id: " + testId + ".",
                "There is some error in dao layer when get " + testMonth.getMonth() + " month of " + testMonth.getYear() + " year lessons for a lecturer with id " + testId + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonService.getLecturerMonthLessons(testId, testMonth);
        } catch (ServiceException serviceException) {
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
    void shouldGenerateLogsWhenGetLecturerMonthLessons() {
        int lecturerId = 2;
        YearMonth month = YearMonth.of(2020, 10);
        List<Lesson> lessons = new ArrayList<>(Arrays.asList(
                new Lesson(), new Lesson(), new Lesson()));
        List<String> names = new ArrayList<>(Arrays.asList(
                "Lesson-101", "Lesson-102", "Lesson-103"));
        List<String> audiences = new ArrayList<>(Arrays.asList(
                "101", "102", "103"));
        List<Lecturer> lecturers = new ArrayList<>(Arrays.asList(
                lecturer2, lecturer2, lecturer2));
        List<Group> groups = new ArrayList<>(Arrays.asList(
                group2, group2, group1));
        List<DayOfWeek> days = new ArrayList<>(Arrays.asList(
                DayOfWeek.THURSDAY, DayOfWeek.MONDAY, DayOfWeek.FRIDAY));
        List<LessonTime> lessonTimes = new ArrayList<>(Arrays.asList(
                lessonTime2, lessonTime1, lessonTime2));
        
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

        when(lessonDAO.getLecturerDayLessons(lecturerId, DayOfWeek.THURSDAY)).thenReturn(lessons.subList(0, 1));
        when(lessonDAO.getLecturerDayLessons(lecturerId, DayOfWeek.MONDAY)).thenReturn(lessons.subList(1, 2));
        when(lessonDAO.getLecturerDayLessons(lecturerId, DayOfWeek.FRIDAY)).thenReturn(lessons.subList(2, 3));
        
        when(lessonDAO.getLessonGroup(lessons.get(0).getId())).thenReturn(lessons.get(0).getGroup());
        when(lessonDAO.getLessonLecturer(lessons.get(0).getId())).thenReturn(lessons.get(0).getLecturer());
        when(lessonDAO.getLessonTime(lessons.get(0).getId())).thenReturn(lessons.get(0).getLessonTime());
        
        when(lessonDAO.getLessonGroup(lessons.get(1).getId())).thenReturn(lessons.get(1).getGroup());
        when(lessonDAO.getLessonLecturer(lessons.get(1).getId())).thenReturn(lessons.get(1).getLecturer());
        when(lessonDAO.getLessonTime(lessons.get(1).getId())).thenReturn(lessons.get(1).getLessonTime());
        
        when(lessonDAO.getLessonGroup(lessons.get(2).getId())).thenReturn(lessons.get(2).getGroup());
        when(lessonDAO.getLessonLecturer(lessons.get(2).getId())).thenReturn(lessons.get(2).getLecturer());
        when(lessonDAO.getLessonTime(lessons.get(2).getId())).thenReturn(lessons.get(2).getLessonTime());
        
        Map<LocalDate, List<Lesson>> expectedLessons = new TreeMap<>();
        
        for(int i = 1; i <= month.lengthOfMonth(); i++) {
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

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to get " + month.getMonth() + " month of " + month.getYear() + " year lessons for a lecturer with id: " + lecturerId + ".",
                "When get " + month.getMonth() + " month of " + month.getYear() + " year lessons for a lecturer with id " + lecturerId + " the result is: " + expectedLessons + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonService.getLecturerMonthLessons(lecturerId, month);

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }
}