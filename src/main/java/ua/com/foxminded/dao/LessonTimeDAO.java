package ua.com.foxminded.dao;

import java.sql.Time;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import ua.com.foxminded.domain.LessonTime;

@Repository
public class LessonTimeDAO implements GenericDAO<LessonTime> {
    private final JdbcTemplate jdbcTemplate;
    @Autowired
    private Environment environment;
    
    @Autowired
    public LessonTimeDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    @Override
    public void create(LessonTime lessonTime) {
        Time startTime = null;
        Time endTime = null;
        if (lessonTime.getStartTime() != null) {
            startTime = Time.valueOf(lessonTime.getStartTime());
        }
        if (lessonTime.getEndTime() != null) {
            endTime = Time.valueOf(lessonTime.getEndTime());
        }
        jdbcTemplate.update(environment.getProperty("create.lesson.time"), startTime, endTime);
    }

    @Override
    public List<LessonTime> findAll() {
        return jdbcTemplate.query(environment.getProperty("find.all.lesson.times"), new LessonTimeMapper());
    }

    @Override
    public LessonTime findById(int id) {
        return jdbcTemplate.queryForStream(environment.getProperty("find.lesson.time.by.id"), new LessonTimeMapper(), id)
                .findAny().orElse(null);
    }

    @Override
    public void update(int id, LessonTime lessonTime) {
        jdbcTemplate.update(environment.getProperty("update.lesson.time"), lessonTime.getStartTime(), lessonTime.getEndTime(), id);
    }

    @Override
    public void deleteById(int id) {
        jdbcTemplate.update(environment.getProperty("delete.lesson.time.by.id"), id);        
    }

}
