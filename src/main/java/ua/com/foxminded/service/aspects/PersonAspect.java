package ua.com.foxminded.service.aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import ua.com.foxminded.domain.Person;
import ua.com.foxminded.service.exceptions.ServiceException;

@Aspect
@Configuration
@Order(10)
public class PersonAspect {
    private final Logger logger = LoggerFactory.getLogger(PersonAspect.class);

    @Pointcut("execution (void ua.com.foxminded.service.LecturerService.create(ua.com.foxminded.domain.Lecturer))")
    private void createLecturerMethod() {
    }

    @Pointcut("execution (void ua.com.foxminded.service.StudentService.create(ua.com.foxminded.domain.Student))")
    private void createStudentMethod() {
    }

    @Pointcut("createLecturerMethod() || createStudentMethod()")
    private void createStudentAndLecturerMethods() {
    }

    @Pointcut("execution (void ua.com.foxminded.service.LecturerService.update(ua.com.foxminded.domain.Lecturer))")
    private void updateLecturerMethod() {
    }

    @Pointcut("execution (void ua.com.foxminded.service.StudentService.update(ua.com.foxminded.domain.Student))")
    private void updateStudentMethod() {
    }

    @Pointcut("updateLecturerMethod() || updateStudentMethod()")
    private void updateStudentAndLecturerMethods() {
    }

    @Before("createStudentAndLecturerMethods()")
    void beforeCreateAdvice(JoinPoint joinPoint) {
        Person person = (Person) joinPoint.getArgs()[0];

        if (logger.isDebugEnabled()) {
            logger.debug("Try to create a new person: {}.", person);
        }

        try {
            if (person == null) {
                IllegalArgumentException exception = new IllegalArgumentException(
                        "A person can't be null when create.");
                logger.error("A person {} can't be null when create.", person, exception);
                throw exception;
            }

            int personId = person.getId();

            if (personId != 0) {
                IllegalArgumentException exception = new IllegalArgumentException("A person id isn't 0 when create.");
                logger.error("A person {} has wrong id {} which is not equal zero when create.", person, personId,
                        exception);
                throw exception;
            }
        } catch (IllegalArgumentException illegalArgumentException) {
            throw new ServiceException("A given person isn't legal when create.", illegalArgumentException);
        }
    }

    @Before("updateStudentAndLecturerMethods()")
    void beforeUpdateStudentAndLecturerAdvice(JoinPoint joinPoint) {
        Person person = (Person) joinPoint.getArgs()[0];
        if (logger.isDebugEnabled()) {
            logger.debug("Try to update a person: {}.", person);
        }

        try {
            if (person == null) {
                IllegalArgumentException exception = new IllegalArgumentException("An updated person is null.");
                logger.error("An updated person {} is null.", person, exception);
                throw exception;
            }

            int personId = person.getId();

            if (personId < 1) {
                IllegalArgumentException exception = new IllegalArgumentException(
                        "A person id isn't positive for existing object.");
                logger.error("An updated person {} has wrong id {} which is not positive.", person, personId,
                        exception);
                throw exception;
            }
        } catch (IllegalArgumentException illegalArgumentException) {
            throw new ServiceException("A given person isn't legal when update.", illegalArgumentException);
        }
    }

}
