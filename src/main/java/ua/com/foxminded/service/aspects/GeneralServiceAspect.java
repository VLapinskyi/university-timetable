package ua.com.foxminded.service.aspects;

import java.util.List;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import ua.com.foxminded.repositories.exceptions.RepositoryException;
import ua.com.foxminded.service.exceptions.NotFoundEntityException;
import ua.com.foxminded.service.exceptions.ServiceException;

@Aspect
@Configuration
@Order(30)
public class GeneralServiceAspect {
    private final Logger logger = LoggerFactory.getLogger(GeneralServiceAspect.class);

    @Pointcut("execution (void ua.com.foxminded.service.*.create(*))")
    private void createMethods() {
    }

    @Pointcut("execution (java.util.List ua.com.foxminded.service.*.getAll())")
    private void getAllMethods() {
    }

    @Pointcut("execution (* ua.com.foxminded.service.*.getById(int))")
    private void getByIdMethods() {
    }

    @Pointcut("execution (void ua.com.foxminded.service.*.update(*))")
    private void updateMethods() {
    }

    @Pointcut("execution (void ua.com.foxminded.service.*.deleteById(int))")
    private void deleteByIdMethods() {
    }

    @Around("createMethods()")
    void aroundCreateAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Object object = proceedingJoinPoint.getArgs()[0];

        try {
            proceedingJoinPoint.proceed();

            if (logger.isDebugEnabled()) {
                logger.debug("The object {} was created.", object);
            }
        } catch (RepositoryException repositoryException) {
            logger.error("There is some error in repositories layer when create an object {}.", object, repositoryException);
            throw new ServiceException("There is some error in repositories layer when create object.", repositoryException);
        }
    }

    @Around("getAllMethods()")
    Object aroundGetAllAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        if (logger.isDebugEnabled()) {
            logger.debug("Try to get all objects.");
        }

        try {
            Object targetMethod = proceedingJoinPoint.proceed();
            if (targetMethod instanceof List<?>) {

                if (((List<?>) targetMethod).isEmpty()) {
                    logger.warn("There are not any objects in the result when getAll.");

                } else {

                    if (logger.isDebugEnabled()) {
                        logger.debug("The result is: {}.", targetMethod);
                    }
                }
            }

            return targetMethod;

        } catch (RepositoryException repositoryException) {
            logger.error("There is some error in repositories layer when getAll.", repositoryException);
            throw new ServiceException("There is some error in repositories layer when getAll.", repositoryException);
        }
    }

    @Around("getByIdMethods()")
    Object aroundGetByIdAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        int id = (int) proceedingJoinPoint.getArgs()[0];

        if (logger.isDebugEnabled()) {
            logger.debug("Try to get an object by id: {}.", id);
        }

        try {
            if (id < 1) {
                IllegalArgumentException exception = new IllegalArgumentException(
                        "A given id is less than 1 when getById.");
                logger.error("A given id {} is less than 1 when getById.", id, exception);
                throw exception;
            }

            Object targetMethod = proceedingJoinPoint.proceed();

            if (logger.isDebugEnabled()) {
                logger.debug("The result object with id {} is {}.", id, targetMethod);
            }

            return targetMethod;
        } catch (RepositoryException repositoryException) {
            if (repositoryException.getException() instanceof NullPointerException) {
                NotFoundEntityException notFoundEntityException = new NotFoundEntityException(repositoryException,
                        "The entity was not found wheh get by id.");
                logger.error("The entity is not found when get object by id {}.", id, notFoundEntityException);
                throw new ServiceException("The entity is not found when get object by id.", notFoundEntityException);
            } else {
                logger.error("There is some error in repositories layer when get object by id {}.", id, repositoryException);
                throw new ServiceException("There is some error in repositories layer when get object by id.", repositoryException);
            }
        } catch (IllegalArgumentException illegalArgumentException) {
            throw new ServiceException("A given id is incorrect when getById.", illegalArgumentException);
        }
    }

    @Around("updateMethods()")
    void aroundUpdateAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Object updatedObject = proceedingJoinPoint.getArgs()[0];
        try {
            proceedingJoinPoint.proceed();

            if (logger.isDebugEnabled()) {
                logger.debug("The object {} was updated.", updatedObject);
            }
        } catch (RepositoryException repositoryException) {
            logger.error("There is some error in repositories layer when update an object {}.", updatedObject, repositoryException);
            throw new ServiceException("Can't update an object because of repositoryException.", repositoryException);
        }
    }

    @Around("deleteByIdMethods()")
    void aroundDeleteByIdAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        int id = (int) proceedingJoinPoint.getArgs()[0];

        if (logger.isDebugEnabled()) {
            logger.debug("Try to delete an object by id: {}.", id);
        }

        try {
            if (id < 1) {
                IllegalArgumentException exception = new IllegalArgumentException(
                        "A given id is less than 1 when deleteById.");
                logger.error("A given id {} is less than 1 when deleteById.", id, exception);
                throw exception;
            }

            proceedingJoinPoint.proceed();

            if (logger.isDebugEnabled()) {
                logger.debug("An object was deleted by id {}.", id);
            }
        } catch (RepositoryException repositoryException) {
            logger.error("There is some error in repositories layer when delete an object by id {}.", id, repositoryException);
            throw new ServiceException("There is some error in repositories layer when delete an object by id.", repositoryException);
        } catch (IllegalArgumentException illegalArgumentException) {
            throw new ServiceException("A given id is less than 1 when deleteById.", illegalArgumentException);
        }
    }
}
