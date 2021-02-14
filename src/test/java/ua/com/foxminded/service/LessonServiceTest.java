package ua.com.foxminded.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.ArgumentMatchers.anyInt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ua.com.foxminded.dao.LessonDAO;
import ua.com.foxminded.domain.Lesson;

class LessonServiceTest {
    @InjectMocks
    private LessonService lessonService;

    @Mock
    private LessonDAO lessonDAO;

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
}
