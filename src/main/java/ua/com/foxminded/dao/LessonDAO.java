package ua.com.foxminded.dao;

import java.time.DayOfWeek;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import ua.com.foxminded.domain.Group;
import ua.com.foxminded.domain.Lecturer;
import ua.com.foxminded.domain.Lesson;

@Repository
public class LessonDAO implements GenericDAO<Lesson> {
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private Environment environment;
    
    @Autowired
    public LessonDAO (JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    @Override
    public void create(Lesson lesson) {
     jdbcTemplate.update(environment.getProperty("create.lesson"), lesson.getName(), lesson.getLecturer().getId(),
             lesson.getGroup().getId(), lesson.getAudience(), lesson.getDay().getValue(),
             lesson.getLessonTime().getStartTime(), lesson.getLessonTime().getEndTime());
    }

    @Override
    public List<Lesson> findAll() {
        return jdbcTemplate.query(environment.getProperty("find.all.lessons"), new LessonMapper());
    }

    @Override
    public Lesson findById(int id) {
        return jdbcTemplate.queryForStream(environment.getProperty("find.lesson.by.id"), new LessonMapper(), id).findAny().orElse(null);
    }

    @Override
    public void update(int id, Lesson lesson) {
        jdbcTemplate.update(environment.getProperty("update.lesson"), lesson.getName(), lesson.getLecturer().getId(),
                lesson.getGroup().getId(), lesson.getAudience(), lesson.getDay().getValue(), id);
        
    }

    @Override
    public void deleteById(int id) {
        jdbcTemplate.update(environment.getProperty("delete.lesson"), id);
    }
    
    public List<Lesson> getDayLessonsForGroup(Group group, DayOfWeek weekDay) {
        return jdbcTemplate.query(environment.getProperty("get.day.lessons.for.group"), new LessonMapper(),
                group.getId(), weekDay.getValue());
    }
    
    public List<Lesson> getDayLessonsForLecturer(Lecturer lecturer, DayOfWeek weekDay) {
        return jdbcTemplate.query(environment.getProperty("get.day.lessons.for.lecturer"), new LessonMapper(),
                lecturer.getId(), weekDay.getValue());
    }
}
