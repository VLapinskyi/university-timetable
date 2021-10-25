package ua.com.foxminded.repositories.aspects;

import java.util.List;

import javax.persistence.PersistenceException;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import ua.com.foxminded.repositories.exceptions.RepositoryException;

@Aspect
@Configuration
public class GeneralRepositoryAspect {
    private final Logger logger = LoggerFactory.getLogger(GeneralRepositoryAspect.class);

    @Pointcut("execution (void ua.com.foxminded.repositories.*.create(*))")
    private void createMethods() {
    }

    @Pointcut("execution (java.util.List ua.com.foxminded.repositories.*.findAll())")
    private void findAllMethods() {
    }

    @Pointcut("execution (* ua.com.foxminded.repositories.*.findById(int))")
    private void findByIdMethods() {
    }

    @Pointcut("execution (void ua.com.foxminded.repositories.*.update(*))")
    private void updateMethods() {
    }

    @Pointcut("execution (void ua.com.foxminded.repositories.*.delete(*))")
    private void deleteMethods() {
    }

    @Around("createMethods()")
    void aroundCreateAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Object object = proceedingJoinPoint.getArgs()[0];

        if (logger.isDebugEnabled()) {
            logger.debug("Try to insert a new object: {}.", object);
        }

        try {
            proceedingJoinPoint.proceed();
            if (logger.isDebugEnabled()) {
                logger.debug("The object {} was inserted.", object);
            }
        } catch (PersistenceException persistenceException) {
            logger.error("Can't insert the object: {}.", object, persistenceException);
            throw new RepositoryException("Can't insert the object", persistenceException);
        }
    }

    @Around("findAllMethods()")
    Object aroundFindAllAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        if (logger.isDebugEnabled()) {
            logger.debug("Try to find all objects.");
        }

        try {
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

        } catch (PersistenceException persistenceException) {
            logger.error("Can't find all objects.", persistenceException);
            throw new RepositoryException("Can't find all objects.", persistenceException);
        }
    }

    @Around("findByIdMethods()")
    Object aroundFindByIdAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        int id = (int) proceedingJoinPoint.getArgs()[0];

        if (logger.isDebugEnabled()) {
            logger.debug("Try to find an object by id: {}.", id);
        }

        try {
            Object targetMethod = proceedingJoinPoint.proceed();
            
            if(targetMethod == null) {
                throw new NullPointerException("There is no object in the database with pointed id.");
            }
            
            if (logger.isDebugEnabled()) {
                logger.debug("The result object with id {} is {}.", id, targetMethod);
            }

            return targetMethod;

        } catch (NullPointerException nullPointerException) {
            logger.error("There is no result when find an object by id {}.", id, nullPointerException);
            throw new RepositoryException("Can't find an object by id.", nullPointerException);
        } catch (PersistenceException persistenceException) {
            logger.error("Can't find an object by id {}.", id, persistenceException);
            throw new RepositoryException("Can't find an object by id.", persistenceException);
        }
    }

    @Around("updateMethods()")
    void aroundUpdateAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Object updatedObject = proceedingJoinPoint.getArgs()[0];

        if (logger.isDebugEnabled()) {
            logger.debug("Try to update an object {}.", updatedObject);
        }
        try {
            proceedingJoinPoint.proceed();
            
            if (logger.isDebugEnabled()) {
                logger.debug("The object {} was updated.", updatedObject);
            }
        } catch (PersistenceException persistenceException) {
            logger.error("Can't update an object {}.", updatedObject, persistenceException);
            throw new RepositoryException("Can't update an object.", persistenceException);
        }
    }

    @Around("deleteMethods()")
    void aroundDeleteByIdAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Object object = proceedingJoinPoint.getArgs()[0];
        
        if (logger.isDebugEnabled()) {
            logger.debug("Try to delete an object {}.", object);            
        }

        try {
            proceedingJoinPoint.proceed();

            if (logger.isDebugEnabled()) {
                logger.debug("The object {} was deleted.", object);
            }
        } catch (PersistenceException persistenceException) {
            logger.error("Can't delete an object {}.", object, persistenceException);
            throw new RepositoryException("Can't delete an object.", persistenceException);
        }
    }
}
