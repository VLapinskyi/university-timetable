package ua.com.foxminded.api.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.ConstraintViolationException;
import ua.com.foxminded.repositories.exceptions.RepositoryException;
import ua.com.foxminded.service.exceptions.ServiceException;

@Aspect
@Configuration
public class GeneralRestControllerAspect {
    private static final Logger LOGGER = LoggerFactory.getLogger(GeneralRestControllerAspect.class);

    @Pointcut("execution(public java.util.List ua.com.foxminded.api.*.get*s())")
    private void getObjectsMethods() {
    }

    @Pointcut("execution(public * ua.com.foxminded.api.*.get*(int))")
    private void getObjectMethods() {
    }

    @Pointcut("execution(public * ua.com.foxminded.api.*.create*(..))")
    private void createObjectMethods() {
    }
    
    @Pointcut("execution(public * ua.com.foxminded.api.*.update*(..))")
    private void updateObjectMethods() {
    }
    
    @Pointcut("execution(public String ua.com.foxminded.api.*.delete*(int))")
    private void deleteObjectMethods() {
    }

    @Around("getObjectsMethods()")
    public Object aroundGetObjectsAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to get all objects.");
        }

        try {
            return proceedingJoinPoint.proceed();

        } catch (ServiceException serviceException) {
            if (serviceException.getException() instanceof RepositoryException) {
                LOGGER.error("There are some errors in reposotories layer when get all objects.", serviceException);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        serviceException.getMessage());
            } else {
                LOGGER.error("There is some error in service layer when get all objects.", serviceException);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, serviceException.getMessage());
            }
        }
    }

    @Around("getObjectMethods()")
    public Object aroundGetObjectAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        int id = (int) proceedingJoinPoint.getArgs()[0];

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to get object with id {}.", id);
        }

        try {
            return proceedingJoinPoint.proceed();

        } catch (ServiceException serviceException) {
            if (serviceException.getException() instanceof RepositoryException) {
                LOGGER.error("There are some errors in repositories layer when get an object with id {}.", id, serviceException);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        serviceException.getMessage());
            } else if (serviceException.getException() instanceof IllegalArgumentException) {
                LOGGER.error("The given id {} is wrong when get an object with this id.", id, serviceException);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        serviceException.getMessage());
            } else {
                LOGGER.error("There is some error in service layer when get an object with id {}.", id, serviceException);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, serviceException.getMessage());
            }
        }
    }

    @Around("createObjectMethods()")
    public Object aroundCreateObjectAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Object object = proceedingJoinPoint.getArgs()[0];

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to create object {}.", object);
        }

        try {
            return proceedingJoinPoint.proceed();

        } catch (ServiceException serviceException) {
            if (serviceException.getException() instanceof RepositoryException) {
                LOGGER.error("There are some errors in repositories layer when create an object {}.", object, serviceException);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        serviceException.getMessage());
            } else if (serviceException.getException() instanceof ConstraintViolationException 
                    || serviceException.getException() instanceof IllegalArgumentException) {
                LOGGER.error("There are errors with given data when create object {}.", object, serviceException);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        serviceException.getMessage());
            } else {
                LOGGER.error("There is some error in service layer when create an object {}.", object,
                        serviceException);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        serviceException.getMessage());
            }
        }
    }
    
    @Around("updateObjectMethods()")
    public Object aroundUpdateObjectAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Object object = proceedingJoinPoint.getArgs()[0];
        
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to update object {}.", object);
        }

        try {
            return proceedingJoinPoint.proceed();

        } catch (ServiceException serviceException) {
            if (serviceException.getException() instanceof RepositoryException) {
                LOGGER.error("There are some errors in repositories layer when update an object {}.", object, serviceException);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        serviceException.getMessage());
            } else if (serviceException.getException() instanceof ConstraintViolationException 
                    || serviceException.getException() instanceof IllegalArgumentException) {
                LOGGER.error("There are errors with given data when update object {}.", object, serviceException);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        serviceException.getMessage());
            } else {
                LOGGER.error("There is some error in service layer when update an object {}.", object,
                        serviceException);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        serviceException.getMessage());
            }
        }
    }
    
    @Around("deleteObjectMethods()")
    public Object aroundDeleteObjectAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        int id = (int) proceedingJoinPoint.getArgs()[0];
        
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to delete object with id {}.", id);            
        }

        try {
            return proceedingJoinPoint.proceed();

        } catch (ServiceException serviceException) {
            if (serviceException.getException() instanceof RepositoryException) {
                LOGGER.error("There are some errors in repositories layer when delete object with id {}.", id, serviceException);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        serviceException.getMessage());
            } else if (serviceException.getException() instanceof IllegalArgumentException) {
                LOGGER.error("There are errors with given data when delete object with id {}.", id, serviceException);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        serviceException.getMessage());
            } else {
                LOGGER.error("There is some error in service layer when delete object with id {}.", id,
                        serviceException);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        serviceException.getMessage());
            }
        }
    }
}
