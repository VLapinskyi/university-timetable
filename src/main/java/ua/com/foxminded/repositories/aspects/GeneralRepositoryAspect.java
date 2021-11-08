package ua.com.foxminded.repositories.aspects;

import java.util.List;
import java.util.Optional;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;

import ua.com.foxminded.repositories.exceptions.RepositoryException;

@Aspect
@Configuration
public class GeneralRepositoryAspect {
    private final Logger logger = LoggerFactory.getLogger(GeneralRepositoryAspect.class);

    @Pointcut("execution (* ua.com.foxminded.repositories.interfaces.*.save(*))")
    private void saveMethods() {
    }

    @Pointcut("execution (java.util.List ua.com.foxminded.repositories.interfaces.*.findAll())")
    private void findAllMethods() {
    }

    @Pointcut("execution (* ua.com.foxminded.repositories.interfaces.*.findById(Integer))")
    private void findByIdMethods() {
    }

    @Pointcut("execution (void ua.com.foxminded.repositories.interfaces.*.deleteById(Integer))")
    private void deleteByIdMethods() {
    }

    @Around("saveMethods()")
    void aroundCreateAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Object object = proceedingJoinPoint.getArgs()[0];

        if (logger.isDebugEnabled()) {
            logger.debug("Try to save/update an object: {}.", object);
        }

        try {
            proceedingJoinPoint.proceed();
            if (logger.isDebugEnabled()) {
                logger.debug("The object {} was saved/updated.", object);
            }
        } catch (DataAccessException dataAccessException) {
            logger.error("Can't save/update the object: {}.", object, dataAccessException);
            throw new RepositoryException("Can't save/update the object", dataAccessException);
        }
    }

    @Around("findAllMethods()")
    Object aroundFindAllAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        if (logger.isDebugEnabled()) {
            logger.debug("Try to find all objects.");
        }
        Object targetMethod = proceedingJoinPoint.proceed();
        if (targetMethod instanceof List<?>) {

            if (((List<?>) targetMethod).isEmpty()) {
                logger.warn("There are not any objects in the result when findAll.");

            } else {

                if (logger.isDebugEnabled()) {
                    logger.debug("The result is: {}.", targetMethod);
                }
            }
        }

        return targetMethod;
    }

    @Around("findByIdMethods()")
    Object aroundFindByIdAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Integer id = (Integer) proceedingJoinPoint.getArgs()[0];
        if (logger.isDebugEnabled()) {
            logger.debug("Try to find an object by id: {}.", id);
        }

        try {
            Optional<Object> targetMethod = (Optional<Object>) proceedingJoinPoint.proceed();

            if(targetMethod.isEmpty()) {
                throw new NullPointerException("There is no object in the database with pointed id.");
            }

            if (logger.isDebugEnabled()) {
                logger.debug("The result object with id {} is {}.", id, targetMethod);
            }

            return targetMethod;

        } catch (NullPointerException nullPointerException) {
            logger.error("There is no result when find an object by id {}.", id, nullPointerException);
            throw new RepositoryException("Can't find an object by id.", nullPointerException);
        } catch (DataAccessException dataAccessException) {
            logger.error("Can't find an object by id {}.", id, dataAccessException);
            throw new RepositoryException("Can't find an object by id.", dataAccessException);
        }
    }

    @Around("deleteByIdMethods()")
    void aroundDeleteByIdAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Integer objectId = (Integer) proceedingJoinPoint.getArgs()[0];

        if (logger.isDebugEnabled()) {
            logger.debug("Try to delete an object by id {}.", objectId);            
        }

        try {
            proceedingJoinPoint.proceed();

            if (logger.isDebugEnabled()) {
                logger.debug("The object with id {} was deleted.", objectId);
            }
        } catch (DataAccessException dataAccessException) {
            logger.error("Can't delete an object by id {}.", objectId, dataAccessException);
            throw new RepositoryException("Can't delete an object by id.", dataAccessException);
        }
    }
}