package ua.com.foxminded.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ua.com.foxminded.domain.Lesson;
import ua.com.foxminded.repositories.interfaces.LessonRepository;

@Service
public class LessonService {
    private LessonRepository lessonRepository;

    @Autowired
    public LessonService(LessonRepository lessonRepository) {
        this.lessonRepository = lessonRepository;
    }

    public void create(Lesson lesson) {
        lessonRepository.save(lesson);
    }

    public List<Lesson> getAll() {
        return lessonRepository.findAll();
    }

    public Lesson getById(int lessonId) {
        return lessonRepository.findById(lessonId).get();
    }

    public void update(Lesson updatedLesson) {
        lessonRepository.save(updatedLesson);
    }

    public void deleteById(int lessonId) {
        lessonRepository.deleteById(lessonId);
    }

    public Map<DayOfWeek, List<Lesson>> getGroupWeekLessons(int groupId) {
        Map<DayOfWeek, List<Lesson>> weekLessons = new TreeMap<>();
        for (int i = 1; i <= DayOfWeek.values().length; i++) {
            List<Lesson> dayLessons = lessonRepository.findByGroupIdAndDay(groupId, DayOfWeek.of(i));
            weekLessons.put(DayOfWeek.of(i), dayLessons);
        }
        return weekLessons;
    }

    public Map<LocalDate, List<Lesson>> getGroupMonthLessons(int groupId, YearMonth month) {
        Map<LocalDate, List<Lesson>> dailyLessons = new TreeMap<>();
        for (int i = 1; i <= month.lengthOfMonth(); i++) {
            LocalDate day = month.atDay(i);
            List<Lesson> lessons = lessonRepository.findByGroupIdAndDay(groupId, day.getDayOfWeek());
            dailyLessons.put(day, lessons);
        }
        return dailyLessons;
    }

    public Map<DayOfWeek, List<Lesson>> getLecturerWeekLessons(int lecturerId) {
        Map<DayOfWeek, List<Lesson>> weekLessons = new TreeMap<>();
        for (int i = 1; i <= DayOfWeek.values().length; i++) {
            List<Lesson> dayLessons = lessonRepository.findByLecturerIdAndDay(lecturerId, DayOfWeek.of(i));
            weekLessons.put(DayOfWeek.of(i), dayLessons);
        }
        return weekLessons;
    }

    public Map<LocalDate, List<Lesson>> getLecturerMonthLessons(int lecturerId, YearMonth month) {
        Map<LocalDate, List<Lesson>> dailyLessons = new TreeMap<>();
        for (int i = 1; i <= month.lengthOfMonth(); i++) {
            LocalDate day = month.atDay(i);
            List<Lesson> lessons = lessonRepository.findByLecturerIdAndDay(lecturerId, day.getDayOfWeek());
            dailyLessons.put(day, lessons);
        }
        return dailyLessons;
    }
}
