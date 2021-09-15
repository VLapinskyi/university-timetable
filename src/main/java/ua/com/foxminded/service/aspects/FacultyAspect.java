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

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import ua.com.foxminded.domain.Faculty;
import ua.com.foxminded.service.exceptions.ServiceException;

@Aspect
@Configuration
public class FacultyAspect {
    private static final Logger LOGGER = LoggerFactory.getLogger(FacultyAspect.class);

    private Validator validator;

    @Autowired
    public FacultyAspect(Validator validator) {
        this.validator = validator;
    }

    @Pointcut("execution (void ua.com.foxminded.service.FacultyService.create(ua.com.foxminded.domain.Faculty))")
    private void createMethod() {
    }

    @Pointcut("execution (void ua.com.foxminded.service.FacultyService.update(ua.com.foxminded.domain.Faculty))")
    private void updateMethod() {
    }

    @Before("createMethod()")
    void beforeCreateAdvice(JoinPoint joinPoint) {
        Faculty faculty = (Faculty) joinPoint.getArgs()[0];

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to create a new faculty: {}.", faculty);
        }

        try {
            if (faculty == null) {
                IllegalArgumentException exception = new IllegalArgumentException(
                        "A faculty can't be null when create.");
                LOGGER.error("A faculty {} can't be null when create.", faculty, exception);
                throw exception;
            }

            Set<ConstraintViolation<Faculty>> violations = validator.validate(faculty);

            if (!violations.isEmpty()) {
                StringJoiner errorMessages = new StringJoiner("; ");

                for (ConstraintViolation<Faculty> violation : violations) {
                    errorMessages.add(violation.getMessage());
                }

                ConstraintViolationException exception = new ConstraintViolationException(
                        "When create the faculty is not valid: " + errorMessages, violations);
                LOGGER.error("The faculty {} is not valid when create. There are errors: {}.", faculty, errorMessages,
                        exception);
                throw exception;
            }

            int facultyId = faculty.getId();

            if (facultyId != 0) {
                IllegalArgumentException exception = new IllegalArgumentException("A faculty id isn't 0 when create.");
                LOGGER.error("A faculty {} has wrong id {} which is not equal zero when create.", faculty, facultyId,
                        exception);
                throw exception;
            }
        } catch (IllegalArgumentException illegalArgumentException) {
            throw new ServiceException("A given faculty isn't legal when create.", illegalArgumentException);
        } catch (ConstraintViolationException constraintViolationException) {
            throw new ServiceException("A given faculty isn't valid when create.", constraintViolationException);
        }
    }

    @Before("updateMethod()")
    void beforeUpdateAdvice(JoinPoint joinPoint) {
        Faculty faculty = (Faculty) joinPoint.getArgs()[0];

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to update a faculty: {}.", faculty);
        }

        try {
            if (faculty == null) {
                IllegalArgumentException exception = new IllegalArgumentException("An updated faculty is null.");
                LOGGER.error("An updated faculty {} is null.", faculty, exception);
                throw exception;
            }

            Set<ConstraintViolation<Faculty>> violations = validator.validate(faculty);

            if (!violations.isEmpty()) {
                StringJoiner errorMessages = new StringJoiner("; ");

                for (ConstraintViolation<Faculty> violation : violations) {
                    errorMessages.add(violation.getMessage());
                }

                ConstraintViolationException exception = new ConstraintViolationException(
                        "When update the faculty is not valid: " + errorMessages, violations);
                LOGGER.error("The faculty {} is not valid when update. There are errors: {}.", faculty, errorMessages,
                        exception);
                throw exception;
            }

            int facultyId = faculty.getId();

            if (facultyId < 1) {
                IllegalArgumentException exception = new IllegalArgumentException(
                        "A faculty id isn't positive for existing object.");
                LOGGER.error("An updated faculty {} has wrong id {} which is not positive.", faculty, facultyId,
                        exception);
                throw exception;
            }
        } catch (IllegalArgumentException illegalArgumentException) {
            throw new ServiceException("A given faculty isn't legal when update.", illegalArgumentException);
        } catch (ConstraintViolationException constraintViolationException) {
            throw new ServiceException("A given faculty isn't valid when update.", constraintViolationException);
        }
    }
}