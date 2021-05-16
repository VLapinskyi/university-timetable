package ua.com.foxminded.dao.aspects;

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

@Aspect
@Configuration
public class StudentDAOAspect {
    private static final Logger LOGGER = LoggerFactory.getLogger(StudentDAOAspect.class);

    @Pointcut("execution (void ua.com.foxminded.dao.StudentDAO.setStudentGroup(int, int))")
    private void setStudentGroupMethod() {
    }

    @Pointcut ("execution (ua.com.foxminded.domain.Group ua.com.foxminded.dao.StudentDAO.getStudentGroup(int))")
    private void getStudentGroupMethod() {
    }

    @Around("setStudentGroupMethod()")
    void aroundSetStudentGroupAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        int groupId = (int) proceedingJoinPoint.getArgs()[0];
        int studentId = (int) proceedingJoinPoint.getArgs()[1];

        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to set a group with id {} for a student with id {}.", groupId, studentId);
        }

        try {
            proceedingJoinPoint.proceed();

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("The group with id {} was setted for the student with id {}.", groupId, studentId);
            }
        } catch (DataAccessException dataAccessException) {
            LOGGER.error("Can't set a group with id {} for a student with id {}.", groupId, studentId, dataAccessException);
            throw new DAOException("Can't set a group for a student.", dataAccessException);
        }
    }

    @Around("getStudentGroupMethod()")
    Group aroundGetStudentGroupAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        int studentId = (int) proceedingJoinPoint.getArgs()[0];

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to get a group for a student with id {}.", studentId);
        }

        try {
            Group targetMethod = (Group) proceedingJoinPoint.proceed();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("The result group for the student with id {} is {}.", studentId, targetMethod);
            }

            return targetMethod;
        } catch (EmptyResultDataAccessException emptyResultDataAccessException) {
            LOGGER.error("There is no result when find a group for a student by student id {}.", studentId, emptyResultDataAccessException);
            throw new DAOException("There is no result when find a group for a student by student id ", emptyResultDataAccessException);
        } catch (DataAccessException dataAccessException) {
            LOGGER.error("Can't get a group for a student with id {}.", studentId, dataAccessException);
            throw new DAOException("Can't get a group for a student by id.", dataAccessException);
        }
    }
}