package ua.com.foxminded.service.aspects;

import java.util.Set;
import java.util.StringJoiner;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import ua.com.foxminded.domain.Lecturer;
import ua.com.foxminded.service.exceptions.ServiceException;

@Aspect
@Configuration
@Order(20)
public class LecturerAspect {
	private static final Logger LOGGER = LoggerFactory.getLogger(LecturerAspect.class);

	private Validator validator;

	@Autowired
	public LecturerAspect(Validator validator) {
		this.validator = validator;
	}

	@Pointcut("execution (void ua.com.foxminded.service.LecturerService.create(ua.com.foxminded.domain.Lecturer))")
	private void createMethod() {
	}

	@Pointcut("execution (void ua.com.foxminded.service.LecturerService.update(ua.com.foxminded.domain.Lecturer))")
	private void updateMethod() {
	}

	@Before("createMethod()")
	void beforeCreateAdvice(JoinPoint joinPoint) {
		Lecturer lecturer = (Lecturer) joinPoint.getArgs()[0];

		try {

			Set<ConstraintViolation<Lecturer>> violations = validator.validate(lecturer);

			if (!violations.isEmpty()) {
				StringJoiner errorMessages = new StringJoiner("; ");

				for (ConstraintViolation<Lecturer> violation : violations) {
					errorMessages.add(violation.getMessage());
				}

				ConstraintViolationException exception = new ConstraintViolationException(
						"When create the lecturer is not valid: " + errorMessages, violations);
				LOGGER.error("The lecturer {} is not valid when create. There are errors: {}.", lecturer, errorMessages,
						exception);
				throw exception;
			}
		} catch (ConstraintViolationException constraintViolationException) {
			throw new ServiceException("A given lecturer isn't valid when create.", constraintViolationException);
		}
	}

	@Before("updateMethod()")
	void beforeUpdateAdvice(JoinPoint joinPoint) {
		Lecturer lecturer = (Lecturer) joinPoint.getArgs()[0];

		try {
			Set<ConstraintViolation<Lecturer>> violations = validator.validate(lecturer);

			if (!violations.isEmpty()) {
				StringJoiner errorMessages = new StringJoiner("; ");

				for (ConstraintViolation<Lecturer> violation : violations) {
					errorMessages.add(violation.getMessage());
				}

				ConstraintViolationException exception = new ConstraintViolationException(
						"When update the lecturer is not valid:" + errorMessages, violations);
				LOGGER.error("The lecturer {} is not valid when update. There are errors: {}.", lecturer, errorMessages,
						exception);
				throw exception;
			}
		} catch (ConstraintViolationException constraintViolationException) {
			throw new ServiceException("A given lecturer isn't valid when update.", constraintViolationException);
		}
	}
}
