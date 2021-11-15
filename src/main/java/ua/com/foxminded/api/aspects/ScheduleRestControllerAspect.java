package ua.com.foxminded.api.aspects;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import ua.com.foxminded.domain.Lesson;
import ua.com.foxminded.repositories.exceptions.RepositoryException;
import ua.com.foxminded.service.exceptions.ServiceException;

@Aspect
@Configuration
public class ScheduleRestControllerAspect {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleRestControllerAspect.class);

    @Pointcut("execution(public java.util.Map ua.com.foxminded.api.ScheduleRestController.getLecturerWeekSchedule(int))")
    private void getLecturerWeekScheduleMethod() {
    }

    @Pointcut("execution(public java.util.Map ua.com.foxminded.api.ScheduleRestController.getLecturerMonthSchedule(int, String))")
    private void getLecturerMonthScheduleMethod() {
    }

    @Pointcut("execution(public java.util.Map ua.com.foxminded.api.ScheduleRestController.getGroupWeekSchedule(int))")
    private void getGroupWeekScheduleMethod() {
    }

    @Pointcut("execution(public java.util.Map ua.com.foxminded.api.ScheduleRestController.getGroupMonthSchedule(int, String))")
    private void getGroupMonthScheduleMethod() {
    }

    @Around("getLecturerWeekScheduleMethod()")
    public Map<LocalDate, List<Lesson>> aroundGetLecturerWeekSchedule(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        int lecturerId = (int) proceedingJoinPoint.getArgs()[0];
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to get week schedule for a lecturer with id: {}.", lecturerId);
        }

        try {
            Map<LocalDate, List<Lesson>> resultMethod = (Map<LocalDate, List<Lesson>>) proceedingJoinPoint.proceed();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("The result model of searching a week schedule for a lecturer with id {} is {}.",
                        lecturerId, resultMethod);
            }

            return resultMethod;

        } catch (ServiceException serviceException) {
            if (serviceException.getException() instanceof RepositoryException) {
                LOGGER.error("There are some errors in repositories layer when get week schedule for a lecturer with id {}.",
                        lecturerId, serviceException);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        serviceException.getMessage());
            } else if (serviceException.getException() instanceof IllegalArgumentException) {
                LOGGER.error("The given lecturerId {} is wrong when get week schedule for a lecturer.", lecturerId,
                        serviceException);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        serviceException.getMessage());
            } else {
                LOGGER.error("There is some error in service layer when get week schedule for a lecturer with id {}.",
                        lecturerId, serviceException);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, serviceException.getMessage());
            }
        }
    }

    @Around("getLecturerMonthScheduleMethod()")
    public Map<LocalDate, List<Lesson>> aroundGetLecturerMonthSchedule(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        int lecturerId = (int) proceedingJoinPoint.getArgs()[0];
        String monthValue = (String) proceedingJoinPoint.getArgs()[1];
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to get schedule of month {} for a lecturer with id: {}.", monthValue, lecturerId);
        }

        try {
            Map<LocalDate, List<Lesson>> resultMethod = (Map<LocalDate, List<Lesson>>) proceedingJoinPoint.proceed();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("The result model of searching  schedule of a month {} for a lecturer with id {} is {}.",
                        monthValue, lecturerId, resultMethod);
            }

            return resultMethod;

        } catch (ServiceException serviceException) {
            if (serviceException.getException() instanceof RepositoryException) {
                LOGGER.error(
                        "There are some errors in repositories layer when get schedule of a month {} for a lecturer with id {}.",
                        monthValue, lecturerId, serviceException);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        serviceException.getMessage());
            } else if (serviceException.getException() instanceof IllegalArgumentException) {
                LOGGER.error(
                        "The given parameters lecturerId = {} and monthValue = {} are wrong when get month schedule for a lecturer.",
                        lecturerId, monthValue, serviceException);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        serviceException.getMessage());
            } else {
                LOGGER.error(
                        "There is some error in service layer when get schedule of a month {} for a lecturer with id {}.",
                        monthValue, lecturerId, serviceException);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, serviceException.getMessage());
            }
        }
    }

    @Around("getGroupWeekScheduleMethod()")
    public Map<LocalDate, List<Lesson>> aroundGetGroupWeekSchedule(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        int groupId = (int) proceedingJoinPoint.getArgs()[0];
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to get week schedule for a group with id: {}.", groupId);
        }

        try {
            Map<LocalDate, List<Lesson>> resultMethod = (Map<LocalDate, List<Lesson>>) proceedingJoinPoint.proceed();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("The result model of searching a week schedule for a group with id {} is {}.", groupId,
                        resultMethod);
            }

            return resultMethod;

        } catch (ServiceException serviceException) {
            if (serviceException.getException() instanceof RepositoryException) {
                LOGGER.error("There are some errors in repositories layer when get week schedule for a group with id {}.",
                        groupId, serviceException);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        serviceException.getMessage());
            } else if (serviceException.getException() instanceof IllegalArgumentException) {
                LOGGER.error("The given groupId {} is wrong when get week schedule for a group.", groupId,
                        serviceException);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        serviceException.getMessage());
            } else {
                LOGGER.error("There is some error in service layer when get week schedule for a group with id {}.",
                        groupId, serviceException);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, serviceException.getMessage());
            }
        }
    }

    @Around("getGroupMonthScheduleMethod()")
    public Map<LocalDate, List<Lesson>> aroundGetGroupMonthSchedule(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        int groupId = (int) proceedingJoinPoint.getArgs()[0];
        String monthValue = (String) proceedingJoinPoint.getArgs()[1];
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to get schedule of month {} for a group with id: {}.", monthValue, groupId);
        }

        try {
            Map<LocalDate, List<Lesson>> resultMethod = (Map<LocalDate, List<Lesson>>) proceedingJoinPoint.proceed();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("The result model of searching  schedule of a month {} for a group with id {} is {}.",
                        monthValue, groupId, resultMethod);
            }

            return resultMethod;

        } catch (ServiceException serviceException) {
            if (serviceException.getException() instanceof RepositoryException) {
                LOGGER.error(
                        "There are some errors in repositories layer when get schedule of a month {} for a group with id {}.",
                        monthValue, groupId, serviceException);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        serviceException.getMessage());
            } else if (serviceException.getException() instanceof IllegalArgumentException) {
                LOGGER.error(
                        "The given parameters groupId = {} and monthValue = {} are wrong when get month schedule for a lecturer.",
                        groupId, monthValue, serviceException);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        serviceException.getMessage());
            } else {
                LOGGER.error(
                        "There is some error in service layer when get schedule of a month {} for a group with id {}.",
                        monthValue, groupId, serviceException);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, serviceException.getMessage());
            }
        }
    }
}