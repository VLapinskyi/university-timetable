package ua.com.foxminded.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.any;

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
	lessonService.getWeekLessonsForGroup(groupId);
	
	verify(lessonDAO).getDayLessonsForGroup(groupId, DayOfWeek.MONDAY);
	verify(lessonDAO).getDayLessonsForGroup(groupId, DayOfWeek.TUESDAY);
	verify(lessonDAO).getDayLessonsForGroup(groupId, DayOfWeek.WEDNESDAY);
	verify(lessonDAO).getDayLessonsForGroup(groupId, DayOfWeek.THURSDAY);
	verify(lessonDAO).getDayLessonsForGroup(groupId, DayOfWeek.FRIDAY);
	verify(lessonDAO).getDayLessonsForGroup(groupId, DayOfWeek.SATURDAY);
	verify(lessonDAO).getDayLessonsForGroup(groupId, DayOfWeek.SUNDAY);
    }
    
    @Test
    void shouldGetMonthLessonForGroup() {
	int groupId = 1;
	YearMonth month = YearMonth.of(2021, Month.FEBRUARY);
	int monthLength = 28;
	List<DayOfWeek> expectedDays = new ArrayList<>(Arrays.asList(
		DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY,
		DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY,
		DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY,
		DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY));
	lessonService.getMonthLessonsForGroup(groupId, month);
	verify(lessonDAO).getDayLessonsForGroup(groupId, DayOfWeek.MONDAY);
	
	List<DayOfWeek> actualDays = dayCaptor.getAllValues();
	
	for (int i = 0; i < monthLength; i++) {
	    assertEquals(expectedDays.get(i), actualDays.get(i));
	}
    }
}