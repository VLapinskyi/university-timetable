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
}
