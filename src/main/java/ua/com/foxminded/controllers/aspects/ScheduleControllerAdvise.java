package ua.com.foxminded.controllers.aspects;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import ua.com.foxminded.dao.exceptions.DAOException;
import ua.com.foxminded.service.exceptions.ServiceException;

@Aspect
@Configuration
public class ScheduleControllerAdvise {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleControllerAdvise.class);
    
    @Pointcut("execution(public String ua.com.foxminded.controllers.ScheduleController.searchSchedule(org.springframework.ui.Model))")
    private void searchScheduleMethod() {
    }
    
    @Pointcut("execution(public String ua.com.foxminded.controllers.ScheduleController.resultSchedule(javax.servlet.http.HttpServletRequest, org.springframework.ui.Model))")
    private void resultScheduleMethod() {
    }
    
    @Around("searchScheduleMethod()")
    public String aroundSearchScheduleMethodAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Prepare search schedule form.");
        }
        
        try {
            return (String) proceedingJoinPoint.proceed();
        } catch (ServiceException serviceException) {
            if (serviceException.getException() instanceof DAOException) {
                LOGGER.error("There are some errors in dao layer when prepare search schedule form.", serviceException);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, serviceException.getServiceExceptionMessage());
            } else {
                LOGGER.error("There is some error in service layer when prepare search schedule form.", serviceException);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, serviceException.getServiceExceptionMessage());
            }
        }
    }
    
    @Around("resultScheduleMethod()")
    public String aroundResultSchedule(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        HttpServletRequest request = (HttpServletRequest) proceedingJoinPoint.getArgs()[0];
        Enumeration<String> parametersNames = request.getAttributeNames();
        Map<String, String> parametersValues = new HashMap<>();
        
        while(parametersNames.hasMoreElements()) {
            String parameterName = parametersNames.nextElement();
            parametersValues.put(parameterName, request.getParameter(parameterName));
        }
        
        if (LOGGER.isDebugEnabled()) {            
            LOGGER.debug("Try to get the result of the search schedule form with parameters: {}.", parametersValues);
        }
        
        try {
            return (String) proceedingJoinPoint.proceed();
            
        } catch (ServiceException serviceException) {
            if (serviceException.getException() instanceof DAOException) {
                LOGGER.error("There are some errors in dao layer when get the result of the search schedule form with parameters: {}.", parametersValues,  serviceException);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, serviceException.getServiceExceptionMessage());
            } else if (serviceException.getException() instanceof IllegalArgumentException) {
                LOGGER.error("The given parameters {} is wrong when get the result of the search schedule form.", parametersValues,  serviceException);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, serviceException.getServiceExceptionMessage());
            } else {
                LOGGER.error("There is some error in service layer when get the result of the search schedule form with parameters: {}.", parametersValues, serviceException);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, serviceException.getServiceExceptionMessage());
            }
        }
    }
}