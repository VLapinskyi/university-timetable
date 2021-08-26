package ua.com.foxminded.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

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

    public void create (Lesson lesson) {
        lessonDAO.create(lesson);
        Optional<Lesson> createdLesson = lessonDAO.findAll().stream().max(Comparator.comparing(Lesson::getId));
        int lessonId = 0;
        if(createdLesson.isPresent()) {
            lessonId = createdLesson.get().getId();
        }
        lessonDAO.setLessonLecturer(lesson.getLecturer().getId(), lessonId);
        lessonDAO.setLessonGroup(lesson.getGroup().getId(), lessonId);
        lessonDAO.setLessonTime(lesson.getLessonTime().getId(), lessonId);
    }

    public List<Lesson> getAll() {
        List<Lesson> lessons = lessonDAO.findAll();
        lessons.stream().forEach(lesson -> lesson.setGroup(lessonDAO.getLessonGroup(lesson.getId())));
        lessons.stream().forEach(lesson -> lesson.setLecturer(lessonDAO.getLessonLecturer(lesson.getId())));
        lessons.stream().forEach(lesson -> lesson.setLessonTime(lessonDAO.getLessonTime(lesson.getId())));
        return lessons;
    }

    public Lesson getById (int lessonId) {
        Lesson lesson =  lessonDAO.findById(lessonId);
        lesson.setGroup(lessonDAO.getLessonGroup(lessonId));
        lesson.setLecturer(lessonDAO.getLessonLecturer(lessonId));
        lesson.setLessonTime(lessonDAO.getLessonTime(lessonId));
        return lesson;     
    }

    public void update(Lesson updatedLesson) {
        lessonDAO.update(updatedLesson.getId(), updatedLesson);
        lessonDAO.setLessonLecturer(updatedLesson.getLecturer().getId(), updatedLesson.getId());
        lessonDAO.setLessonGroup(updatedLesson.getGroup().getId(), updatedLesson.getId());
        lessonDAO.setLessonTime(updatedLesson.getLessonTime().getId(), updatedLesson.getId());
    }

    public void deleteById (int lessonId) {
        lessonDAO.deleteById(lessonId);
    }

    public Map<DayOfWeek, List<Lesson>> getGroupWeekLessons (int groupId) {
        Map<DayOfWeek, List<Lesson>> weekLessons = new TreeMap<>();
        for (int i = 1; i <= DayOfWeek.values().length; i++) {
            List<Lesson> dayLessons = lessonDAO.getGroupDayLessons(groupId, DayOfWeek.of(i));
            if(!dayLessons.isEmpty()) {
                dayLessons.stream().forEach(lesson -> lesson.setGroup(lessonDAO.getLessonGroup(lesson.getId())));
                dayLessons.stream().forEach(lesson -> lesson.setLecturer(lessonDAO.getLessonLecturer(lesson.getId())));
                dayLessons.stream().forEach(lesson -> lesson.setLessonTime(lessonDAO.getLessonTime(lesson.getId())));
            }
            weekLessons.put(DayOfWeek.of(i), dayLessons);
        }
        return weekLessons;
    }

    public Map<LocalDate, List<Lesson>> getGroupMonthLessons(int groupId, YearMonth month) {
        Map<LocalDate, List<Lesson>> dailyLessons = new TreeMap<>();
        for (int i = 1; i <= month.lengthOfMonth(); i++) {
            LocalDate day = month.atDay(i);
            List<Lesson> lessons = lessonDAO.getGroupDayLessons(groupId, day.getDayOfWeek());
            if (!lessons.isEmpty()) {
                lessons.stream().forEach(lesson -> lesson.setGroup(lessonDAO.getLessonGroup(lesson.getId())));
                lessons.stream().forEach(lesson -> lesson.setLecturer(lessonDAO.getLessonLecturer(lesson.getId())));
                lessons.stream().forEach(lesson -> lesson.setLessonTime(lessonDAO.getLessonTime(lesson.getId())));
            }
            dailyLessons.put(day, lessons);
        }
        return dailyLessons;
    }

    public Map<DayOfWeek, List<Lesson>> getLecturerWeekLessons(int lecturerId) {
        Map<DayOfWeek, List<Lesson>> weekLessons = new TreeMap<>();
        for (int i = 1; i <= DayOfWeek.values().length; i++) {
            List<Lesson> dayLessons = lessonDAO.getLecturerDayLessons(lecturerId, DayOfWeek.of(i));

            if(!dayLessons.isEmpty()) {
                dayLessons.stream().forEach(lesson -> lesson.setGroup(lessonDAO.getLessonGroup(lesson.getId())));
                dayLessons.stream().forEach(lesson -> lesson.setLecturer(lessonDAO.getLessonLecturer(lesson.getId())));
                dayLessons.stream().forEach(lesson -> lesson.setLessonTime(lessonDAO.getLessonTime(lesson.getId())));
            }
            weekLessons.put(DayOfWeek.of(i), dayLessons);
        }
        return weekLessons;
    }

    public Map<LocalDate, List<Lesson>> getLecturerMonthLessons(int lecturerId, YearMonth month) {
        Map<LocalDate, List<Lesson>> dailyLessons = new TreeMap<>();
        for (int i = 1; i <= month.lengthOfMonth(); i++) {
            LocalDate day = month.atDay(i);
            List<Lesson> lessons = lessonDAO.getLecturerDayLessons(lecturerId, day.getDayOfWeek());
            
            if (!lessons.isEmpty()) {
                lessons.stream().forEach(lesson -> lesson.setGroup(lessonDAO.getLessonGroup(lesson.getId())));
                lessons.stream().forEach(lesson -> lesson.setLecturer(lessonDAO.getLessonLecturer(lesson.getId())));
                lessons.stream().forEach(lesson -> lesson.setLessonTime(lessonDAO.getLessonTime(lesson.getId())));
            }
            dailyLessons.put(day, lessons);
        }
        return dailyLessons;
    }
}
