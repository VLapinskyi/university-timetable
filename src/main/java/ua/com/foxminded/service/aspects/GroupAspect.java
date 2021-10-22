package ua.com.foxminded.service.aspects;

import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import ua.com.foxminded.domain.Group;
import ua.com.foxminded.repositories.exceptions.RepositoryException;
import ua.com.foxminded.service.exceptions.ServiceException;

@Aspect
@Configuration
public class GroupAspect {
    private static final Logger LOGGER = LoggerFactory.getLogger(GroupAspect.class);

    private Validator validator;

    @Autowired
    public GroupAspect(Validator validator) {
        this.validator = validator;
    }

    @Pointcut("execution (void ua.com.foxminded.service.GroupService.create(ua.com.foxminded.domain.Group))")
    private void createMethod() {
    }

    @Pointcut("execution (void ua.com.foxminded.service.GroupService.update(ua.com.foxminded.domain.Group))")
    private void updateMethod() {
    }

    @Pointcut("execution (java.util.List ua.com.foxminded.service.GroupService.getGroupsFromFaculty(int))")
    private void getGroupsFromFacultyMethod() {
    }

    @Before("createMethod()")
    void beforeCreateAdvice(JoinPoint joinPoint) {
        Group group = (Group) joinPoint.getArgs()[0];

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to create a new group: {}.", group);
        }

        try {
            if (group == null) {
                IllegalArgumentException exception = new IllegalArgumentException("A group can't be null when create.");
                LOGGER.error("A group {} can't be null when create.", group, exception);
                throw exception;
            }

            Set<ConstraintViolation<Group>> violations = validator.validate(group);

            if (!violations.isEmpty()) {
                StringJoiner errorMessages = new StringJoiner("; ");

                for (ConstraintViolation<Group> violation : violations) {
                    errorMessages.add(violation.getMessage());
                }

                ConstraintViolationException exception = new ConstraintViolationException(
                        "When create the group is not valid: " + errorMessages, violations);
                LOGGER.error("The group {} is not valid when create. There are errors: {}.", group, errorMessages,
                        exception);
                throw exception;
            }

            int groupId = group.getId();

            if (groupId != 0) {
                IllegalArgumentException exception = new IllegalArgumentException("A group id isn't 0 when create.");
                LOGGER.error("A group {} has wrong id {} which is not equal zero when create.", group, groupId,
                        exception);
                throw exception;
            }
        } catch (IllegalArgumentException illegalArgumentException) {
            throw new ServiceException("A given group isn't legal when create.", illegalArgumentException);
        } catch (ConstraintViolationException constraintViolationException) {
            throw new ServiceException("A given group isn't valid when create.", constraintViolationException);
        }
    }

    @Before("updateMethod()")
    void beforeUpdateAdvice(JoinPoint joinPoint) {
        Group group = (Group) joinPoint.getArgs()[0];

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to update a group: {}.", group);
        }

        try {
            if (group == null) {
                IllegalArgumentException exception = new IllegalArgumentException("An updated group is null.");
                LOGGER.error("An updated group {} is null.", group, exception);
                throw exception;
            }

            Set<ConstraintViolation<Group>> violations = validator.validate(group);

            if (!violations.isEmpty()) {
                StringJoiner errorMessages = new StringJoiner("; ");

                for (ConstraintViolation<Group> violation : violations) {
                    errorMessages.add(violation.getMessage());
                }

                ConstraintViolationException exception = new ConstraintViolationException(
                        "When update the group is not valid:" + errorMessages, violations);
                LOGGER.error("The group {} is not valid when update. There are errors: {}.", group, errorMessages,
                        exception);
                throw exception;
            }

            int groupId = group.getId();

            if (groupId < 1) {
                IllegalArgumentException exception = new IllegalArgumentException(
                        "A group id isn't positive for existing object.");
                LOGGER.error("An updated group {} has wrong id {} which is not positive.", group, groupId, exception);
                throw exception;
            }
        } catch (IllegalArgumentException illegalArgumentException) {
            throw new ServiceException("A given group isn't legal when update.", illegalArgumentException);
        } catch (ConstraintViolationException constraintViolationException) {
            throw new ServiceException("A given group isn't valid when update.", constraintViolationException);
        }
    }

    @Around("getGroupsFromFacultyMethod()")
    Object aroundGetGroupsFromFacultyAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        int facultyId = (int) proceedingJoinPoint.getArgs()[0];

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to get groups from faculty by faculty id: {}.", facultyId);
        }

        try {

            if (facultyId < 1) {
                IllegalArgumentException exception = new IllegalArgumentException(
                        "A faculty id isn't positive for existing object.");
                LOGGER.error("A faculty id {} is not positive when get groups from a faculty by faculty id.", facultyId,
                        exception);
                throw exception;
            }

            Object targetMethod = proceedingJoinPoint.proceed();
            if (targetMethod instanceof List<?>) {
                if (((List<?>) targetMethod).isEmpty()) {
                    LOGGER.warn("There are not any groups from faculty with id {}.", facultyId);
                } else {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("The result is: {}.", targetMethod);
                    }
                }
            }
            return targetMethod;
        } catch (IllegalArgumentException illegalArgumentException) {
            throw new ServiceException(
                    "There is an error with given number when getting groups from one faculty by faculty id.",
                    illegalArgumentException);
        } catch (RepositoryException repositoryException) {
            LOGGER.error("There is some error in repositories layer when getGroupsFromFaculty by faculty id {}.", facultyId,
                    repositoryException);
            throw new ServiceException("There is some error in repositories layer when getGroupsFromFaculty.", repositoryException);
        }
    }
}
