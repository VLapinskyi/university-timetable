package ua.com.foxminded.service;

import java.time.DayOfWeek;
import java.time.YearMonth;
import java.util.ArrayList;
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

    public void updateLesson(int lessonId, Lesson updatedLesson) {
        lessonDAO.update(lessonId, updatedLesson);
        lessonDAO.setLessonLecturer(updatedLesson.getLecturer().getId(), lessonId);
        lessonDAO.setLessonGroup(updatedLesson.getGroup().getId(), lessonId);
        lessonDAO.setLessonTime(updatedLesson.getLessonTime().getId(), lessonId);
    }

    public List<Lesson> getGroupWeekLessons (int groupId) {
        List<Lesson> weekLessons = new ArrayList<>();
        for (int i = 1; i <= DayOfWeek.values().length; i++) {
            weekLessons.addAll(lessonDAO.getGroupDayLessons(groupId, DayOfWeek.of(i)));
        }
        return weekLessons;
    }

    public List<Lesson> getGroupMonthLessons(int groupId, YearMonth month) {
        List<Lesson> lessons = new ArrayList<>();
        for (int i = 1; i <= month.lengthOfMonth(); i++) {
            DayOfWeek day = month.atDay(i).getDayOfWeek();
            lessons.addAll(lessonDAO.getGroupDayLessons(groupId, day));
        }
        return lessons;
    }
    
    public List<Lesson> getLecturerWeekLessons(int lecturerId) {
        List<Lesson> weekLessons = new ArrayList<>();
        for (int i = 1; i <= DayOfWeek.values().length; i++) {
            weekLessons.addAll(lessonDAO.getLecturerDayLessons(lecturerId, DayOfWeek.of(i)));
        }
        return weekLessons;
    }
    
    public List<Lesson> getLecturerMonthLessons(int lecturerId, YearMonth month) {
        List<Lesson> lessons = new ArrayList<>();
        for (int i = 1; i <= month.lengthOfMonth(); i++) {
            DayOfWeek day = month.atDay(i).getDayOfWeek();
            lessons.addAll(lessonDAO.getLecturerDayLessons(lecturerId, day));
        }
        return lessons;
    }
}
