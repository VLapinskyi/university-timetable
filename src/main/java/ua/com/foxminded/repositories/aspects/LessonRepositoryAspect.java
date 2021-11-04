package ua.com.foxminded.repositories.aspects;

import java.time.DayOfWeek;
import java.util.List;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

@Aspect
@Configuration
public class LessonRepositoryAspect {
    private final Logger logger = LoggerFactory.getLogger(LessonRepositoryAspect.class);

    @Pointcut("execution (java.util.List ua.com.foxminded.repositories.interfaces.LessonRepository.findByGroupIdAndDay(Integer, java.time.DayOfWeek))")
    private void findByGroupIdAndDayMethod() {
    }

    @Pointcut("execution (java.util.List ua.com.foxminded.repositories.interfaces.LessonRepository.findByLecturerIdAndDay(Integer, java.time.DayOfWeek))")
    private void findByLecturerIdAndDayMethod() {
    }

    @Around("findByGroupIdAndDayMethod()")
    Object aroundFindByGroupIdAndDayAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Integer groupId = (Integer) proceedingJoinPoint.getArgs()[0];
        DayOfWeek weekDay = (DayOfWeek) proceedingJoinPoint.getArgs()[1];

        if (logger.isDebugEnabled()) {
            logger.debug("Try to get all lessons for a group with id {} which is on a day {}.", groupId, weekDay);
        }

            Object targetMethod = proceedingJoinPoint.proceed();

            if (targetMethod instanceof List<?>) {
                if (((List<?>) targetMethod).isEmpty()) {
                    logger.warn("There are not any lesson for the group with id {} on a day {}.", groupId, weekDay);
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("For the group with id {} on a day {} there are lessons: {}.", groupId, weekDay,
                                targetMethod);
                    }
                }
            }

            return targetMethod;
    }

    @Around("findByLecturerIdAndDayMethod()")
    Object aroundFindByLecturerIdAndDayAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Integer lecturerId = (Integer) proceedingJoinPoint.getArgs()[0];
        DayOfWeek weekDay = (DayOfWeek) proceedingJoinPoint.getArgs()[1];

        if (logger.isDebugEnabled()) {
            logger.debug("Try to get all lessons for a lecturer with id {} on a day {}.", lecturerId, weekDay);
        }

            Object targetMethod = proceedingJoinPoint.proceed();

            if (targetMethod instanceof List<?>) {

                if (((List<?>) targetMethod).isEmpty()) {
                    logger.warn("There are not any lesson for the lecturer with id {} on a day {}.", lecturerId,
                            weekDay);
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("For the lecturer with id {} on a day {} there are lessons: {}.", lecturerId,
                                weekDay, targetMethod);
                    }
                }
            }

            return targetMethod;
    }
}