package ua.com.foxminded.service.aspects;

import java.lang.reflect.Method;
import java.util.List;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import ua.com.foxminded.dao.exceptions.DAOException;
import ua.com.foxminded.service.exceptions.ServiceException;

@Aspect
@Configuration
public class GeneralServiceAspect {
    private static final Logger LOGGER = LoggerFactory.getLogger(GeneralServiceAspect.class);

    @Pointcut ("execution (void ua.com.foxminded.service.*.create(*))")
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
    void aroundCreateAdvice (ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Object object = proceedingJoinPoint.getArgs()[0];

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to create a new object: {}.", object);
        }

        try {        
            if (object == null) {
                IllegalArgumentException exception = new IllegalArgumentException("An object can't be null when create.");
                LOGGER.error("An object {} is null when create.", object, exception);
                throw exception;
            }

            Method getIdMethod = object.getClass().getMethod("getId");
            Method getNameMethod = object.getClass().getMethod("getName");
            int objectId = (int) getIdMethod.invoke(object);
            String objectName = (String) getNameMethod.invoke(object);

            if (objectId != 0) {
                IllegalArgumentException exception = new IllegalArgumentException("An object id isn't 0 when create.");
                LOGGER.error("An object {} has wrong id {} which is not equal zero when create.", object, objectId, exception);
                throw exception;
            }

            if (objectName == null || objectName.trim().length() < 2) {
                IllegalArgumentException exception = new IllegalArgumentException("An object name isn't correct when create.");
                LOGGER.error("An object {} has wrong name {} when create.", object, objectName, exception);
                throw exception;
            }

            proceedingJoinPoint.proceed();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("The object {} was created.", object);
            }
        } catch (DAOException daoException) {
            LOGGER.error("There is some error in dao layer when create an object {}.", object, daoException);
            throw new ServiceException("There is some error in dao layer when create object.", daoException);
        } catch (IllegalArgumentException illegalArgumentException) {
            throw new ServiceException("A given object isn't correct when create.", illegalArgumentException);
        }
    }

    @Around("getAllMethods()")
    Object aroundGetAllAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to get all objects.");
        }

        try {
            Object targetMethod = proceedingJoinPoint.proceed();
            if (targetMethod instanceof List<?>) {

                if (((List<?>) targetMethod).isEmpty()) {
                    LOGGER.warn("There are not any objects in the result when getAll.");

                } else {

                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("The result is: {}.", targetMethod);
                    }
                }
            }

            return targetMethod;

        } catch (DAOException daoException) {
            LOGGER.error("There is some error in dao layer when getAll.", daoException);
            throw new ServiceException("There is some error in dao layer when getAll.", daoException);
        }
    }

    @Around ("getByIdMethods()")
    Object aroundGetByIdAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        int id = (int) proceedingJoinPoint.getArgs()[0];

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to get an object by id: {}.", id);
        }

        try {
            if (id < 1) {
                IllegalArgumentException exception = new IllegalArgumentException("A given id is less than 1 when getById.");
                LOGGER.error("A given id {} is less than 1 when getById.", id, exception);
                throw exception;
            }


            Object targetMethod = proceedingJoinPoint.proceed();

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("The result object with id {} is {}.", id, targetMethod);
            }

            return targetMethod;
        } catch (DAOException daoException) {
            LOGGER.error("There is some error in dao layer when get object by id {}.", id, daoException);
            throw new ServiceException("There is some error in dao layer when get object by id.", daoException);
        } catch (IllegalArgumentException illegalArgumentException) {
            throw new ServiceException("A given id is incorrect when getById.", illegalArgumentException);
        }
    }

    @Around ("updateMethods()")
    void aroundUpdateAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Object updatedObject = proceedingJoinPoint.getArgs()[0];

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to update an object: {}.", updatedObject);
        }

        try {
            if (updatedObject == null) {
                IllegalArgumentException exception = new IllegalArgumentException("A given object is null when update.");
                LOGGER.error("A given object {} is null when update.", updatedObject, exception);
                throw exception;
            }

            Method getIdMethod = updatedObject.getClass().getMethod("getId");
            Method getNameMethod = updatedObject.getClass().getMethod("getName");
            int objectId = (int) getIdMethod.invoke(updatedObject);
            String objectName = (String) getNameMethod.invoke(updatedObject);

            if (objectId < 1) {
                IllegalArgumentException exception = new IllegalArgumentException("An object id isn't positive when update.");
                LOGGER.error("An object {} has wrong id {} which is not positive when update.", updatedObject, objectId, exception);
                throw exception;
            }

            if (objectName == null || objectName.trim().length() < 2) {
                IllegalArgumentException exception = new IllegalArgumentException("An object name isn't correct when update.");
                LOGGER.error("An object {} has wrong name {} when update.", updatedObject, objectName, exception);
                throw exception;
            }

            proceedingJoinPoint.proceed();

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("The object {} was updated.", updatedObject);
            }
        } catch (DAOException daoException) {
            LOGGER.error("There is some error in dao layer when update an object {}.", updatedObject, daoException);
            throw new ServiceException("Can't update an object.", daoException);
        } catch (IllegalArgumentException illegalArgumentException) {
            throw new ServiceException("A given object isn't correct when update.", illegalArgumentException);
        }
    }

    @Around ("deleteByIdMethods()")
    void aroundDeleteByIdAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        int id = (int) proceedingJoinPoint.getArgs()[0];

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to delete an object by id: {}.", id);
        }

        try {
            if (id < 1) {
                IllegalArgumentException exception = new IllegalArgumentException("A given id is less than 1 when deleteById.");
                LOGGER.error("A given id {} is less than 1 when deleteById.", id, exception);
                throw exception;
            }

            proceedingJoinPoint.proceed();

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("An object was deleted by id {}.", id);
            }
        } catch (DAOException daoException) {
            LOGGER.error("There is some error in dao layer when delete an object by id {}.", id, daoException);
            throw new ServiceException("There is some error in dao layer when delete an object by id.", daoException);
        } catch (IllegalArgumentException illegalArgumentException) {
            throw new ServiceException("A given id is less than 1 when deleteById.", illegalArgumentException);
        }
    }
}
