package ua.com.foxminded.repositories.interfaces;

import java.time.DayOfWeek;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import ua.com.foxminded.domain.Lesson;

public interface LessonRepository extends JpaRepository<Lesson, Integer> {
    public List<Lesson> findByGroupIdAndDay(Integer groupId, DayOfWeek day);
    
    public List<Lesson> findByLecturerIdAndDay(Integer lecturerId, DayOfWeek day);
}
