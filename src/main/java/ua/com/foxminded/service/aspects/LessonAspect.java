package ua.com.foxminded.service.aspects;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import ua.com.foxminded.dao.exceptions.DAOException;
import ua.com.foxminded.domain.Lesson;
import ua.com.foxminded.service.exceptions.ServiceException;

@Aspect
@Configuration
public class LessonAspect {
    private static final Logger LOGGER = LoggerFactory.getLogger(LessonAspect.class);

    private Validator validator;

    @Autowired
    public LessonAspect(Validator validator) {
        this.validator = validator;
    }

    @Pointcut("execution (void ua.com.foxminded.service.LessonService.create(ua.com.foxminded.domain.Lesson))")
    private void createMethod() {
    }

    @Pointcut("execution (void ua.com.foxminded.service.LessonService.update(ua.com.foxminded.domain.Lesson))")
    private void updateMethod() {
    }

    @Pointcut("execution (java.util.Map ua.com.foxminded.service.LessonService.getGroupWeekLessons(int))")
    private void getGroupWeekLessonsMethod() {
    }

    @Pointcut("execution (java.util.Map ua.com.foxminded.service.LessonService.getGroupMonthLessons(int, java.time.YearMonth))")
    private void getGroupMonthLessonsMethod() {
    }

    @Pointcut("execution (java.util.Map ua.com.foxminded.service.LessonService.getLecturerWeekLessons(int))")
    private void getLecturerWeekLessonsMethod() {
    }

    @Pointcut("execution (java.util.Map ua.com.foxminded.service.LessonService.getLecturerMonthLessons(int, java.time.YearMonth))")
    private void getLecturerMonthLessonsMethod() {
    }

    @Before("createMethod()")
    void beforeCreateAdvice(JoinPoint joinPoint) {
        Lesson lesson = (Lesson) joinPoint.getArgs()[0];

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to create a new lesson: {}.", lesson);
        }

        try {
            if (lesson == null) {
                IllegalArgumentException exception = new IllegalArgumentException("A lesson can't be null.");
                LOGGER.error("A lesson {} can't be null when create.", lesson, exception);
                throw exception;
            }

            Set<ConstraintViolation<Lesson>> violations = validator.validate(lesson);

            if (!violations.isEmpty()) {
                StringJoiner errorMessages = new StringJoiner("; ");

                for (ConstraintViolation<Lesson> violation : violations) {
                    errorMessages.add(violation.getMessage());
                }

                ConstraintViolationException exception = new ConstraintViolationException(
                        "When create the lesson is not valid: " + errorMessages, violations);
                LOGGER.error("The lesson {} is not valid when create. There are errors: {}.", lesson, errorMessages,
                        exception);
                throw exception;
            }

            int lessonId = lesson.getId();

            if (lessonId != 0) {
                IllegalArgumentException exception = new IllegalArgumentException("A lesson id isn't 0 when create.");
                LOGGER.error("A lesson {} has wrong id {} which is not equal zero when create.", lesson, lessonId,
                        exception);
                throw exception;
            }
        } catch (IllegalArgumentException illegalArgumentException) {
            throw new ServiceException("A given lesson isn't legal when create.", illegalArgumentException);
        } catch (ConstraintViolationException constraintViolationException) {
            throw new ServiceException("A given lesson isn't valid when create.", constraintViolationException);
        }
    }

    @Before("updateMethod()")
    void beforeUpdateAdvice(JoinPoint joinPoint) {
        Lesson lesson = (Lesson) joinPoint.getArgs()[0];

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to update a lesson: {}.", lesson);
        }

        try {
            if (lesson == null) {
                IllegalArgumentException exception = new IllegalArgumentException("An updated lesson is null.");
                LOGGER.error("An updated lesson {} is null.", lesson, exception);
                throw exception;
            }

            Set<ConstraintViolation<Lesson>> violations = validator.validate(lesson);

            if (!violations.isEmpty()) {
                StringJoiner errorMessages = new StringJoiner("; ");

                for (ConstraintViolation<Lesson> violation : violations) {
                    errorMessages.add(violation.getMessage());
                }

                ConstraintViolationException exception = new ConstraintViolationException(
                        "When update the lesson is not valid:" + errorMessages, violations);
                LOGGER.error("The lesson {} is not valid when update. There are errors: {}.", lesson, errorMessages,
                        exception);
                throw exception;
            }

            int lessonId = lesson.getId();

            if (lessonId < 1) {
                IllegalArgumentException exception = new IllegalArgumentException(
                        "A lesson id isn't positive for existing object.");
                LOGGER.error("An updated lesson {} has wrong id {} which is not positive.", lesson, lessonId,
                        exception);
                throw exception;
            }
        } catch (IllegalArgumentException illegalArgumentException) {
            throw new ServiceException("A given lesson isn't legal when update.", illegalArgumentException);
        } catch (ConstraintViolationException constraintViolationException) {
            throw new ServiceException("A given lesson isn't valid when update.", constraintViolationException);
        }
    }

    @Around("getGroupWeekLessonsMethod()")
    Object aroundGetGroupWeekLessonsAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        int groupId = (int) proceedingJoinPoint.getArgs()[0];

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to get week lessons for a group with id: {}.", groupId);
        }

        try {

            if (groupId < 1) {
                IllegalArgumentException exception = new IllegalArgumentException(
                        "A group id isn't positive for existing object.");
                LOGGER.error("A group id {} is not positive when get week lessons for a group.", groupId, exception);
                throw exception;
            }

            Object targetMethod = proceedingJoinPoint.proceed();
            if (targetMethod instanceof Map<?, ?>) {
                boolean isEmpty = false;

                for (Map.Entry<?, ?> entry : ((Map<?, ?>) targetMethod).entrySet()) {
                    if ((entry.getValue() instanceof List<?>)) {
                        isEmpty = ((List<?>) entry.getValue()).isEmpty();

                        if (!isEmpty) {
                            break;
                        }
                    }
                }

                if (isEmpty) {
                    LOGGER.warn("There are not any week lessons for a group with id {}.", groupId);
                } else {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("When get week lessons for a group with id {} the result is: {}.", groupId,
                                targetMethod);
                    }
                }
            }
            return targetMethod;
        } catch (IllegalArgumentException illegalArgumentException) {
            throw new ServiceException("There is an error with given number when get week lessons for a group.",
                    illegalArgumentException);
        } catch (DAOException daoException) {
            LOGGER.error("There is some error in dao layer when get week lessons for a group with id {}.", groupId,
                    daoException);
            throw new ServiceException("There is some error in dao layer when get week lessons for a group.",
                    daoException);
        }
    }

    @Around("getGroupMonthLessonsMethod()")
    Object aroundGetMonthLessonsAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        int groupId = (int) proceedingJoinPoint.getArgs()[0];
        YearMonth month = (YearMonth) proceedingJoinPoint.getArgs()[1];

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to get {} month of {} year lessons for a group with id: {}.", month.getMonth(),
                    month.getYear(), groupId);
        }

        try {

            if (groupId < 1) {
                IllegalArgumentException exception = new IllegalArgumentException(
                        "A group id isn't positive for existing object.");
                LOGGER.error("A group id {} is not positive when get {} month of {} year lessons for a group.", groupId,
                        month.getMonth(), month.getYear(), exception);
                throw exception;
            }

            Object targetMethod = proceedingJoinPoint.proceed();
            if (targetMethod instanceof Map<?, ?>) {
                boolean isEmpty = false;

                for (Map.Entry<?, ?> entry : ((Map<?, ?>) targetMethod).entrySet()) {
                    if ((entry.getValue() instanceof List<?>)) {
                        isEmpty = ((List<?>) entry.getValue()).isEmpty();

                        if (!isEmpty) {
                            break;
                        }
                    }
                }

                if (isEmpty) {
                    LOGGER.warn("There are not any {} month of {} year lessons for a group with id {}.",
                            month.getMonth(), month.getYear(), groupId);
                } else {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("When get {} month of {} year lessons for a group with id {} the result is: {}.",
                                month.getMonth(), month.getYear(), groupId, targetMethod);
                    }
                }
            }
            return targetMethod;
        } catch (IllegalArgumentException illegalArgumentException) {
            throw new ServiceException("There is an error with given number when get month lessons for a group.",
                    illegalArgumentException);
        } catch (DAOException daoException) {
            LOGGER.error(
                    "There is some error in dao layer when get {} month of {} year lessons for a group with id {}.",
                    month.getMonth(), month.getYear(), groupId, daoException);
            throw new ServiceException("There is some error in dao layer when get month lessons for a group.",
                    daoException);
        }
    }

    @Around("getLecturerWeekLessonsMethod()")
    Object aroundGetLecturerWeekLessonsAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        int lecturerId = (int) proceedingJoinPoint.getArgs()[0];

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to get week lessons for a lecturer with id: {}.", lecturerId);
        }

        try {

            if (lecturerId < 1) {
                IllegalArgumentException exception = new IllegalArgumentException(
                        "A lecturer id isn't positive for existing object.");
                LOGGER.error("A lecturer id {} is not positive when get week lessons for a lecturer.", lecturerId,
                        exception);
                throw exception;
            }

            Object targetMethod = proceedingJoinPoint.proceed();
            if (targetMethod instanceof Map<?, ?>) {
                boolean isEmpty = false;

                for (Map.Entry<?, ?> entry : ((Map<?, ?>) targetMethod).entrySet()) {
                    if ((entry.getValue() instanceof List<?>)) {
                        isEmpty = ((List<?>) entry.getValue()).isEmpty();
                        if (!isEmpty) {
                            break;
                        }
                    }
                }

                if (isEmpty) {
                    LOGGER.warn("There are not any week lessons for a lecturer with id {}.", lecturerId);
                } else {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("When get week lessons for a lecturer with id {} the result is: {}.", lecturerId,
                                targetMethod);
                    }
                }
            }
            return targetMethod;
        } catch (IllegalArgumentException illegalArgumentException) {
            throw new ServiceException("There is an error with given number when get week lessons for a lecturer.",
                    illegalArgumentException);
        } catch (DAOException daoException) {
            LOGGER.error("There is some error in dao layer when get week lessons for a lecturer with id {}.",
                    lecturerId, daoException);
            throw new ServiceException("There is some error in dao layer when get week lessons for a group.",
                    daoException);
        }
    }

    @Around("getLecturerMonthLessonsMethod()")
    Object aroundGetLecturerMonthLessonsAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        int lecturerId = (int) proceedingJoinPoint.getArgs()[0];
        YearMonth month = (YearMonth) proceedingJoinPoint.getArgs()[1];

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to get {} month of {} year lessons for a lecturer with id: {}.", month.getMonth(),
                    month.getYear(), lecturerId);
        }

        try {

            if (lecturerId < 1) {
                IllegalArgumentException exception = new IllegalArgumentException(
                        "A lecturer id isn't positive for existing object.");
                LOGGER.error("A lecturer id {} is not positive when get {} month of {} year lessons for a lecturer.",
                        lecturerId, month.getMonth(), month.getYear(), exception);
                throw exception;
            }

            Object targetMethod = proceedingJoinPoint.proceed();
            if (targetMethod instanceof Map<?, ?>) {
                boolean isEmpty = false;

                for (Map.Entry<?, ?> entry : ((Map<?, ?>) targetMethod).entrySet()) {
                    if ((entry.getValue() instanceof List<?>)) {
                        isEmpty = ((List<?>) entry.getValue()).isEmpty();

                        if (!isEmpty) {
                            break;
                        }
                    }
                }

                if (isEmpty) {
                    LOGGER.warn("There are not any {} month of {} year lessons for a lecturer with id {}.",
                            month.getMonth(), month.getYear(), lecturerId);
                } else {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug(
                                "When get {} month of {} year lessons for a lecturer with id {} the result is: {}.",
                                month.getMonth(), month.getYear(), lecturerId, targetMethod);
                    }
                }
            }
            return targetMethod;
        } catch (IllegalArgumentException illegalArgumentException) {
            throw new ServiceException("There is an error with given number when getting month lessons for a lecturer.",
                    illegalArgumentException);
        } catch (DAOException daoException) {
            LOGGER.error(
                    "There is some error in dao layer when get {} month of {} year lessons for a lecturer with id {}.",
                    month.getMonth(), month.getYear(), lecturerId, daoException);
            throw new ServiceException("There is some error in dao layer when get month lessons for a group.",
                    daoException);
        }
    }
}
