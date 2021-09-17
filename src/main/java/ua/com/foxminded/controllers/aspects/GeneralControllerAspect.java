package ua.com.foxminded.controllers.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.ConstraintViolationException;
import ua.com.foxminded.dao.exceptions.DAOException;
import ua.com.foxminded.service.exceptions.ServiceException;

@Aspect
@Configuration
public class GeneralControllerAspect {
    private static final Logger LOGGER = LoggerFactory.getLogger(GeneralControllerAspect.class);

    @Pointcut("execution(public String ua.com.foxminded.controllers.*.get*s(org.springframework.ui.Model))")
    private void getObjectsMethods() {
    }

    @Pointcut("execution(public String ua.com.foxminded.controllers.*.get*(int, org.springframework.ui.Model))")
    private void getObjectMethods() {
    }
    
    @Pointcut("execution(public String ua.com.foxminded.controllers.*.new*(*, org.springframework.ui.Model))")
    private void newObjectMethods() {
    }

    @Pointcut("execution(public String ua.com.foxminded.controllers.*.create*(..))")
    private void createObjectMethods() {
    }
    
    @Pointcut("execution(public String ua.com.foxminded.controllers.*.update*(ua.com.foxminded.domain.*))")
    private void updateObjectMethods() {
    }
    
    @Pointcut("execution(public String ua.com.foxminded.controllers.*.delete*(int))")
    private void deleteObjectMethods() {
    }

    @Around("getObjectsMethods()")
    public String aroundGetObjectsAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to get all objects.");
        }

        try {
            return (String) proceedingJoinPoint.proceed();

        } catch (ServiceException serviceException) {
            if (serviceException.getException() instanceof DAOException) {
                LOGGER.error("There are some errors in dao layer when get all objects.", serviceException);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        serviceException.getServiceExceptionMessage());
            } else {
                LOGGER.error("There is some error in service layer when get all objects.", serviceException);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, serviceException.getServiceExceptionMessage());
            }
        }
    }

    @Around("getObjectMethods()")
    public String aroundGetObjectAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        int id = (int) proceedingJoinPoint.getArgs()[0];

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to get object with id {}.", id);
        }

        try {
            return (String) proceedingJoinPoint.proceed();

        } catch (ServiceException serviceException) {
            if (serviceException.getException() instanceof DAOException) {
                LOGGER.error("There are some errors in dao layer when get an object with id{}.", id, serviceException);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        serviceException.getServiceExceptionMessage());
            } else if (serviceException.getException() instanceof IllegalArgumentException) {
                LOGGER.error("The given id {} is wrong when get an object with this id.", id, serviceException);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        serviceException.getServiceExceptionMessage());
            } else {
                LOGGER.error("There is some error in service layer when get an object with id {}.", serviceException);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, serviceException.getServiceExceptionMessage());
            }
        }
    }

    @Around("createObjectMethods()")
    public String aroundCreateObjectAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Object object = proceedingJoinPoint.getArgs()[0];

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to create object {}.", object);
        }

        try {
            return (String) proceedingJoinPoint.proceed();

        } catch (ServiceException serviceException) {
            if (serviceException.getException() instanceof DAOException) {
                LOGGER.error("There are some errors in dao layer when create an object {}.", object, serviceException);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        serviceException.getServiceExceptionMessage());
            } else if (serviceException.getException() instanceof ConstraintViolationException 
                    || serviceException.getException() instanceof IllegalArgumentException) {
                LOGGER.error("There are errors with given data when create object {}.", object, serviceException);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        serviceException.getServiceExceptionMessage());
            } else {
                LOGGER.error("There is some error in service layer when create an object {}.", object,
                        serviceException);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        serviceException.getServiceExceptionMessage());
            }
        }
    }
    
    @Around("updateObjectMethods()")
    public String aroundUpdateObjectAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Object object = proceedingJoinPoint.getArgs()[0];
        
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to update object {}.", object);
        }

        try {
            return (String) proceedingJoinPoint.proceed();

        } catch (ServiceException serviceException) {
            if (serviceException.getException() instanceof DAOException) {
                LOGGER.error("There are some errors in dao layer when update an object {}.", object, serviceException);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        serviceException.getServiceExceptionMessage());
            } else if (serviceException.getException() instanceof ConstraintViolationException 
                    || serviceException.getException() instanceof IllegalArgumentException) {
                LOGGER.error("There are errors with given data when update object {}.", object, serviceException);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        serviceException.getServiceExceptionMessage());
            } else {
                LOGGER.error("There is some error in service layer when update an object {}.", object,
                        serviceException);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        serviceException.getServiceExceptionMessage());
            }
        }
    }
    
    @Around("deleteObjectMethods()")
    public String aroundDeleteObjectAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        int id = (int) proceedingJoinPoint.getArgs()[0];

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to delete object with id {}.", id);
        }

        try {
            return (String) proceedingJoinPoint.proceed();

        } catch (ServiceException serviceException) {
            if (serviceException.getException() instanceof DAOException) {
                LOGGER.error("There are some errors in dao layer when delete object with id {}.", id, serviceException);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        serviceException.getServiceExceptionMessage());
            } else if (serviceException.getException() instanceof IllegalArgumentException) {
                LOGGER.error("There are errors with given data when delete object with id {}.", id, serviceException);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        serviceException.getServiceExceptionMessage());
            } else {
                LOGGER.error("There is some error in service layer when delete object with id {}.", id,
                        serviceException);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        serviceException.getServiceExceptionMessage());
            }
        }
    }
    
    @Around("newObjectMethods()")
    public String aroundNewObjectAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to get a page for creating a new object.");
        }

        try {
            return (String) proceedingJoinPoint.proceed();

        } catch (ServiceException serviceException) {
            if (serviceException.getException() instanceof DAOException) {
                LOGGER.error("There are some errors in dao layer when get a page for creating a new object.", serviceException);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        serviceException.getServiceExceptionMessage());
            } else {
                LOGGER.error("There is some error in service layer when get a page for creating a new object.",
                        serviceException);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        serviceException.getServiceExceptionMessage());
            }
        }
    }
}
