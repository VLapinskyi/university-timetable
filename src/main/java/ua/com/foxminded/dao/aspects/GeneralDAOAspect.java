package ua.com.foxminded.dao.aspects;

import java.util.List;

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

@Aspect
@Configuration
public class GeneralDAOAspect {
	private static final Logger LOGGER = LoggerFactory.getLogger(GeneralDAOAspect.class);

	@Pointcut("execution (void ua.com.foxminded.dao.*.create(*))")
	private void createMethods() {
	}

	@Pointcut("execution (java.util.List ua.com.foxminded.dao.*.findAll())")
	private void findAllMethods() {
	}

	@Pointcut("execution (* ua.com.foxminded.dao.*.findById(int))")
	private void findByIdMethods() {
	}

	@Pointcut("execution (void ua.com.foxminded.dao.*.update(int, *))")
	private void updateMethods() {
	}

	@Pointcut("execution (void ua.com.foxminded.dao.*.deleteById(int))")
	private void deleteByIdMethods() {
	}

	@Around("createMethods()")
	void aroundCreateAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		Object object = proceedingJoinPoint.getArgs()[0];

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Try to insert a new object: {}.", object);
		}

		try {
			proceedingJoinPoint.proceed();
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("The object {} was inserted.", object);
			}
		} catch (DataAccessException dataAccessException) {
			LOGGER.error("Can't insert the object: {}.", object, dataAccessException);
			throw new DAOException("Can't insert the object", dataAccessException);
		}
	}

	@Around("findAllMethods()")
	Object aroundFindAllAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Try to find all objects.");
		}

		try {
			Object targetMethod = proceedingJoinPoint.proceed();
			if (targetMethod instanceof List<?>) {

				if (((List<?>) targetMethod).isEmpty()) {
					LOGGER.warn("There are not any objects in the result when findAll.");

				} else {

					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("The result is: {}.", targetMethod);
					}
				}
			}

			return targetMethod;

		} catch (DataAccessException dataAccessException) {
			LOGGER.error("Can't find all objects.", dataAccessException);
			throw new DAOException("Can't find all objects.", dataAccessException);
		}
	}

	@Around("findByIdMethods()")
	Object aroundFindByIdAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		int id = (int) proceedingJoinPoint.getArgs()[0];

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Try to find an object by id: {}.", id);
		}

		try {
			Object targetMethod = proceedingJoinPoint.proceed();

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("The result object with id {} is {}.", id, targetMethod);
			}

			return targetMethod;

		} catch (EmptyResultDataAccessException emptyResultDataAccessException) {
			LOGGER.error("There is no result when find an object by id {}.", id, emptyResultDataAccessException);
			throw new DAOException("Can't find an object by id.", emptyResultDataAccessException);
		} catch (DataAccessException dataAccessException) {
			LOGGER.error("Can't find an object by id {}.", id, dataAccessException);
			throw new DAOException("Can't find an object by id.", dataAccessException);
		}
	}

	@Around("updateMethods()")
	void aroundUpdateAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		int id = (int) proceedingJoinPoint.getArgs()[0];
		Object updatedObject = proceedingJoinPoint.getArgs()[1];

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Try to update an object {} with id {}.", updatedObject, id);
		}
		try {
			proceedingJoinPoint.proceed();

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("The object {} with id {} was updated.", updatedObject, id);
			}
		} catch (DataAccessException dataAccessException) {
			LOGGER.error("Can't update an object {} with id {}.", updatedObject, id, dataAccessException);
			throw new DAOException("Can't update an object.", dataAccessException);
		}
	}

	@Around("deleteByIdMethods()")
	void aroundDeleteByIdAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		int id = (int) proceedingJoinPoint.getArgs()[0];

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Try to delete an object by id {}.", id);
		}

		try {
			proceedingJoinPoint.proceed();

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("The object was deleted by id {}.", id);
			}
		} catch (DataAccessException dataAccessException) {
			LOGGER.error("Can't delete an object by id {}.", id, dataAccessException);
			throw new DAOException("Can't delete an object by id.", dataAccessException);
		}
	}
}
