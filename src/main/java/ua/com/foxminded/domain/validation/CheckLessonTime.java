package ua.com.foxminded.domain.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { LessonTimeValidator.class })
public @interface CheckLessonTime {
	public String message() default "LessonTime's startTime must be before endTime";

	public Class<?>[] groups() default {};

	public Class<? extends Payload>[] payload() default {};
}
