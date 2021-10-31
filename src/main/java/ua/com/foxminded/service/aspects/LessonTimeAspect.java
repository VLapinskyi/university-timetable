package ua.com.foxminded.service.aspects;

import java.util.Set;
import java.util.StringJoiner;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import ua.com.foxminded.domain.LessonTime;
import ua.com.foxminded.service.exceptions.ServiceException;

@Aspect
@Configuration
@Order(10)
public class LessonTimeAspect {
    private final Logger logger = LoggerFactory.getLogger(LessonTimeAspect.class);

    private Validator validator;

    @Autowired
    public LessonTimeAspect(Validator validator) {
        this.validator = validator;
    }

    @Pointcut("execution (void ua.com.foxminded.service.LessonTimeService.create(ua.com.foxminded.domain.LessonTime))")
    private void createMethod() {
    }

    @Pointcut("execution (void ua.com.foxminded.service.LessonTimeService.update(ua.com.foxminded.domain.LessonTime))")
    private void updateMethod() {
    }

    @Before("createMethod()")
    void beforeCreateAdvice(JoinPoint joinPoint) {
        LessonTime lessonTime = (LessonTime) joinPoint.getArgs()[0];

        if (logger.isDebugEnabled()) {
            logger.debug("Try to create a lessonTime: {}.", lessonTime);
        }

        try {
            if (lessonTime == null) {
                IllegalArgumentException exception = new IllegalArgumentException(
                        "A lessonTime can't be null when create.");
                logger.error("A lessonTime {} can't be null when create.", lessonTime, exception);
                throw exception;
            }

            Set<ConstraintViolation<LessonTime>> violations = validator.validate(lessonTime);

            if (!violations.isEmpty()) {
                StringJoiner errorMessages = new StringJoiner("; ");

                for (ConstraintViolation<LessonTime> violation : violations) {
                    errorMessages.add(violation.getMessage());
                }

                ConstraintViolationException exception = new ConstraintViolationException(
                        "When create a lessonTime is not valid: " + errorMessages, violations);
                logger.error("The lessonTime {} is not valid when create. There are errors: {}.", lessonTime,
                        errorMessages, exception);
                throw exception;
            }

            int lessonTimeId = lessonTime.getId();

            if (lessonTimeId != 0) {
                IllegalArgumentException exception = new IllegalArgumentException(
                        "A LessonTime id isn't 0 when create.");
                logger.error("The lessonTime {} has wrong id {} which is not zero when create.", lessonTime,
                        lessonTimeId, exception);
                throw exception;
            }
        } catch (IllegalArgumentException illegalArgumentException) {
            throw new ServiceException("A given lessonTime isn't legal when create.", illegalArgumentException);
        } catch (ConstraintViolationException constraintViolationException) {
            throw new ServiceException("A given lessonTime isn't valid when create.", constraintViolationException);
        }
    }

    @Before("updateMethod()")
    void beforeUpdateAdvice(JoinPoint joinPoint) {
        LessonTime lessonTime = (LessonTime) joinPoint.getArgs()[0];

        if (logger.isDebugEnabled()) {
            logger.debug("Try to update a lessonTime: {}.", lessonTime);
        }

        try {
            if (lessonTime == null) {
                IllegalArgumentException exception = new IllegalArgumentException("An updated lessonTime is null.");
                logger.error("An updated lessonTime {} is null.", lessonTime, exception);
                throw exception;
            }

            Set<ConstraintViolation<LessonTime>> violations = validator.validate(lessonTime);

            if (!violations.isEmpty()) {
                StringJoiner errorMessages = new StringJoiner("; ");

                for (ConstraintViolation<LessonTime> violation : violations) {
                    errorMessages.add(violation.getMessage());
                }

                ConstraintViolationException exception = new ConstraintViolationException(
                        "When update the lessonTime is not valid: " + errorMessages, violations);
                logger.error("The lessonTime {} is not valid when update. There are errors: {}.", lessonTime,
                        errorMessages, exception);
                throw exception;
            }

            int lessonTimeId = lessonTime.getId();

            if (lessonTimeId < 1) {
                IllegalArgumentException exception = new IllegalArgumentException(
                        "A lessonTime id isn't positive for existing object.");
                logger.error("An updated lessonTime {} has wrong id {} which is not positive.", lessonTime,
                        lessonTimeId, exception);
                throw exception;
            }
        } catch (IllegalArgumentException illegalArgumentException) {
            throw new ServiceException("A given lessonTime isn't legal when update.", illegalArgumentException);
        } catch (ConstraintViolationException constraintViolationException) {
            throw new ServiceException("A given lessonTime isn't valid when update.", constraintViolationException);
        }
    }
}