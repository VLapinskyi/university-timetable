package ua.com.foxminded.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ua.com.foxminded.dao.LecturerDAO;
import ua.com.foxminded.domain.Lecturer;
import ua.com.foxminded.domain.Lesson;

@Service
public class LecturerService {
    private LecturerDAO lecturerDAO;
    private LessonService lessonService;
    
    @Autowired
    public LecturerService(LecturerDAO lecturerDAO, LessonService lessonService) {
        this.lecturerDAO = lecturerDAO;
        this.lessonService = lessonService;
    }
    
    public void createLecturer(Lecturer lecturer) {
        lecturerDAO.create(lecturer);
    }
    
    public List<Lecturer> getAllLecturers() {
        List<Lecturer> lecturers = lecturerDAO.findAll();
        List<Lesson> lessons = lessonService.getAllLessons();
        lecturers.stream().forEach(lecturer -> {
           List<Lesson> lecturerLessons = new ArrayList<>();
           for (Lesson lesson : lessons) {
               if(lesson.getLecturer().getId() == lecturer.getId()) {
                   lecturerLessons.add(lesson);
               }
           }
           lecturer.setLessons(lecturerLessons);
        });
        return lecturers;
    }
    
    public Lecturer getLecturerById (int lecturerId) {
        Lecturer lecturer = lecturerDAO.findById(lecturerId);
        lecturer.setLessons(lessonService.getAllLessons().stream()
                .filter(lesson -> lesson.getLecturer().getId() == lecturer.getId()).collect(Collectors.toList()));
        return lecturer;
    }
    
    public void updateLecturer(int lecturerId, Lecturer lecturer) {
	lecturerDAO.update(lecturerId, lecturer);
    }
    
    public void deleteLecturerById(int lecturerId) {
	lecturerDAO.deleteById(lecturerId);
    }
}
