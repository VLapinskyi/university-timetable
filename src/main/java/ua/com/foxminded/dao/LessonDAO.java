package ua.com.foxminded.dao;

import java.time.DayOfWeek;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import ua.com.foxminded.dao.exceptions.DAOException;
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

        try {
            jdbcTemplate.update(environment.getProperty("create.lesson"), lesson.getName(), lesson.getAudience(),
                    lesson.getDay().getValue());

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("The lesson {} was inserted.", lesson);
            }
        } catch (DataAccessException dataAccessException) {
            LOGGER.error("Can't create lesson: {}.", lesson, dataAccessException);
            throw new DAOException("Can't create lesson", dataAccessException);
        }
    }

    @Override
    public List<Lesson> findAll() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to find all lessons.");
        }

        try {
            List<Lesson> resultLessons = jdbcTemplate.query(environment.getProperty("find.all.lessons"), new LessonMapper());

            if (resultLessons.isEmpty()) {
                LOGGER.warn("There are not any lessons in the result.");
            } else {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("The result is: {}.", resultLessons);
                }
            }

            return resultLessons;
        } catch (DataAccessException dataAccessException) {
            LOGGER.error("Can't find all lessons.", dataAccessException);
            throw new DAOException("Can't find all lessons.", dataAccessException);
        }
    }

    @Override
    public Lesson findById(int id) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to find lesson by id {}.", id);
        }

        try {
            Lesson resultLesson = jdbcTemplate.queryForObject(environment.getProperty("find.lesson.by.id"), new LessonMapper(), id);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("The result lesson with id {} is {}.", id, resultLesson);
            }

            return resultLesson;
        } catch (EmptyResultDataAccessException emptyResultDataAccessException) {
            LOGGER.error("There is no result when find by id {}.", id, emptyResultDataAccessException);
            throw new DAOException("There is no result when find by id.", emptyResultDataAccessException);
        } catch (DataAccessException dataAccessException) {
            LOGGER.error("Can't find lesson by id {}.", id, dataAccessException);
            throw new DAOException("Can't find lesson by id.", dataAccessException);
        }
    }

    @Override
    public void update(int id, Lesson lesson) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to update lesson {} with id {}.", lesson, id);
        }

        try {
            jdbcTemplate.update(environment.getProperty("update.lesson"), lesson.getName(),
                    lesson.getAudience(), lesson.getDay().getValue(), id);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("The lesson {} with id {} was changed.", lesson, id);
            }
        } catch (DataAccessException dataAccessException) {
            LOGGER.error("Can't update lesson {} with id {}.", lesson, id, dataAccessException);
            throw new DAOException("Can't update lesson.", dataAccessException);
        }
    }

    @Override
    public void deleteById(int id) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to delete lesson by id {}.", id);
        }

        try {
            jdbcTemplate.update(environment.getProperty("delete.lesson"), id);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("The lesson with id {} was deleted.", id);
            }
        } catch(DataAccessException dataAccessException) {
            LOGGER.error("Can't delete lesson by id {}.", id, dataAccessException);
            throw new DAOException("Can't delete lesson by id.", dataAccessException);
        }
    }

    public void setLessonLecturer (int lecturerId, int lessonId) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to set lecturer with id {} for lesson with id {}.", lecturerId, lessonId);
        }

        try {
            jdbcTemplate.update(environment.getProperty("set.lesson.lecturer"), lecturerId, lessonId);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("The lecturer with id {} was setted for lesson with id {}.", lecturerId, lessonId);
            }
        } catch(DataAccessException dataAccessException) {
            LOGGER.error("Can't set lecturer with id {} to lesson with id {}.", lecturerId, lessonId, dataAccessException);
            throw new DAOException("Can't set lecturer to lesson.", dataAccessException);
        }
    }

    public Lecturer getLessonLecturer (int lessonId) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to get lecturer for lesson with id {}.", lessonId);
        }

        try {
            Lecturer resultLecturer = jdbcTemplate.queryForObject(environment.getProperty("get.lesson.lecturer"), new LecturerMapper(), lessonId);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("The result lecturer for lesson with id {} is {}.", lessonId, resultLecturer);
            }

            return resultLecturer;
        } catch (EmptyResultDataAccessException emptyResultDataAccessException) {
            LOGGER.error("There is no a lecturer for lesson with id {}.", lessonId, emptyResultDataAccessException);
            throw new DAOException("There is no a lecturer for lesson", emptyResultDataAccessException);
        } catch (DataAccessException dataAccessException) {
            LOGGER.error("Can't get lecturer for lesson with id {}.", lessonId, dataAccessException);
            throw new DAOException("Can't get lecturer for lesson.", dataAccessException);
        }
    }

    public void setLessonGroup (int groupId, int lessonId) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to assign lesson with id {} to group with id {}.", lessonId, groupId);
        }

        try {
            jdbcTemplate.update(environment.getProperty("set.lesson.group"), groupId, lessonId);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("The lesson with id {} was assigned to group with id {}.", lessonId, groupId);
            }
        } catch (DataAccessException dataAccessException) {
            LOGGER.error("Can't set lesson with id {} to group with id {}.", lessonId, groupId, dataAccessException);
            throw new DAOException("Can't set lesson to group", dataAccessException);
        }
    }

    public Group getLessonGroup (int lessonId) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to get group which was assigned for lesson with id {}.", lessonId);
        }

        try {
            Group resultGroup = jdbcTemplate.queryForObject(environment.getProperty("get.lesson.group"), new GroupMapper(), lessonId);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("The result group for lesson with id {} is {}.", lessonId, resultGroup);
            }

            return resultGroup;
        } catch (EmptyResultDataAccessException emptyResultDataAccessException) {
            LOGGER.error("There is no group from lesson with id {}.", lessonId, emptyResultDataAccessException);
            throw new DAOException("", emptyResultDataAccessException);
        } catch (DataAccessException dataAccessException) {
            LOGGER.error("Can't get group from lesson with id {}.", lessonId, dataAccessException);
            throw new DAOException("Can't get group from lesson.", dataAccessException);
        }
    }

    public void setLessonTime (int lessonTimeId, int lessonId) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to set lessonTime with id {} for lesson with id {}.", lessonTimeId, lessonId);
        }
        try {
            jdbcTemplate.update(environment.getProperty("set.lesson.time"), lessonTimeId, lessonId);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("The lessonTime with id {} was setted for lesson with id {}.", lessonTimeId, lessonId);
            }
        } catch (DataAccessException dataAccessException) {
            LOGGER.error("Can't set lessonTime with id {} for lesson with id {}.", lessonTimeId, lessonId, dataAccessException);
            throw new DAOException("Can't set lessonTime for lesson.", dataAccessException);
        }
    }

    public LessonTime getLessonTime (int lessonId) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to get lessonTime for lesson with id {}.", lessonId);
        }

        try {
            LessonTime resultLessonTime = jdbcTemplate.queryForObject(environment.getProperty("get.lesson.time"), new LessonTimeMapper(), lessonId);

            if(LOGGER.isDebugEnabled()) {
                LOGGER.debug("The result lessonTime for lesson with id {} is {}.", lessonId, resultLessonTime);
            }

            return resultLessonTime;
        } catch (EmptyResultDataAccessException emptyResultDataAccessException) {
            LOGGER.error("There is no lessonTime for lesson with id {}.", lessonId, emptyResultDataAccessException);
            throw new DAOException("There is no lessonTime for lesson.", emptyResultDataAccessException);
        } catch (DataAccessException dataAccessException) {
            LOGGER.error("Can't get lessonTime for lesson with id {}.", lessonId, dataAccessException);
            throw new DAOException("Can't get lessonTime for lesson.", dataAccessException);
        }
    }

    public List<Lesson> getGroupDayLessons(int groupId, DayOfWeek weekDay) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to get all lessons for group with id {} which is on a day {}.", groupId, weekDay);
        }

        try {
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
        } catch (DataAccessException dataAccessException) {
            LOGGER.error("Can't get lessons for group with id {} on a day {}.", groupId, weekDay, dataAccessException);
            throw new DAOException("Can't get day lessons for group.", dataAccessException);
        }
    }

    public List<Lesson> getLecturerDayLessons(int lecturerId, DayOfWeek weekDay) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to get all lessons for lecturer with id {} on a day {}.", lecturerId, weekDay);
        }

        try {
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
        } catch (DataAccessException dataAccessException) {
            LOGGER.error("Can't get lessons for lecturer with id {} on a day {}.", lecturerId, weekDay, dataAccessException);
            throw new DAOException("Can't get lessons for lecturer.", dataAccessException);
        }
    }
}
