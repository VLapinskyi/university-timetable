package ua.com.foxminded.domain.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import ua.com.foxminded.domain.LessonTime;

public class LessonTimeValidator implements ConstraintValidator<CheckLessonTime, LessonTime> {

    @Override
    public boolean isValid(LessonTime value, ConstraintValidatorContext context) {
        if (value.getStartTime() != null && value.getEndTime() != null
                && value.getStartTime().isAfter(value.getEndTime())) {
            return false;
        } else {
            return true;
        }
    }

}
