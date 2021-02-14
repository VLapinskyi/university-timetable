package ua.com.foxminded.service;

import static org.mockito.Mockito.verify;

import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ua.com.foxminded.dao.LessonTimeDAO;
import ua.com.foxminded.domain.LessonTime;

class LessonTimeServiceTest {
    @InjectMocks
    private LessonTimeService lessonTimeService;
    
    @Mock
    private LessonTimeDAO lessonTimeDAO;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }
    
    @Test
    void shouldCreateLessonTime() {
        LocalTime startTime = LocalTime.of(9, 0);
        LocalTime endTime = LocalTime.of(11, 0);
        LessonTime lessonTime = new LessonTime();
        lessonTime.setStartTime(startTime);
        lessonTime.setEndTime(endTime);
        
        lessonTimeService.createLessonTime(lessonTime);
        verify(lessonTimeDAO).create(lessonTime);
    }
    
    @Test
    void shouldGetAllLessonTimes() {
        lessonTimeService.getAllLessonTimes();
        verify(lessonTimeDAO).findAll();
    }
    
    @Test
    void shouldGetLessonTimeById() {
        int lessonTimeId = 2;
        lessonTimeService.getLessonTimeById(lessonTimeId);
        verify(lessonTimeDAO).findById(lessonTimeId);
    }
    
    @Test
    void shouldChangeLessonTimeData() {
        int lessonTimeId = 1;
        LocalTime startTime = LocalTime.of(9, 0);
        LocalTime endTime = LocalTime.of(11, 0);
        LessonTime lessonTime = new LessonTime();
        lessonTime.setStartTime(startTime);
        lessonTime.setEndTime(endTime);
        
        lessonTimeService.changeLessonTimeData(lessonTimeId, lessonTime);
        verify(lessonTimeDAO).update(lessonTimeId, lessonTime);
    }
    
    @Test
    void shouldDeleteLessonTimeById() {
        int lessonTimeId = 4;
        lessonTimeService.deleteLessonTimeById(lessonTimeId);
        verify(lessonTimeDAO).deleteById(lessonTimeId);
    }
}
