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
    
    public void setLessonLecturer (int lecturerId, int lessonId) {
	jdbcTemplate.update(environment.getProperty("set.lesson.lecturer"), lecturerId, lessonId);
    }
    
    public Lecturer getLessonLecturer (int lessonId) {
	return jdbcTemplate.queryForStream(environment.getProperty("get.lesson.lecturer"), new LecturerMapper(), lessonId).findFirst().get();
    }
    
    public void setLessonGroup (int groupId, int lessonId) {
	jdbcTemplate.update(environment.getProperty("set.lesson.group"), groupId, lessonId);
    }
    
    public Group getLessonGroup (int lessonId) {
	return jdbcTemplate.queryForStream(environment.getProperty("get.lesson.group"), new GroupMapper(), lessonId).findFirst().get();
    }
    
    public void setLessonTime (int lessonTimeId, int lessonId) {
	jdbcTemplate.update(environment.getProperty("set.lesson.time"), lessonTimeId, lessonId);
    }
    
    public LessonTime getLessonTime (int lessonId) {
	return jdbcTemplate.queryForStream(environment.getProperty("get.lesson.time"), new LessonTimeMapper(), lessonId)
		.findFirst().get();
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
