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

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.Month;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import org.springframework.test.util.ReflectionTestUtils;

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
    void shouldGetWeekLessonsForGroup() {
        int groupId = 2;
        lessonService.getGroupWeekLessons(groupId);
        for (int i = 1; i <= DayOfWeek.values().length; i++) {
            verify(lessonDAO).getGroupDayLessons(groupId, DayOfWeek.of(i));
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
        lessonService.getLecturerWeekLessons(lecturerId);
        for (int i = 1; i <= DayOfWeek.values().length; i++) {
            verify(lessonDAO).getLecturerDayLessons(lecturerId, DayOfWeek.of(i));
        }
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
}