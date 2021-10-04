package ua.com.foxminded.controllers.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.server.ResponseStatusException;

import ua.com.foxminded.dao.exceptions.DAOException;
import ua.com.foxminded.service.exceptions.ServiceException;

@Aspect
@Configuration
public class ScheduleControllerAspect {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleControllerAspect.class);

    @Pointcut("execution(public String ua.com.foxminded.controllers.ScheduleController.searchSchedule(org.springframework.ui.Model))")
    private void searchScheduleMethod() {
    }

    @Pointcut("execution(public String ua.com.foxminded.controllers.ScheduleController.getLecturerWeekSchedule(int, org.springframework.ui.Model))")
    private void getLecturerWeekScheduleMethod() {
    }

    @Pointcut("execution(public String ua.com.foxminded.controllers.ScheduleController.getLecturerMonthSchedule(int, String, org.springframework.ui.Model))")
    private void getLecturerMonthScheduleMethod() {
    }

    @Pointcut("execution(public String ua.com.foxminded.controllers.ScheduleController.getGroupWeekSchedule(int, org.springframework.ui.Model))")
    private void getGroupWeekScheduleMethod() {
    }

    @Pointcut("execution(public String ua.com.foxminded.controllers.ScheduleController.getGroupMonthSchedule(int, String, org.springframework.ui.Model))")
    private void getGroupMonthScheduleMethod() {
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
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        serviceException.getServiceExceptionMessage());
            } else {
                LOGGER.error("There is some error in service layer when prepare search schedule form.",
                        serviceException);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, serviceException.getServiceExceptionMessage());
            }
        }
    }

    @Around("getLecturerWeekScheduleMethod()")
    public String aroundGetLecturerWeekSchedule(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        int lecturerId = (int) proceedingJoinPoint.getArgs()[0];
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to get week schedule for a lecturer with id: {}.", lecturerId);
        }

        try {
            String resultMethod = (String) proceedingJoinPoint.proceed();
            Model model = (Model) proceedingJoinPoint.getArgs()[1];
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("The result model of searching a week schedule for a lecturer with id {} is {}.",
                        lecturerId, model.asMap());
            }

            return resultMethod;

        } catch (ServiceException serviceException) {
            if (serviceException.getException() instanceof DAOException) {
                LOGGER.error("There are some errors in dao layer when get week schedule for a lecturer with id {}.",
                        lecturerId, serviceException);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        serviceException.getServiceExceptionMessage());
            } else if (serviceException.getException() instanceof IllegalArgumentException) {
                LOGGER.error("The given lecturerId {} is wrong when get week schedule for a lecturer.", lecturerId,
                        serviceException);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        serviceException.getServiceExceptionMessage());
            } else {
                LOGGER.error("There is some error in service layer when get week schedule for a lecturer with id {}.",
                        lecturerId, serviceException);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, serviceException.getServiceExceptionMessage());
            }
        }
    }

    @Around("getLecturerMonthScheduleMethod()")
    public String aroundGetLecturerMonthSchedule(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        int lecturerId = (int) proceedingJoinPoint.getArgs()[0];
        String monthValue = (String) proceedingJoinPoint.getArgs()[1];
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to get schedule of month {} for a lecturer with id: {}.", monthValue, lecturerId);
        }

        try {
            String resultMethod = (String) proceedingJoinPoint.proceed();
            Model model = (Model) proceedingJoinPoint.getArgs()[2];
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("The result model of searching  schedule of a month {} for a lecturer with id {} is {}.",
                        monthValue, lecturerId, model.asMap());
            }

            return resultMethod;

        } catch (ServiceException serviceException) {
            if (serviceException.getException() instanceof DAOException) {
                LOGGER.error(
                        "There are some errors in dao layer when get schedule of a month {} for a lecturer with id {}.",
                        monthValue, lecturerId, serviceException);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        serviceException.getServiceExceptionMessage());
            } else if (serviceException.getException() instanceof IllegalArgumentException) {
                LOGGER.error(
                        "The given parameters lecturerId = {} and monthValue = {} are wrong when get month schedule for a lecturer.",
                        lecturerId, monthValue, serviceException);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        serviceException.getServiceExceptionMessage());
            } else {
                LOGGER.error(
                        "There is some error in service layer when get schedule of a month {} for a lecturer with id {}.",
                        monthValue, lecturerId, serviceException);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, serviceException.getServiceExceptionMessage());
            }
        }
    }

    @Around("getGroupWeekScheduleMethod()")
    public String aroundGetGroupWeekSchedule(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        int groupId = (int) proceedingJoinPoint.getArgs()[0];
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to get week schedule for a group with id: {}.", groupId);
        }

        try {
            String resultMethod = (String) proceedingJoinPoint.proceed();
            Model model = (Model) proceedingJoinPoint.getArgs()[1];
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("The result model of searching a week schedule for a group with id {} is {}.", groupId,
                        model.asMap());
            }

            return resultMethod;

        } catch (ServiceException serviceException) {
            if (serviceException.getException() instanceof DAOException) {
                LOGGER.error("There are some errors in dao layer when get week schedule for a group with id {}.",
                        groupId, serviceException);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        serviceException.getServiceExceptionMessage());
            } else if (serviceException.getException() instanceof IllegalArgumentException) {
                LOGGER.error("The given groupId {} is wrong when get week schedule for a group.", groupId,
                        serviceException);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        serviceException.getServiceExceptionMessage());
            } else {
                LOGGER.error("There is some error in service layer when get week schedule for a group with id {}.",
                        groupId, serviceException);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, serviceException.getServiceExceptionMessage());
            }
        }
    }

    @Around("getGroupMonthScheduleMethod()")
    public String aroundGetGroupMonthSchedule(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        int groupId = (int) proceedingJoinPoint.getArgs()[0];
        String monthValue = (String) proceedingJoinPoint.getArgs()[1];
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to get schedule of month {} for a group with id: {}.", monthValue, groupId);
        }

        try {
            String resultMethod = (String) proceedingJoinPoint.proceed();
            Model model = (Model) proceedingJoinPoint.getArgs()[2];
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("The result model of searching  schedule of a month {} for a group with id {} is {}.",
                        monthValue, groupId, model.asMap());
            }

            return resultMethod;

        } catch (ServiceException serviceException) {
            if (serviceException.getException() instanceof DAOException) {
                LOGGER.error(
                        "There are some errors in dao layer when get schedule of a month {} for a group with id {}.",
                        monthValue, groupId, serviceException);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        serviceException.getServiceExceptionMessage());
            } else if (serviceException.getException() instanceof IllegalArgumentException) {
                LOGGER.error(
                        "The given parameters groupId = {} and monthValue = {} are wrong when get month schedule for a lecturer.",
                        groupId, monthValue, serviceException);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        serviceException.getServiceExceptionMessage());
            } else {
                LOGGER.error(
                        "There is some error in service layer when get schedule of a month {} for a group with id {}.",
                        monthValue, groupId, serviceException);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, serviceException.getServiceExceptionMessage());
            }
        }
    }
}