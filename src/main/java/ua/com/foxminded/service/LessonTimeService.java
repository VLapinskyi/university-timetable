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

    public void create(LessonTime lessonTime) {
        lessonTimeDAO.create(lessonTime);
    }

    public List<LessonTime> getAll() {
        return lessonTimeDAO.findAll();
    }

    public LessonTime getById(int lessonTimeId) {
        return lessonTimeDAO.findById(lessonTimeId);
    }

    public void update(LessonTime updatedLessonTime) {
        lessonTimeDAO.update(updatedLessonTime.getId(), updatedLessonTime);
    }

    public void deleteById(int lessonTimeId) {
        lessonTimeDAO.deleteById(lessonTimeId);
    }
}
