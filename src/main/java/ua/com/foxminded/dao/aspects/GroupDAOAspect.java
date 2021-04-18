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
import ua.com.foxminded.domain.Faculty;

@Aspect
@Configuration
public class GroupDAOAspect {
    private static final Logger LOGGER = LoggerFactory.getLogger(GroupDAOAspect.class);

    @Pointcut("execution (void ua.com.foxminded.dao.GroupDAO.setGroupFaculty (int, int))")
    private void setGroupFacultyMethod() {
    }
    
    @Pointcut("execution (ua.com.foxminded.domain.Faculty ua.com.foxminded.dao.GroupDAO.getGroupFaculty (int))")
    private void getGroupFacultyMethod() {
    }

    @Around("setGroupFacultyMethod()")
    void aroundSetGroupFacultyAdvice (ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        int facultyId = (int) proceedingJoinPoint.getArgs()[0];
        int groupId = (int) proceedingJoinPoint.getArgs()[1];

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to set a faculty with id {} for a group with id {}.", facultyId, groupId);
        }

        try {
            proceedingJoinPoint.proceed();

            if(LOGGER.isDebugEnabled()) {
                LOGGER.debug("The faculty with id {} was setted for the group with id {}.", 
                        facultyId, groupId);
            }
        } catch (DataAccessException dataAccessException) {
            LOGGER.error("Can't set a faculty with id {} for a group with id {}.", facultyId, groupId, dataAccessException);
            throw new DAOException("Can't set a faculty for a group.", dataAccessException);
        }
    }
    
    @Around("getGroupFacultyMethod()")
    Faculty aroundGetGroupFacultyAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        int groupId = (int) proceedingJoinPoint.getArgs()[0];
        
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to get a faculty for a group with id {}.", groupId);
        }
        
        try {
            Faculty targetMethod = (Faculty) proceedingJoinPoint.proceed();
            
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("The result faculty for the group with id {} is {}.", groupId, targetMethod);
            }
            return targetMethod;
        } catch (EmptyResultDataAccessException emptyResultDataAccessException) {
            LOGGER.error("There is no a faculty for a group with id {}.", groupId, emptyResultDataAccessException);
            throw new DAOException("There is no a faculty for a group by groupId.", emptyResultDataAccessException);
        } catch (DataAccessException dataAccessException) {
            LOGGER.error("Can't get a faculty for a group with id {}.", groupId, dataAccessException);
            throw new DAOException("Can't get a faculty for a group by groupId.", dataAccessException);
        }
    }
}