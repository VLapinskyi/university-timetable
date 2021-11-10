package ua.com.foxminded.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import ua.com.foxminded.domain.LessonTime;
import ua.com.foxminded.repositories.exceptions.RepositoryException;
import ua.com.foxminded.repositories.interfaces.LessonTimeRepository;
import ua.com.foxminded.service.aspects.GeneralServiceAspect;
import ua.com.foxminded.service.aspects.LessonTimeAspect;
import ua.com.foxminded.service.exceptions.ServiceException;
import ua.com.foxminded.settings.SpringTestConfiguration;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = SpringTestConfiguration.class)
class LessonTimeServiceTest {
    private ListAppender<ILoggingEvent> testAppender;
    
    @Autowired
    private GeneralServiceAspect generalServiceAspect;
    
    @Autowired
    private LessonTimeAspect lessonTimeAspect;
    
    @Autowired
    private LessonTimeService lessonTimeService;

    @MockBean
    private LessonTimeRepository lessonTimeRepository;

    @BeforeEach
    void init() {
        ReflectionTestUtils.setField(lessonTimeService, "lessonTimeRepository", lessonTimeRepository);
        
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger generalLogger = (Logger) ReflectionTestUtils.getField(generalServiceAspect, "logger");
        Logger lessonTimeLogger = (Logger) ReflectionTestUtils.getField(lessonTimeAspect, "logger");
        
        testAppender = new ListAppender<>();
        testAppender.setContext(loggerContext);
        testAppender.start();
        
        generalLogger.addAppender(testAppender);
        lessonTimeLogger.addAppender(testAppender);
    }

    @AfterEach
    void tearDown() {
        testAppender.stop();
    }

    @Test
    void shouldCreateLessonTime() {
        LocalTime startTime = LocalTime.of(9, 0);
        LocalTime endTime = LocalTime.of(11, 0);
        LessonTime lessonTime = new LessonTime();
        lessonTime.setStartTime(startTime);
        lessonTime.setEndTime(endTime);

        lessonTimeService.create(lessonTime);
        verify(lessonTimeRepository).save(lessonTime);
    }

    @Test
    void shouldGetAllLessonTimes() {
        lessonTimeService.getAll();
        verify(lessonTimeRepository).findAll();
    }

    @Test
    void shouldGetLessonTimeById() {
        int lessonTimeId = 2;
        LessonTime lessonTime = new LessonTime();
        lessonTime.setId(lessonTimeId);
        lessonTime.setStartTime(LocalTime.of(9, 0));
        lessonTime.setEndTime(LocalTime.of(10, 0));
        when(lessonTimeRepository.findById(lessonTimeId)).thenReturn(Optional.of(lessonTime));
        lessonTimeService.getById(lessonTimeId);
        verify(lessonTimeRepository).findById(lessonTimeId);
    }

    @Test
    void shouldUpdateLessonTime() {
        LocalTime startTime = LocalTime.of(9, 0);
        LocalTime endTime = LocalTime.of(11, 0);
        LessonTime lessonTime = new LessonTime();
        lessonTime.setId(1);
        lessonTime.setStartTime(startTime);
        lessonTime.setEndTime(endTime);

        lessonTimeService.update(lessonTime);
        verify(lessonTimeRepository).save(lessonTime);
    }

    @Test
    void shouldDeleteLessonTimeById() {
        int lessonTimeId = 4;
        lessonTimeService.deleteById(lessonTimeId);
        verify(lessonTimeRepository).deleteById(lessonTimeId);
    }

    @Test
    void shouldThrowServiceExceptionWhenLessonTimeIsNullWhileCreate() {
        LessonTime lessonTime = null;
        RuntimeException exception = assertThrows(ServiceException.class, () -> lessonTimeService.create(lessonTime));
    
        String message = "A given lessonTime isn't legal when create.";
        assertEquals(message, exception.getMessage());
    }

    @Test
    void shouldThrowServiceExceptionWhenLessonTimeIdIsNotZeroWhileCreate() {
        LessonTime lessonTime = new LessonTime();
        lessonTime.setId(5);
        lessonTime.setStartTime(LocalTime.of(9, 0));
        lessonTime.setEndTime(LocalTime.of(10, 0));
        RuntimeException exception = assertThrows(ServiceException.class, () -> lessonTimeService.create(lessonTime));
    
        String message = "A given lessonTime isn't legal when create.";
        assertEquals(message, exception.getMessage());
    }

    @Test
    void shouldThrowServiceExceptionWhenLessonStartTimeIsNullWhileCreate() {
        LessonTime lessonTime = new LessonTime();
        lessonTime.setEndTime(LocalTime.of(11, 0));
        RuntimeException exception = assertThrows(ServiceException.class, () -> lessonTimeService.create(lessonTime));
    
        String message = "A given lessonTime isn't valid when create.";
        assertEquals(message, exception.getMessage());
    }

    @Test
    void shouldThrowServiceExceptionWhenLessonEndTimeIsNullWhileCreate() {
        LessonTime lessonTime = new LessonTime();
        lessonTime.setStartTime(LocalTime.of(9, 0));
        RuntimeException exception = assertThrows(ServiceException.class, () -> lessonTimeService.create(lessonTime));
    
        String message = "A given lessonTime isn't valid when create.";
        assertEquals(message, exception.getMessage());
    }

    @Test
    void shouldThrowServiceExceptionWhenLessonEndTimeIsBeforeStartTimeWhileCreate() {
        LessonTime lessonTime = new LessonTime();
        lessonTime.setStartTime(LocalTime.of(11, 0));
        lessonTime.setEndTime(LocalTime.of(9, 0));
        RuntimeException exception = assertThrows(ServiceException.class, () -> lessonTimeService.create(lessonTime));
    
        String message = "A given lessonTime isn't valid when create.";
        assertEquals(message, exception.getMessage());
    }

    @Test
    void shouldThrowServiceExceptionWhenRepositoryExceptionWhileCreate() {
        LessonTime lessonTime = new LessonTime();
        lessonTime.setStartTime(LocalTime.of(9, 0));
        lessonTime.setEndTime(LocalTime.of(11, 0));
        doThrow(RepositoryException.class).when(lessonTimeRepository).save(lessonTime);
        RuntimeException exception = assertThrows(ServiceException.class, () -> lessonTimeService.create(lessonTime));
    
        String message = "There is some error in repositories layer when create object.";
        assertEquals(message, exception.getMessage());
    }

    @Test
    void shouldThrowServiceExceptionWhenRepositoryExceptionWhileGetAll() {
        when(lessonTimeRepository.findAll()).thenThrow(RepositoryException.class);
        RuntimeException exception = assertThrows(ServiceException.class, () -> lessonTimeService.getAll());
    
        String message = "There is some error in repositories layer when getAll.";
        assertEquals(message, exception.getMessage());
    }

    @Test
    void shouldThrowServiceExceptionWhenLessonTimeIdIsZeroWhileGetById() {
        int testId = 0;
        RuntimeException exception = assertThrows(ServiceException.class, () -> lessonTimeService.getById(testId));
    
        String message = "A given id is incorrect when getById.";
        assertEquals(message, exception.getMessage());
    }

    @Test
    void shouldThrowServiceExceptionWhenRepositoryExceptionWhileGetById() {
        int testId = 2;
        when(lessonTimeRepository.findById(testId)).thenThrow(RepositoryException.class);
        RuntimeException exception = assertThrows(ServiceException.class, () -> lessonTimeService.getById(testId));
    
        String message = "There is some error in repositories layer when get object by id.";
        assertEquals(message, exception.getMessage());
    }

    @Test
    void shouldThrowServiceExceptionWhenLessonTimeIsNullWhileUpdate() {
        LessonTime lessonTime = null;
        RuntimeException exception = assertThrows(ServiceException.class, () -> lessonTimeService.update(lessonTime));
    
        String message = "A given lessonTime isn't legal when update.";
        assertEquals(message, exception.getMessage());
    }

    @Test
    void shouldThrowServiceExceptionWhenLessonTimeIsInvalidWhileUpdate() {
        LessonTime lessonTime = new LessonTime();
        lessonTime.setId(-22);
        lessonTime.setStartTime(LocalTime.of(9, 0));
        lessonTime.setEndTime(LocalTime.of(11, 0));
        RuntimeException exception = assertThrows(ServiceException.class, () -> lessonTimeService.update(lessonTime));
    
        String message = "A given lessonTime isn't valid when update.";
        assertEquals(message, exception.getMessage());
    }

    @Test
    void shouldThrowServiceExceptionWhenRepositoryExceptionWhileUpdate() {
        LessonTime lessonTime = new LessonTime();
        lessonTime.setId(5);
        lessonTime.setStartTime(LocalTime.of(15, 30));
        lessonTime.setEndTime(LocalTime.of(17, 30));
        doThrow(RepositoryException.class).when(lessonTimeRepository).save(lessonTime);
        RuntimeException exception = assertThrows(ServiceException.class, () -> lessonTimeService.update(lessonTime));
    
        String message = "Can't update an object because of repositoryException.";
        assertEquals(message, exception.getMessage());
    }

    @Test
    void shouldThrowServiceExceptionWhenLessonTimeIdIsZeroWhileDeleteById() {
        int testId = 0;
        RuntimeException exception = assertThrows(ServiceException.class, () -> lessonTimeService.deleteById(testId));
    
        String message = "A given id is less than 1 when deleteById.";
        assertEquals(message, exception.getMessage());
    }

    @Test
    void shouldThrowServiceExceptionWhenRepositoryExceptionWhileDeleteById() {
        int testId = 2;
        doThrow(RepositoryException.class).when(lessonTimeRepository).deleteById(testId);
        RuntimeException exception = assertThrows(ServiceException.class, () -> lessonTimeService.deleteById(testId));
    
        String message = "There is some error in repositories layer when delete an object by id.";
        assertEquals(message, exception.getMessage());
    }

    @Test
    void shouldGenerateLogsWhenLessonTimeIsNullWhileCreate() {
        LessonTime lessonTime = null;
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to create a lessonTime: " + lessonTime + ".",
                "A lessonTime " + lessonTime + " can't be null when create."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonTimeService.create(lessonTime);
        } catch (ServiceException serviceException) {
            // do nothing
        }

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenLessonTimeIdIsNotZeroWhileCreate() {
        LessonTime lessonTime = new LessonTime();
        lessonTime.setId(8);
        lessonTime.setStartTime(LocalTime.of(9, 0));
        lessonTime.setEndTime(LocalTime.of(9, 0));
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(
                Arrays.asList("Try to create a lessonTime: " + lessonTime + ".", "The lessonTime " + lessonTime
                        + " has wrong id " + lessonTime.getId() + " which is not zero when create."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonTimeService.create(lessonTime);
        } catch (ServiceException serviceException) {
            // do nothing
        }

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenLessonStartTimeIsNullWhileCreate() {
        LessonTime lessonTime = new LessonTime();
        lessonTime.setEndTime(LocalTime.of(11, 0));
        String violationMessage = "LessonTime's startTime can't be null";
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(
                Arrays.asList("Try to create a lessonTime: " + lessonTime + ".", "The lessonTime " + lessonTime
                        + " is not valid when create. There are errors: " + violationMessage + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonTimeService.create(lessonTime);
        } catch (ServiceException serviceException) {
            // do nothing
        }

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenLessonEndTimeIsNullWhileCreate() {
        LessonTime lessonTime = new LessonTime();
        lessonTime.setStartTime(LocalTime.of(9, 0));
        String violationMessage = "LessonTime's endTime can't be null";
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(
                Arrays.asList("Try to create a lessonTime: " + lessonTime + ".", "The lessonTime " + lessonTime
                        + " is not valid when create. There are errors: " + violationMessage + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonTimeService.create(lessonTime);
        } catch (ServiceException serviceException) {
            // do nothing
        }

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenLessonEndTimeIsBeforeStartTimeWhileCreate() {
        LessonTime lessonTime = new LessonTime();
        lessonTime.setStartTime(LocalTime.of(11, 0));
        lessonTime.setEndTime(LocalTime.of(9, 0));
        String violationMessage = "LessonTime's startTime must be before endTime";
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(
                Arrays.asList("Try to create a lessonTime: " + lessonTime + ".", "The lessonTime " + lessonTime
                        + " is not valid when create. There are errors: " + violationMessage + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonTimeService.create(lessonTime);
        } catch (ServiceException serviceException) {
            // do nothing
        }

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenRepositoryExceptionWhileCreate() {
        LessonTime lessonTime = new LessonTime();
        lessonTime.setStartTime(LocalTime.of(12, 0));
        lessonTime.setEndTime(LocalTime.of(14, 0));
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to create a lessonTime: " + lessonTime + ".",
                "There is some error in repositories layer when create an object " + lessonTime + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        doThrow(RepositoryException.class).when(lessonTimeRepository).save(lessonTime);

        try {
            lessonTimeService.create(lessonTime);
        } catch (ServiceException serviceException) {
            // do nothing
        }

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenCreate() {
        LessonTime lessonTime = new LessonTime();
        lessonTime.setStartTime(LocalTime.of(15, 0));
        lessonTime.setEndTime(LocalTime.of(17, 0));
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to create a lessonTime: " + lessonTime + ".",
                "The object " + lessonTime + " was created."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonTimeService.create(lessonTime);

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenResultIsEmptyWhileGetAll() {
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.WARN));
        List<String> expectedMessages = new ArrayList<>(
                Arrays.asList("Try to get all objects.", "There are not any objects in the result when getAll."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonTimeService.getAll();

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenRepositoryExceptionWhileGetAll() {
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(
                Arrays.asList("Try to get all objects.", "There is some error in repositories layer when getAll."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        when(lessonTimeRepository.findAll()).thenThrow(RepositoryException.class);

        try {
            lessonTimeService.getAll();
        } catch (ServiceException serviceException) {
            // do nothing
        }

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenGetAll() {
        List<LessonTime> expectedLessonTimes = new ArrayList<>(
                Arrays.asList(new LessonTime(), new LessonTime(), new LessonTime()));

        for (int i = 0; i < expectedLessonTimes.size(); i++) {
            int index = i + 1;
            int startHour = 7 + (index * 2);
            int endHour = startHour + 2;
            expectedLessonTimes.get(i).setId(index);
            expectedLessonTimes.get(i).setStartTime(LocalTime.of(startHour, 0));
            expectedLessonTimes.get(i).setEndTime(LocalTime.of(endHour, 0));
        }

        when(lessonTimeRepository.findAll()).thenReturn(expectedLessonTimes);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(
                Arrays.asList("Try to get all objects.", "The result is: " + expectedLessonTimes + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonTimeService.getAll();

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenLessonTimeIdIsNegativeWhileGetById() {
        int negativeId = -5;

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to get an object by id: " + negativeId + ".",
                "A given id " + negativeId + " is less than 1 when getById."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonTimeService.getById(negativeId);
        } catch (ServiceException serviceException) {
            // do nothing
        }

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenEntityIsNotFoundInDatabaseWhileGetById() {
        int testId = 8;

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to get an object by id: " + testId + ".",
                "The entity is not found when get object by id " + testId + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        RepositoryException repositoryException = new RepositoryException("The result is empty", new NullPointerException());
        when(lessonTimeRepository.findById(testId)).thenThrow(repositoryException);

        try {
            lessonTimeService.getById(testId);
        } catch (ServiceException serviceException) {
            // do nothing
        }

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenRepositoryExceptionWhileGetById() {
        int testId = 5;

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to get an object by id: " + testId + ".",
                "There is some error in repositories layer when get object by id " + testId + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        when(lessonTimeRepository.findById(testId)).thenThrow(RepositoryException.class);

        try {
            lessonTimeService.getById(testId);
        } catch (ServiceException serviceException) {
            // do nothing
        }

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenGetById() {
        int testId = 2;

        LessonTime expectedLessonTime = new LessonTime();
        expectedLessonTime.setId(testId);
        expectedLessonTime.setStartTime(LocalTime.of(12, 0));
        expectedLessonTime.setEndTime(LocalTime.of(14, 0));

        when(lessonTimeRepository.findById(testId)).thenReturn(Optional.of(expectedLessonTime));

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to get an object by id: " + testId + ".",
                "The result object with id " + testId + " is " + expectedLessonTime + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonTimeService.getById(testId);

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenLessonTimeIsNullWhileUpdate() {
        LessonTime lessonTime = null;

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to update a lessonTime: " + lessonTime + ".",
                "An updated lessonTime " + lessonTime + " is null."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonTimeService.update(lessonTime);
        } catch (ServiceException serviceException) {
            // do nothing
        }

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenLessonTimeIsInvalidWhileUpdate() {
        LessonTime lessonTime = new LessonTime();
        lessonTime.setId(3);
        lessonTime.setStartTime(LocalTime.of(11, 0));
        lessonTime.setEndTime(LocalTime.of(10, 0));

        String violationMessage = "LessonTime's startTime must be before endTime";

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(
                Arrays.asList("Try to update a lessonTime: " + lessonTime + ".", "The lessonTime " + lessonTime
                        + " is not valid when update. There are errors: " + violationMessage + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonTimeService.update(lessonTime);
        } catch (ServiceException serviceException) {
            // do nothing
        }

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenRepositoryExceptionWhileUpdate() {
        LessonTime lessonTime = new LessonTime();
        lessonTime.setId(1);
        lessonTime.setStartTime(LocalTime.of(9, 0));
        lessonTime.setEndTime(LocalTime.of(11, 0));

        doThrow(RepositoryException.class).when(lessonTimeRepository).save(lessonTime);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to update a lessonTime: " + lessonTime + ".",
                "There is some error in repositories layer when update an object " + lessonTime + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonTimeService.update(lessonTime);
        } catch (ServiceException serviceException) {
            // do nothing
        }

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenUpdate() {
        LessonTime lessonTime = new LessonTime();
        lessonTime.setId(2);
        lessonTime.setStartTime(LocalTime.of(12, 0));
        lessonTime.setEndTime(LocalTime.of(14, 0));

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to update a lessonTime: " + lessonTime + ".",
                "The object " + lessonTime + " was updated."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonTimeService.update(lessonTime);
        } catch (ServiceException serviceException) {
            // do nothing
        }

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenFacultyIdIsNegativeWhileDeleteById() {
        int testId = -7;

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to delete an object by id: " + testId + ".",
                "A given id " + testId + " is less than 1 when deleteById."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonTimeService.deleteById(testId);
        } catch (ServiceException serviceException) {
            // do nothing
        }

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenRepositoryExceptionWhileDeleteById() {
        int testId = 4;

        doThrow(RepositoryException.class).when(lessonTimeRepository).deleteById(testId);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to delete an object by id: " + testId + ".",
                "There is some error in repositories layer when delete an object by id " + testId + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lessonTimeService.deleteById(testId);
        } catch (ServiceException serviceException) {
            // do nothing
        }

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenDeleteById() {
        int testId = 2;

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to delete an object by id: " + testId + ".",
                "An object was deleted by id " + testId + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lessonTimeService.deleteById(testId);

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }
}