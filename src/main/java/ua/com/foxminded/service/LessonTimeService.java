package ua.com.foxminded.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ua.com.foxminded.dao.LessonTimeDAO;
import ua.com.foxminded.domain.LessonTime;

@Service
public class LessonTimeService {
    private LessonTimeDAO lessonTimeDAO;
    
    @Autowired
    public LessonTimeService(LessonTimeDAO lessonTimeDAO) {
        this.lessonTimeDAO = lessonTimeDAO;
    }
    
    public void createLessonTime(LessonTime lessonTime) {
        lessonTimeDAO.create(lessonTime);
    }
    
    public List<LessonTime> getAllLessonTimes() {
        return lessonTimeDAO.findAll();
    }
    
    public LessonTime getLessonTimeById(int lessonTimeId) {
        return lessonTimeDAO.findById(lessonTimeId);
    }
    
    public void changeLessonTimeData(int lessonId, LessonTime updatedLessonTime) {
        lessonTimeDAO.update(lessonId, updatedLessonTime);
    }
    
    public void deleteLessonTimeById(int lessonTimeId) {
        lessonTimeDAO.deleteById(lessonTimeId);
    }
}
