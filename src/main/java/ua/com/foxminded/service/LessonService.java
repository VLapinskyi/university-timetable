package ua.com.foxminded.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
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

    public void create(Lesson lesson) {
        lessonDAO.create(lesson);
    }

    public List<Lesson> getAll() {
        return lessonDAO.findAll();
    }

    public Lesson getById(int lessonId) {
        return lessonDAO.findById(lessonId);
    }

    public void update(Lesson updatedLesson) {
        lessonDAO.update(updatedLesson);
    }

    public void deleteById(int lessonId) {
        Lesson lesson = lessonDAO.findById(lessonId);
        lessonDAO.delete(lesson);
    }

    public Map<DayOfWeek, List<Lesson>> getGroupWeekLessons(int groupId) {
        Map<DayOfWeek, List<Lesson>> weekLessons = new TreeMap<>();
        for (int i = 1; i <= DayOfWeek.values().length; i++) {
            List<Lesson> dayLessons = lessonDAO.getGroupDayLessons(groupId, DayOfWeek.of(i));
            weekLessons.put(DayOfWeek.of(i), dayLessons);
        }
        return weekLessons;
    }

    public Map<LocalDate, List<Lesson>> getGroupMonthLessons(int groupId, YearMonth month) {
        Map<LocalDate, List<Lesson>> dailyLessons = new TreeMap<>();
        for (int i = 1; i <= month.lengthOfMonth(); i++) {
            LocalDate day = month.atDay(i);
            List<Lesson> lessons = lessonDAO.getGroupDayLessons(groupId, day.getDayOfWeek());
            dailyLessons.put(day, lessons);
        }
        return dailyLessons;
    }

    public Map<DayOfWeek, List<Lesson>> getLecturerWeekLessons(int lecturerId) {
        Map<DayOfWeek, List<Lesson>> weekLessons = new TreeMap<>();
        for (int i = 1; i <= DayOfWeek.values().length; i++) {
            List<Lesson> dayLessons = lessonDAO.getLecturerDayLessons(lecturerId, DayOfWeek.of(i));
            weekLessons.put(DayOfWeek.of(i), dayLessons);
        }
        return weekLessons;
    }

    public Map<LocalDate, List<Lesson>> getLecturerMonthLessons(int lecturerId, YearMonth month) {
        Map<LocalDate, List<Lesson>> dailyLessons = new TreeMap<>();
        for (int i = 1; i <= month.lengthOfMonth(); i++) {
            LocalDate day = month.atDay(i);
            List<Lesson> lessons = lessonDAO.getLecturerDayLessons(lecturerId, day.getDayOfWeek());
            dailyLessons.put(day, lessons);
        }
        return dailyLessons;
    }
}
