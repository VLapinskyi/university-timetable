package ua.com.foxminded.domain.validation;

import java.time.DayOfWeek;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class DayOfWeekLessonValidator implements ConstraintValidator<NotDayOff, DayOfWeek> {

    @Override
    public boolean isValid(DayOfWeek value, ConstraintValidatorContext context) {
        if(value != null && value.getValue() == 7) {
            return false;
        } else {
            return true;
        }
    }

}
