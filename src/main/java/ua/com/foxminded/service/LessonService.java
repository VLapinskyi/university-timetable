package ua.com.foxminded.service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ua.com.foxminded.dao.LessonDAO;
import ua.com.foxminded.domain.Lesson;

@Service
public class LessonService {
    private LessonDAO lessonDAO;
    
    @Autowired
    public LessonService(LessonDAO lessonDAO) {
        this.lessonDAO = lessonDAO;
    }
    
    public void createLesson (int lecturerId, int groupId, int lessonTimeId, Lesson lesson) {
        lessonDAO.create(lesson);
        Optional<Lesson> createdLesson = lessonDAO.findAll().stream().max(Comparator.comparing(Lesson::getId));
        int lessonId = 0;
        if(createdLesson.isPresent()) {
            lessonId = createdLesson.get().getId();
        }
        lessonDAO.setLessonLecturer(lecturerId, lessonId);
        lessonDAO.setLessonGroup(groupId, lessonId);
        lessonDAO.setLessonTime(lessonTimeId, lessonId);
    }
    
    public List<Lesson> getAllLessons() {
        List<Lesson> lessons = lessonDAO.findAll();
        lessons.stream().forEach(lesson -> lesson.setGroup(lessonDAO.getLessonGroup(lesson.getId())));
        lessons.stream().forEach(lesson -> lesson.setLecturer(lessonDAO.getLessonLecturer(lesson.getId())));
        lessons.stream().forEach(lesson -> lesson.setLessonTime(lessonDAO.getLessonTime(lesson.getId())));
        return lessons;
    }
    
    public Lesson getLessonById (int lessonId) {
        Lesson lesson =  lessonDAO.findById(lessonId);
        lesson.setGroup(lessonDAO.getLessonGroup(lessonId));
        lesson.setLecturer(lessonDAO.getLessonLecturer(lessonId));
        lesson.setLessonTime(lessonDAO.getLessonTime(lessonId));
        return lesson;     
    }
}
