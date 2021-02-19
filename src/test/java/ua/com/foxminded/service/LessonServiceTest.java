package ua.com.foxminded.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.anyInt;

import java.time.DayOfWeek;
import java.time.Month;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ua.com.foxminded.dao.LessonDAO;
import ua.com.foxminded.domain.Group;
import ua.com.foxminded.domain.Lecturer;
import ua.com.foxminded.domain.Lesson;
import ua.com.foxminded.domain.LessonTime;

class LessonServiceTest {

    @InjectMocks
    private LessonService lessonService;   

    @Mock
    private LessonDAO lessonDAO;

    @Captor
    ArgumentCaptor<DayOfWeek> dayCaptor;
    @Captor
    ArgumentCaptor<Integer> numberCaptor;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldCreateLesson() {
        Lesson savedLesson = new Lesson();
        savedLesson.setId(1);

        Lesson creatingLesson = new Lesson();
        creatingLesson.setId(2);

        int lecturerId = 1;
        int groupId = 2;
        int lessonTimeId = 3;
        when(lessonDAO.findAll()).thenReturn(new ArrayList<Lesson>(Arrays.asList(savedLesson, creatingLesson)));
        lessonService.createLesson(lecturerId, groupId, lessonTimeId, creatingLesson);
        verify(lessonDAO).create(creatingLesson);
        verify(lessonDAO).setLessonLecturer(lecturerId, creatingLesson.getId());
        verify(lessonDAO).setLessonGroup(groupId, creatingLesson.getId());
        verify(lessonDAO).setLessonTime(lessonTimeId, creatingLesson.getId());
    }

    @Test
    void shouldGetAllLessons() {
        List<Integer> lessonIndexes = new ArrayList<>(Arrays.asList(
                1, 2, 3));
        List<Lesson> lessons = new ArrayList<>(Arrays.asList(
                new Lesson(), new Lesson(), new Lesson()));
        for(int i = 0; i < lessons.size(); i++) {
            lessons.get(i).setId(lessonIndexes.get(i));
        }

        when(lessonDAO.findAll()).thenReturn(lessons);
        lessonService.getAllLessons();
        verify(lessonDAO).findAll();

        verify(lessonDAO, times(3)).getLessonGroup(anyInt());
        for (Integer index : lessonIndexes) {
            verify(lessonDAO).getLessonGroup(index);
        }

        verify(lessonDAO, times(3)).getLessonLecturer(anyInt());
        for (Integer index : lessonIndexes) {
            verify(lessonDAO).getLessonLecturer(index);
        }

        verify(lessonDAO, times(3)).getLessonTime(anyInt());
        for (Integer index : lessonIndexes) {
            verify(lessonDAO).getLessonTime(index);
        }
    }

    @Test
    void shouldGetLessonById() {
        int lessonId = 4;
        Lesson lesson = new Lesson();
        lesson.setId(lessonId);
        when(lessonDAO.findById(lessonId)).thenReturn(lesson);
        lessonService.getLessonById(lessonId);
        verify(lessonDAO).findById(lessonId);
        verify(lessonDAO).getLessonGroup(lessonId);
        verify(lessonDAO).getLessonLecturer(lessonId);
        verify(lessonDAO).getLessonTime(lessonId);
    }

    @Test
    void shouldUpdateLesson() {
        int lecturerId = 1;
        Lecturer lecturer = new Lecturer();
        lecturer.setId(lecturerId);

        int groupId = 2;
        Group group = new Group();
        group.setId(groupId);

        int lessonTimeId = 3;
        LessonTime lessonTime = new LessonTime();
        lessonTime.setId(lessonTimeId);

        int lessonId = 4;
        Lesson lesson = new Lesson();
        lesson.setId(lessonId);
        lesson.setLecturer(lecturer);
        lesson.setGroup(group);
        lesson.setLessonTime(lessonTime);

        lessonService.updateLesson(lessonId, lesson);

        verify(lessonDAO).update(lessonId, lesson);
        verify(lessonDAO).setLessonLecturer(lecturerId, lessonId);
        verify(lessonDAO).setLessonGroup(groupId, lessonId);
        verify(lessonDAO).setLessonTime(lessonTimeId, lessonId);
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
}