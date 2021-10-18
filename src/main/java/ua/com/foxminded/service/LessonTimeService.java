package ua.com.foxminded.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ua.com.foxminded.domain.LessonTime;
import ua.com.foxminded.repositories.LessonTimeRepository;

@Service
public class LessonTimeService {
    private LessonTimeRepository lessonTimeRepository;

    @Autowired
    public LessonTimeService(LessonTimeRepository lessonTimeRepository) {
        this.lessonTimeRepository = lessonTimeRepository;
    }

    public void create(LessonTime lessonTime) {
        lessonTimeRepository.create(lessonTime);
    }

    public List<LessonTime> getAll() {
        return lessonTimeRepository.findAll();
    }

    public LessonTime getById(int lessonTimeId) {
        return lessonTimeRepository.findById(lessonTimeId);
    }

    public void update(LessonTime updatedLessonTime) {
        lessonTimeRepository.update(updatedLessonTime);
    }

    public void deleteById(int lessonTimeId) {
        LessonTime lessonTime = lessonTimeRepository.findById(lessonTimeId);
        lessonTimeRepository.delete(lessonTime);
    }
}
