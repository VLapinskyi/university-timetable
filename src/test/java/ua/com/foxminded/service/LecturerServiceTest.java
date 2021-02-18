package ua.com.foxminded.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ua.com.foxminded.dao.LecturerDAO;
import ua.com.foxminded.domain.Lecturer;
import ua.com.foxminded.domain.Lesson;

class LecturerServiceTest {
    @InjectMocks
    private LecturerService lecturerService;
    
    @Mock
    private LecturerDAO lecturerDAO;
    @Mock
    LessonService lessonService;
    
    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldCreateLecturer() {
        Lecturer lecturer = new Lecturer();
        lecturer.setFirstName("Valentyn");
        lecturer.setLastName("Lapinskyi");
        lecturerService.createLecturer(lecturer);
        verify(lecturerDAO).create(lecturer);
    }
    
    @Test
    void shouldGetAllLecturers() {
        List<Lecturer> lecturers = new ArrayList<>(Arrays.asList(
                new Lecturer(), new Lecturer()));
        lecturers.get(0).setId(1);
        lecturers.get(1).setId(2);
        
        List<Lesson> lessons = new ArrayList<>(Arrays.asList(
                new Lesson(), new Lesson()));
        lessons.get(0).setId(1);
        lessons.get(0).setLecturer(lecturers.get(1));
        lessons.get(1).setId(2);
        lessons.get(1).setLecturer(lecturers.get(0));
        
        List<Lecturer> expectedLecturers = new ArrayList<>(lecturers);
        expectedLecturers.get(0).setLessons(lessons.subList(0, 1));
        expectedLecturers.get(1).setLessons(lessons.subList(1, lessons.size()));
        
        when(lecturerDAO.findAll()).thenReturn(lecturers);
        when(lessonService.getAllLessons()).thenReturn(lessons);
        List<Lecturer> actualLecturers = lecturerService.getAllLecturers();
        assertTrue(expectedLecturers.containsAll(actualLecturers) && actualLecturers.containsAll(expectedLecturers));
        verify(lecturerDAO).findAll();
        verify(lessonService).getAllLessons();
    }
    
    @Test
    void shouldGetLecturerById() {
        int testLecturerId = 2;
        Lecturer lecturer = new Lecturer();
        lecturer.setId(testLecturerId);
        
        List<Lesson> lessons = new ArrayList<>(Arrays.asList(
                new Lesson(), new Lesson()));
        lessons.get(0).setId(1);
        lessons.get(1).setId(2);
        lessons.stream().forEach(lesson -> lesson.setLecturer(lecturer));
        
        Lecturer expectedLecturer = new Lecturer();
        lecturer.setId(testLecturerId);
        lecturer.setLessons(lessons);
        
        when(lecturerDAO.findById(testLecturerId)).thenReturn(lecturer);
        when(lessonService.getAllLessons()).thenReturn(lessons);
        Lecturer actualLecturer = lecturerService.getLecturerById(testLecturerId);
        assertEquals(expectedLecturer, actualLecturer);
        verify(lecturerDAO).findById(testLecturerId);
        verify(lessonService).getAllLessons();
    }
}
