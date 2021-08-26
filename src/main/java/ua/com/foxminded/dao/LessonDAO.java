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
import ua.com.foxminded.domain.LessonTime;
import ua.com.foxminded.mapper.GroupMapper;
import ua.com.foxminded.mapper.LecturerMapper;
import ua.com.foxminded.mapper.LessonMapper;
import ua.com.foxminded.mapper.LessonTimeMapper;

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
                Integer.toString(lesson.getDay().getValue()));
    }

    @Override
    public List<Lesson> findAll() {
        return jdbcTemplate.query(environment.getProperty("find.all.lessons"), new LessonMapper());
    }

    @Override
    public Lesson findById(int id) {
        return jdbcTemplate.queryForObject(environment.getProperty("find.lesson.by.id"), new LessonMapper(), id);
    }

    @Override
    public void update(int id, Lesson lesson) {
        jdbcTemplate.update(environment.getProperty("update.lesson"), lesson.getName(),
                lesson.getAudience(), Integer.toString(lesson.getDay().getValue()), id);
    }

    @Override
    public void deleteById(int id) {
        jdbcTemplate.update(environment.getProperty("delete.lesson"), id);
    }

    public void setLessonLecturer (int lecturerId, int lessonId) {
        jdbcTemplate.update(environment.getProperty("set.lesson.lecturer"), lecturerId, lessonId);
    }

    public Lecturer getLessonLecturer (int lessonId) {
        return jdbcTemplate.queryForObject(environment.getProperty("get.lesson.lecturer"), new LecturerMapper(), lessonId);
    }

    public void setLessonGroup (int groupId, int lessonId) {
        jdbcTemplate.update(environment.getProperty("set.lesson.group"), groupId, lessonId);
    }

    public Group getLessonGroup (int lessonId) {
        return jdbcTemplate.queryForObject(environment.getProperty("get.lesson.group"), new GroupMapper(), lessonId);
    }

    public void setLessonTime (int lessonTimeId, int lessonId) {
        jdbcTemplate.update(environment.getProperty("set.lesson.time"), lessonTimeId, lessonId);
    }

    public LessonTime getLessonTime (int lessonId) {
        return jdbcTemplate.queryForObject(environment.getProperty("get.lesson.time"), new LessonTimeMapper(), lessonId);
    }

    public List<Lesson> getGroupDayLessons(int groupId, DayOfWeek weekDay) {
        return jdbcTemplate.query(environment.getProperty("get.day.lessons.for.group"), new LessonMapper(),
                groupId, Integer.toString(weekDay.getValue()));
    }

    public List<Lesson> getLecturerDayLessons(int lecturerId, DayOfWeek weekDay) {
        return jdbcTemplate.query(environment.getProperty("get.day.lessons.for.lecturer"), new LessonMapper(),
                lecturerId, Integer.toString(weekDay.getValue()));
    }
}