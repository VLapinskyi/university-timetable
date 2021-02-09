package ua.com.foxminded.dao;

import java.time.DayOfWeek;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import ua.com.foxminded.domain.Lesson;
import ua.com.foxminded.mapper.LessonMapper;

@Repository
public class LessonDAO implements GenericDAO<Lesson> {
    private JdbcTemplate jdbcTemplate;
    private Environment environment;
    
    @Autowired
    public LessonDAO (JdbcTemplate jdbcTemplate, Environment environment) {
        this.jdbcTemplate = jdbcTemplate;
        this.environment = environment;
    }
    
    @Override
    public void create(Lesson lesson) {
     jdbcTemplate.update(environment.getProperty("create.lesson"), lesson.getName(), lesson.getAudience(),
             lesson.getDay().getValue());
    }

    @Override
    public List<Lesson> findAll() {
        return jdbcTemplate.query(environment.getProperty("find.all.lessons"), new LessonMapper());
    }

    @Override
    public Lesson findById(int id) {
        return jdbcTemplate.queryForStream(environment.getProperty("find.lesson.by.id"), new LessonMapper(), id)
                .findAny().orElse(null);
    }

    @Override
    public void update(int id, Lesson lesson) {
        jdbcTemplate.update(environment.getProperty("update.lesson"), lesson.getName(),
                lesson.getAudience(), lesson.getDay().getValue(), id);
        
    }

    @Override
    public void deleteById(int id) {
        jdbcTemplate.update(environment.getProperty("delete.lesson"), id);
    }
    
    public List<Lesson> getDayLessonsForGroup(int groupId, DayOfWeek weekDay) {
        return jdbcTemplate.query(environment.getProperty("get.day.lessons.for.group"), new LessonMapper(),
                groupId, weekDay.getValue());
    }
    
    public List<Lesson> getDayLessonsForLecturer(int lecturerId, DayOfWeek weekDay) {
        return jdbcTemplate.query(environment.getProperty("get.day.lessons.for.lecturer"), new LessonMapper(),
                lecturerId, weekDay.getValue());
    }
}
