package ua.com.foxminded.dao;

import java.time.DayOfWeek;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(LessonDAO.class);
    private JdbcTemplate jdbcTemplate;
    private Environment environment;

    @Autowired
    public LessonDAO (JdbcTemplate jdbcTemplate, Environment environment) {
        this.jdbcTemplate = jdbcTemplate;
        this.environment = environment;
    }

    @Override
    public void create(Lesson lesson) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to create lesson: {}.", lesson);
        }
        
        jdbcTemplate.update(environment.getProperty("create.lesson"), lesson.getName(), lesson.getAudience(),
                lesson.getDay().getValue());
        
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("The lesson {} was inserted.", lesson);
        }
    }

    @Override
    public List<Lesson> findAll() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to find all lessons.");
        }
        
        List<Lesson> resultLessons = jdbcTemplate.query(environment.getProperty("find.all.lessons"), new LessonMapper());
        
        if (resultLessons.isEmpty()) {
            LOGGER.warn("There are not any lessons in the result.");
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("The result is: {}.", resultLessons);
            }
        }
        
        return resultLessons;
    }

    @Override
    public Lesson findById(int id) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to find lesson by id {}.", id);
        }
        Lesson resultLesson = jdbcTemplate.queryForStream(environment.getProperty("find.lesson.by.id"), new LessonMapper(), id)
                .findAny().orElse(null);
        
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("The result lesson with id {} is {}.", id, resultLesson);
        }
                
        return resultLesson;
    }

    @Override
    public void update(int id, Lesson lesson) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to update lesson {} with id {}.", lesson, id);
        }
        
        jdbcTemplate.update(environment.getProperty("update.lesson"), lesson.getName(),
                lesson.getAudience(), lesson.getDay().getValue(), id);
        
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("The lesson {} with id {} was changed.", lesson, id);
        }
    }

    @Override
    public void deleteById(int id) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to delete lesson by id {}.", id);
        }
        
        jdbcTemplate.update(environment.getProperty("delete.lesson"), id);
        
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("The lesson with id {} was deleted.", id);
        }
    }

    public void setLessonLecturer (int lecturerId, int lessonId) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to set lecturer with id {} for lesson with id {}.", lecturerId, lessonId);
        }
        
        jdbcTemplate.update(environment.getProperty("set.lesson.lecturer"), lecturerId, lessonId);
        
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("The lecturer with id {} was setted for lesson with id {}.", lecturerId, lessonId);
        }
    }

    public Lecturer getLessonLecturer (int lessonId) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to get lecturer for lesson with id {}.", lessonId);
        }
        
        Lecturer resultLecturer = jdbcTemplate.queryForStream(environment.getProperty("get.lesson.lecturer"), new LecturerMapper(), lessonId).findFirst().get();
        
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("The result lecturer for lesson with id {} is {}.", lessonId, resultLecturer);
        }
        
        return resultLecturer;
    }

    public void setLessonGroup (int groupId, int lessonId) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to assign lesson with id {} to group with id {}.", lessonId, groupId);
        }
        
        jdbcTemplate.update(environment.getProperty("set.lesson.group"), groupId, lessonId);
        
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("The lesson with id {} was assigned to group with id {}.", lessonId, groupId);
        }
    }

    public Group getLessonGroup (int lessonId) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to get group which was assigned for lesson with id {}.", lessonId);
        }
        Group resultGroup = jdbcTemplate.queryForStream(environment.getProperty("get.lesson.group"), new GroupMapper(), lessonId).findFirst().get();
        
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("The result group for lesson with id {} is {}.", lessonId, resultGroup);
        }
        
        return resultGroup;
    }

    public void setLessonTime (int lessonTimeId, int lessonId) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to set lessonTime with id {} for lesson with id {}.", lessonTimeId, lessonId);
        }
        
        jdbcTemplate.update(environment.getProperty("set.lesson.time"), lessonTimeId, lessonId);
        
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("The lessonTime with id {} was setted for lesson with id {}.", lessonTimeId, lessonId);
        }
    }

    public LessonTime getLessonTime (int lessonId) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to get lessonTime for lesson with id {}.", lessonId);
        }
        
        LessonTime resultLessonTime = jdbcTemplate.queryForStream(environment.getProperty("get.lesson.time"), new LessonTimeMapper(), lessonId)
                .findFirst().get();
        
        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("The result lessonTime for lesson with id {} is {}.", lessonId, resultLessonTime);
        }
        
        return resultLessonTime;
    }

    public List<Lesson> getGroupDayLessons(int groupId, DayOfWeek weekDay) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to get all lessons for group with id {} which is on a day {}.", groupId, weekDay);
        }
        
        List<Lesson> resultGroupDayLessons = jdbcTemplate.query(environment.getProperty("get.day.lessons.for.group"), new LessonMapper(),
                groupId, weekDay.getValue());
        
        if (resultGroupDayLessons.isEmpty()) {
            LOGGER.warn("There are not any lessons for group with id {} on a day {}.", groupId, weekDay);
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("For group with id {} on a day {} there are lessons: {}.", groupId, weekDay, resultGroupDayLessons);
            }
        }
        
        return resultGroupDayLessons;
    }

    public List<Lesson> getLecturerDayLessons(int lecturerId, DayOfWeek weekDay) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to get all lessons for lecturer with id {} on a day {}.", lecturerId, weekDay);
        }
        
        List<Lesson> resultLecturerLessons = jdbcTemplate.query(environment.getProperty("get.day.lessons.for.lecturer"), new LessonMapper(),
                lecturerId, weekDay.getValue());
        
        if (resultLecturerLessons.isEmpty()) {
            LOGGER.warn("There are not any lessons for lecturer with id {} on a day {}.", lecturerId, weekDay);
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("For lecturer with id {} on a day {} there are lessons: {}.", lecturerId, weekDay, resultLecturerLessons);
            }
        }
        
        return resultLecturerLessons;
    }
}
