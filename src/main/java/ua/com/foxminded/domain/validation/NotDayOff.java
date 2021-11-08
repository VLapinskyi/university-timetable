package ua.com.foxminded.domain.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DayOfWeekLessonValidator.class)
public @interface NotDayOff {
    public String message() default "Lesson must not be on day off.";

    public Class<?>[] groups() default {};

    public Class<? extends Payload>[] payload() default {};
}
