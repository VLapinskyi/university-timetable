package ua.com.foxminded.dao;

import java.sql.Time;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import ua.com.foxminded.domain.LessonTime;
import ua.com.foxminded.mapper.LessonTimeMapper;

@Repository
public class LessonTimeDAO implements GenericDAO<LessonTime> {
    private static final Logger LOGGER = LoggerFactory.getLogger(LessonTimeDAO.class);
    private final JdbcTemplate jdbcTemplate;
    private Environment environment;

    @Autowired
    public LessonTimeDAO(JdbcTemplate jdbcTemplate, Environment environment) {
        this.jdbcTemplate = jdbcTemplate;
        this.environment  = environment;
    }

    @Override
    public void create(LessonTime lessonTime) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to insert new lessonTime: {}.", lessonTime);
        }
        Time startTime = null;
        Time endTime = null;
        if (lessonTime.getStartTime() != null) {
            startTime = Time.valueOf(lessonTime.getStartTime());
        }
        if (lessonTime.getEndTime() != null) {
            endTime = Time.valueOf(lessonTime.getEndTime());
        }
        
        jdbcTemplate.update(environment.getProperty("create.lesson.time"), startTime, endTime);
        
       if (LOGGER.isDebugEnabled()) {
           LOGGER.debug("The lessonTime {} was inserted.", lessonTime);
       }
    }

    @Override
    public List<LessonTime> findAll() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to find all lessonTimes.");
        }
        
        List<LessonTime> resultLessonTimes = jdbcTemplate.query(environment.getProperty("find.all.lesson.times"), new LessonTimeMapper());
        
        if (resultLessonTimes.isEmpty()) {
            LOGGER.warn("There are not any lessonTimes in the result.");
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("The result is: {}.", resultLessonTimes);
            }
        }
        
        return resultLessonTimes;
    }

    @Override
    public LessonTime findById(int id) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to find lessonTime by id {}.", id);
        }
        
        LessonTime resultLessonTime = jdbcTemplate.queryForStream(environment.getProperty("find.lesson.time.by.id"), new LessonTimeMapper(), id)
                .findAny().orElse(null);
        
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("The result lessonTime with id {} is {}.", id, resultLessonTime);
        }
        
        return resultLessonTime;
    }

    @Override
    public void update(int id, LessonTime lessonTime) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to update lessonTime {} with id {}.", lessonTime, id);
        }
        
        jdbcTemplate.update(environment.getProperty("update.lesson.time"), lessonTime.getStartTime(), lessonTime.getEndTime(), id);
        
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("The lessonTime {} with id {} was changed.", lessonTime, id);
        }
    }

    @Override
    public void deleteById(int id) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to delete lessonTime by id {}.", id);
        }
        
        jdbcTemplate.update(environment.getProperty("delete.lesson.time.by.id"), id);
        
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("The lessonTime with id {} was deleted.", id);
        }
    }
}
