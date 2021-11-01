package ua.com.foxminded.service.aspects;

import java.util.List;
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
import org.springframework.core.annotation.Order;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import ua.com.foxminded.domain.Student;
import ua.com.foxminded.repositories.exceptions.RepositoryException;
import ua.com.foxminded.service.exceptions.ServiceException;

@Aspect
@Configuration
@Order(20)
public class StudentAspect {
    private final Logger logger = LoggerFactory.getLogger(StudentAspect.class);

    private Validator validator;

    @Autowired
    public StudentAspect(Validator validator) {
        this.validator = validator;
    }

    @Pointcut("execution (void ua.com.foxminded.service.StudentService.create(ua.com.foxminded.domain.Student))")
    private void createMethod() {
    }

    @Pointcut("execution (void ua.com.foxminded.service.StudentService.update(ua.com.foxminded.domain.Student))")
    private void updateMethod() {
    }

    @Pointcut("execution (java.util.List ua.com.foxminded.service.StudentService.getStudentsFromGroup(int))")
    private void getStudentsFromGroupMethod() {
    }

    @Before("createMethod()")
    void beforeCreateAdvice(JoinPoint joinPoint) {
        Student student = (Student) joinPoint.getArgs()[0];

        try {

            Set<ConstraintViolation<Student>> violations = validator.validate(student);

            if (!violations.isEmpty()) {
                StringJoiner errorMessages = new StringJoiner("; ");

                for (ConstraintViolation<Student> violation : violations) {
                    errorMessages.add(violation.getMessage());
                }

                ConstraintViolationException exception = new ConstraintViolationException(
                        "When create the student is not valid: " + errorMessages, violations);
                logger.error("The student {} is not valid when create. There are errors: {}.", student, errorMessages,
                        exception);
                throw exception;
            }
            
            if (student.getGroup() == null) {
                NullPointerException nullPointerException = new NullPointerException("Student group can't be null.");
                logger.error("The student {} can't have group which is null", nullPointerException);
                throw nullPointerException;
            }
        } catch (ConstraintViolationException constraintViolationException) {
            throw new ServiceException("A given student isn't valid when create.", constraintViolationException);
        } catch (NullPointerException nullPointerException) {
            throw new ServiceException("A given student is wrong.", nullPointerException);
        }
    }

    @Before("updateMethod()")
    void beforeUpdateAdvice(JoinPoint joinPoint) {
        Student student = (Student) joinPoint.getArgs()[0];

        try {
            Set<ConstraintViolation<Student>> violations = validator.validate(student);

            if (!violations.isEmpty()) {
                StringJoiner errorMessages = new StringJoiner("; ");

                for (ConstraintViolation<Student> violation : violations) {
                    errorMessages.add(violation.getMessage());
                }

                ConstraintViolationException exception = new ConstraintViolationException(
                        "When update the student is not valid:" + errorMessages, violations);
                logger.error("The student {} is not valid when update. There are errors: {}.", student, errorMessages,
                        exception);
                throw exception;
            }
        } catch (ConstraintViolationException constraintViolationException) {
            throw new ServiceException("A given student isn't valid when update.", constraintViolationException);
        }
    }

    @Around("getStudentsFromGroupMethod()")
    Object aroundGetStudentsFromGroupAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        int groupId = (int) proceedingJoinPoint.getArgs()[0];
        if (logger.isDebugEnabled()) {
            logger.debug("Try to get students from a group by id: {}.", groupId);
        }

        try {
            if (groupId < 1) {
                IllegalArgumentException exception = new IllegalArgumentException(
                        "A given groupId is less than 1 when getStudentsFromGroup");
                logger.error("A given groupId {} is less than 1 when getStudentsFromGroup.", groupId, exception);
                throw new ServiceException("A given groupId is less than 1 when getStudentsFromGroup.", exception);
            }

            Object targetMethod = proceedingJoinPoint.proceed();
            if (targetMethod instanceof List<?>) {

                if (((List<?>) targetMethod).isEmpty()) {
                    logger.warn("There are not any students in a group with id {}.", groupId);
                } else {

                    if (logger.isDebugEnabled()) {
                        logger.debug("Students from group with id {} are: {}.", groupId, targetMethod);
                    }
                }
            }

            return targetMethod;
        } catch (RepositoryException repositoryException) {
            logger.error("There is some error in repositories layer when get students from a group by groupId {}.", groupId,
                    repositoryException);
            throw new ServiceException("There is some error in repositories layer when get students from a group.",
                    repositoryException);
        }
    }
}
