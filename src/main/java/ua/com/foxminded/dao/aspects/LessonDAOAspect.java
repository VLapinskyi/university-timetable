package ua.com.foxminded.dao.aspects;

import java.time.DayOfWeek;
import java.util.List;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;

import ua.com.foxminded.dao.exceptions.DAOException;
import ua.com.foxminded.domain.Group;
import ua.com.foxminded.domain.Lecturer;
import ua.com.foxminded.domain.LessonTime;

@Aspect
@Configuration
public class LessonDAOAspect {
    private static final Logger LOGGER = LoggerFactory.getLogger(LessonDAOAspect.class);

    @Pointcut("execution (void ua.com.foxminded.dao.LessonDAO.setLessonLecturer(int, int))")
    private void setLessonLecturerMethod() {
    }

    @Pointcut("execution (ua.com.foxminded.domain.Lecturer ua.com.foxminded.dao.LessonDAO.getLessonLecturer (int))")
    private void getLessonLecturerMethod() {
    }

    @Pointcut("execution (void ua.com.foxminded.dao.LessonDAO.setLessonGroup(int, int))")
    private void setLessonGroupMethod() {
    }

    @Pointcut("execution (ua.com.foxminded.domain.Group ua.com.foxminded.dao.LessonDAO.getLessonGroup(int))")
    private void getLessonGroupMethod() {
    }

    @Pointcut("execution (void ua.com.foxminded.dao.LessonDAO.setLessonTime(int, int))")
    private void setLessonTimeMethod() {
    }

    @Pointcut("execution (ua.com.foxminded.domain.LessonTime ua.com.foxminded.dao.LessonDAO.getLessonTime(int))")
    private void getLessonTimeMethod() {
    }

    @Pointcut("execution (java.util.List ua.com.foxminded.dao.LessonDAO.getGroupDayLessons(int, java.time.DayOfWeek))")
    private void getGroupDayLessonsMethod() {
    }

    @Pointcut("execution (java.util.List ua.com.foxminded.dao.LessonDAO.getLecturerDayLessons(int, java.time.DayOfWeek))")
    private void getLecturerDayLessonsMethod() {
    }

    @Around("setLessonLecturerMethod()")
    void aroundSetLessonLecturerAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        int lecturerId = (int) proceedingJoinPoint.getArgs()[0];
        int lessonId = (int) proceedingJoinPoint.getArgs()[1];

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to set a lecturer with id {} for a lesson with id {}.", lecturerId, lessonId);
        }

        try {
            proceedingJoinPoint.proceed();

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("The lecturer with id {} was setted for the lesson with id {}.", lecturerId, lessonId);
            }
        } catch (DataAccessException dataAccessException) {
            LOGGER.error("Can't set a lecturer with id {} to a lesson with id {}.", lecturerId, lessonId,
                    dataAccessException);
            throw new DAOException("Can't set a lecturer to a lesson", dataAccessException);
        }
    }

    @Around("getLessonLecturerMethod()")
    Lecturer aroundGetLessonLecturerAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        int lessonId = (int) proceedingJoinPoint.getArgs()[0];

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to get a lecturer for a lesson with id {}.", lessonId);
        }

        try {
            Lecturer targetMethod = (Lecturer) proceedingJoinPoint.proceed();

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("The result lecturer for the lesson with id {} is {}.", lessonId, targetMethod);
            }
            return targetMethod;
        } catch (EmptyResultDataAccessException emptyResultDataAccessException) {
            LOGGER.error("There is no a lecturer for a lesson with id {}.", lessonId, emptyResultDataAccessException);
            throw new DAOException("There is no a lecturer for a lesson", emptyResultDataAccessException);
        } catch (DataAccessException dataAccessException) {
            LOGGER.error("Can't get a lecturer for a lesson with id {}.", lessonId, dataAccessException);
            throw new DAOException("Can't get a lecturer for a lesson.", dataAccessException);
        }
    }

    @Around("setLessonGroupMethod()")
    void aroundSetLessonGroupAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        int groupId = (int) proceedingJoinPoint.getArgs()[0];
        int lessonId = (int) proceedingJoinPoint.getArgs()[1];

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to assign a lesson with id {} to a group with id {}.", lessonId, groupId);
        }

        try {
            proceedingJoinPoint.proceed();

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("The lesson with id {} was assigned to the group with id {}.", lessonId, groupId);
            }
        } catch (DataAccessException dataAccessException) {
            LOGGER.error("Can't set a lesson with id {} to a group with id {}.", lessonId, groupId,
                    dataAccessException);
            throw new DAOException("Can't set a lesson to a group.", dataAccessException);
        }
    }

    @Around("getLessonGroupMethod()")
    Group aroundGetLessonGroupAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        int lessonId = (int) proceedingJoinPoint.getArgs()[0];

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to get a group which was assigned for a lesson with id {}.", lessonId);
        }

        try {
            Group targetMethod = (Group) proceedingJoinPoint.proceed();

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("The result group for the lesson with id {} is {}.", lessonId, targetMethod);
            }
            return targetMethod;
        } catch (EmptyResultDataAccessException emptyResultDataAccessException) {
            LOGGER.error("There is no a group from a lesson with id {}.", lessonId, emptyResultDataAccessException);
            throw new DAOException("There is no a group from a lesson .", emptyResultDataAccessException);
        } catch (DataAccessException dataAccessException) {
            LOGGER.error("Can't get a group from a lesson with id {}.", lessonId);
            throw new DAOException("Can't get a group from a lesson", dataAccessException);
        }
    }

    @Around("setLessonTimeMethod()")
    void aroundSetLessonTimeAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        int lessonTimeId = (int) proceedingJoinPoint.getArgs()[0];
        int lessonId = (int) proceedingJoinPoint.getArgs()[1];

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to set lessonTime with id {} for a lesson with id {}.", lessonTimeId, lessonId);
        }

        try {
            proceedingJoinPoint.proceed();

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("The lessonTime with id {} was setted for the lesson with id {}.", lessonTimeId, lessonId);
            }
        } catch (DataAccessException dataAccessException) {
            LOGGER.error("Can't set lessonTime with id {} for a lesson with id {}.", lessonTimeId, lessonId);
            throw new DAOException("Can't set lessonTime for a lesson.", dataAccessException);
        }
    }

    @Around("getLessonTimeMethod()")
    LessonTime aroundGetLessonTimeAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        int lessonId = (int) proceedingJoinPoint.getArgs()[0];

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to get lessonTime for a lesson with id {}.", lessonId);
        }

        try {
            LessonTime targetMethod = (LessonTime) proceedingJoinPoint.proceed();

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("The result lessonTime for the lesson with id {} is {}.", lessonId, targetMethod);
            }

            return targetMethod;
        } catch (EmptyResultDataAccessException emptyResultDataAccessException) {
            LOGGER.error("There is no lessonTime for a lesson with id {}.", lessonId, emptyResultDataAccessException);
            throw new DAOException("There is no lessonTime for a lesson", emptyResultDataAccessException);
        } catch (DataAccessException dataAccessException) {
            LOGGER.error("Can't get lessonTime for a lesson with id {}.", lessonId, dataAccessException);
            throw new DAOException("Can't get lessonTime for a lesson.", dataAccessException);
        }
    }

    @Around("getGroupDayLessonsMethod()")
    Object aroundGetGroupDayLessonsAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        int groupId = (int) proceedingJoinPoint.getArgs()[0];
        DayOfWeek weekDay = (DayOfWeek) proceedingJoinPoint.getArgs()[1];

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to get all lessons for a group with id {} which is on a day {}.", groupId, weekDay);
        }

        try {
            Object targetMethod = proceedingJoinPoint.proceed();

            if (targetMethod instanceof List<?>) {
                if (((List<?>) targetMethod).isEmpty()) {
                    LOGGER.warn("There are not any lesson for the group with id {} on a day {}.", groupId, weekDay);
                } else {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("For the group with id {} on a day {} there are lessons: {}.", groupId, weekDay,
                                targetMethod);
                    }
                }
            }

            return targetMethod;

        } catch (DataAccessException dataAccessException) {
            LOGGER.error("Can't get lessons for a group with id {} on a day {}.", groupId, weekDay,
                    dataAccessException);
            throw new DAOException("Can't get day lessons for a group.", dataAccessException);
        }
    }

    @Around("getLecturerDayLessonsMethod()")
    Object aroundGetLecturerDayLessonsAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        int lecturerId = (int) proceedingJoinPoint.getArgs()[0];
        DayOfWeek weekDay = (DayOfWeek) proceedingJoinPoint.getArgs()[1];

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to get all lessons for a lecturer with id {} on a day {}.", lecturerId, weekDay);
        }

        try {
            Object targetMethod = proceedingJoinPoint.proceed();

            if (targetMethod instanceof List<?>) {

                if (((List<?>) targetMethod).isEmpty()) {
                    LOGGER.warn("There are not any lesson for the lecturer with id {} on a day {}.", lecturerId,
                            weekDay);
                } else {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("For the lecturer with id {} on a day {} there are lessons: {}.", lecturerId,
                                weekDay, targetMethod);
                    }
                }
            }

            return targetMethod;
        } catch (DataAccessException dataAccessException) {
            LOGGER.error("Can't get lessons for a lecturer with id {} on a day {}.", lecturerId, weekDay,
                    dataAccessException);
            throw new DAOException("Can't get day lessons for a lecturer.", dataAccessException);
        }
    }
}