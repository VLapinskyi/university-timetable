package ua.com.foxminded.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

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
import ua.com.foxminded.domain.Faculty;
import ua.com.foxminded.repositories.exceptions.RepositoryException;
import ua.com.foxminded.repositories.interfaces.FacultyRepository;
import ua.com.foxminded.service.aspects.FacultyAspect;
import ua.com.foxminded.service.aspects.GeneralServiceAspect;
import ua.com.foxminded.service.exceptions.ServiceException;
import ua.com.foxminded.settings.SpringTestConfiguration;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = SpringTestConfiguration.class)
class FacultyServiceTest {
    private ListAppender<ILoggingEvent> testAppender;
    
    @Autowired
    private GeneralServiceAspect generalServiceAspect;
    
    @Autowired
    private FacultyAspect facultyAspect;
    
    @Autowired
    private FacultyService facultyService;

    @MockBean
    private FacultyRepository facultyRepository;

    @BeforeEach
    void init() {
        ReflectionTestUtils.setField(facultyService, "facultyRepository", facultyRepository);
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger generalLogger = (Logger) ReflectionTestUtils.getField(generalServiceAspect, "logger");
        Logger facultyLogger = (Logger) ReflectionTestUtils.getField(facultyAspect, "logger");
        
        testAppender = new ListAppender<>();
        testAppender.setContext(loggerContext);
        testAppender.start();
        
        generalLogger.addAppender(testAppender);
        facultyLogger.addAppender(testAppender);
    }

    @AfterEach
    void tearDown() {
        testAppender.stop();
    }

    @Test
    void shouldCreate() {
        String facultyName = "TestName";
        Faculty faculty = new Faculty();
        faculty.setName(facultyName);
        facultyService.create(faculty);
        verify(facultyRepository).save(faculty);
    }

    @Test
    void shouldGetAll() {
        facultyService.getAll();
        verify(facultyRepository).findAll();
    }

    @Test
    void shouldGetFacultyById() {
        int facultyId = 5;
        Faculty faculty = new Faculty();
        faculty.setName("Faculty");
        faculty.setId(facultyId);
        when(facultyRepository.findById(facultyId)).thenReturn(Optional.of(faculty));
        
        facultyService.getById(facultyId);
        verify(facultyRepository).findById(facultyId);
    }

    @Test
    void shouldUpdateFaculty() {
        int facultyId = 3;
        String testName = "Faculty Name";
        Faculty faculty = new Faculty();
        faculty.setName(testName);
        faculty.setId(facultyId);
        facultyService.update(faculty);
        verify(facultyRepository).save(faculty);
    }

    @Test
    void shouldDeleteFacultyById() {
        int facultyId = 1;
        facultyService.deleteById(facultyId);
        verify(facultyRepository).deleteById(facultyId);
    }

    @Test
    void shouldThrowServiceExceptionWhenFacultyIsNullWhileCreate() {
        Faculty faculty = null;
        RuntimeException  exception = assertThrows(ServiceException.class, () -> facultyService.create(faculty));
    
        String message = "A given faculty isn't legal when create.";
        assertEquals(message, exception.getMessage());
    }

    @Test
    void shouldThrowServiceExceptionWhenFacultyIdIsNotZeroWhileCreate() {
        Faculty faculty = new Faculty();
        faculty.setId(5);
        faculty.setName("Test name");
        RuntimeException  exception = assertThrows(ServiceException.class, () -> facultyService.create(faculty));
        
        String message = "A given faculty isn't legal when create.";
        assertEquals(message, exception.getMessage());
    }

    @Test
    void shouldThrowServiceExceptionWhenFacultyNameIsNullWhileCreate() {
        Faculty faculty = new Faculty();
        RuntimeException exception = assertThrows(ServiceException.class, () -> facultyService.create(faculty));
        
        String message = "A given faculty isn't valid when create.";
        assertEquals(message, exception.getMessage());
    }

    @Test
    void shouldThrowServiceExceptionWhenFacultyNameIsShortWhileCreate() {
        Faculty faculty = new Faculty();
        faculty.setName("q   ");
        RuntimeException exception = assertThrows(ServiceException.class, () -> facultyService.create(faculty));
        
        String message = "A given faculty isn't valid when create.";
        assertEquals(message, exception.getMessage());
    }

    @Test
    void shouldThrowServiceExceptionWhenFacultyNameStartsWithWhiteSpaceWhileCreate() {
        Faculty faculty = new Faculty();
        faculty.setName(" Test name");
        RuntimeException exception = assertThrows(ServiceException.class, () -> facultyService.create(faculty));
        
        String message = "A given faculty isn't valid when create.";
        assertEquals(message, exception.getMessage());
    }

    @Test
    void shouldThrowServiceExceptionWhenRepositoryExceptionWhileCreate() {
        Faculty faculty = new Faculty();
        faculty.setName("Test faculty");
        doThrow(RepositoryException.class).when(facultyRepository).save(faculty);
        RuntimeException exception = assertThrows(ServiceException.class, () -> facultyService.create(faculty));
        
        String message = "There is some error in repositories layer when create object.";
        assertEquals(message, exception.getMessage());
    }

    @Test
    void shouldThrowServiceExceptionWhenRepositoryExceptionWhileGetAll() {
        when(facultyRepository.findAll()).thenThrow(RepositoryException.class);
        RuntimeException exception = assertThrows(ServiceException.class, () -> facultyService.getAll());
        
        String message = "There is some error in repositories layer when getAll.";
        
        assertEquals(message, exception.getMessage());
    }

    @Test
    void shouldThrowServiceExceptionWhenFacultyIdIsZeroWhileGetById() {
        int testId = 0;
        RuntimeException exception = assertThrows(ServiceException.class, () -> facultyService.getById(testId));
        
        String message = "A given id is incorrect when getById.";
        assertEquals(message, exception.getMessage());
    }

    @Test
    void shouldThrowServiceExceptionWhenRepositoryExceptionWhileGetById() {
        int testId = 2;
        when(facultyRepository.findById(testId)).thenThrow(RepositoryException.class);
        RuntimeException exception = assertThrows(ServiceException.class, () -> facultyService.getById(testId));
        
        String message = "There is some error in repositories layer when get object by id.";
        assertEquals(message, exception.getMessage());
    }

    @Test
    void shouldThrowServiceExceptionWhenFacultyIsNullWhileUpdate() {
        Faculty faculty = null;
        RuntimeException exception = assertThrows(ServiceException.class, () -> facultyService.update(faculty));
        
        String message = "A given faculty isn't legal when update.";
        assertEquals(message, exception.getMessage());
    }

    @Test
    void shouldThrowServiceExceptionWhenFacultyIsInvalidWhileUpdate() {
        Faculty faculty = new Faculty();
        faculty.setId(-22);
        faculty.setName(" Test name");
        RuntimeException exception = assertThrows(ServiceException.class, () -> facultyService.update(faculty));
        
        String message = "A given faculty isn't valid when update.";
        assertEquals(message, exception.getMessage());
    }

    @Test
    void shouldThrowServiceExceptionWhenRepositoryExceptionWhileUpdate() {
        Faculty faculty = new Faculty();
        faculty.setId(12);
        faculty.setName("Test faculty");
        doThrow(RepositoryException.class).when(facultyRepository).save(faculty);
        RuntimeException exception = assertThrows(ServiceException.class, () -> facultyService.update(faculty));
        
        String message = "Can't update an object because of repositoryException.";
        assertEquals(message, exception.getMessage());
    }

    @Test
    void shouldThrowServiceExceptionWhenFacultyIdIsZeroWhileDeleteById() {
        int testId = 0;
        RuntimeException exception = assertThrows(ServiceException.class, () -> facultyService.deleteById(testId));
        
        String message = "A given id is less than 1 when deleteById.";
        assertEquals(message, exception.getMessage());
    }

    @Test
    void shouldThrowServiceExceptionWhenRepositoryExceptionWhileDeleteById() {
        int testId = 2;
        doThrow(RepositoryException.class).when(facultyRepository).deleteById(testId);
        RuntimeException exception = assertThrows(ServiceException.class, () -> facultyService.deleteById(testId));
        
        String message = "There is some error in repositories layer when delete an object by id.";
        assertEquals(message, exception.getMessage());
    }

    @Test
    void shouldGenerateLogsWhenFacultyIsNullWhileCreate() {
        Faculty faculty = null;
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to create a new faculty: " + faculty + ".",
                "A faculty " + faculty + " can't be null when create."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            facultyService.create(faculty);
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
    void shouldGenerateLogsWhenFacultyIdIsNotZeroWhileCreate() {
        Faculty faculty = new Faculty();
        faculty.setId(8);
        faculty.setName("Test name");
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to create a new faculty: " + faculty + ".",
                "A faculty " + faculty + " has wrong id " + faculty.getId() + " which is not equal zero when create."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            facultyService.create(faculty);
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
    void shouldGenerateLogsWhenFacultyNameIsNullWhileCreate() {
        Faculty faculty = new Faculty();
        String violationMessage = "Faculty name can't be null";
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to create a new faculty: " + faculty + ".",
                "The faculty " + faculty + " is not valid when create. There are errors: " + violationMessage + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            facultyService.create(faculty);
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
    void shouldGenerateLogsWhenFacultyNameIsNotValidWhileCreate() {
        Faculty faculty = new Faculty();
        faculty.setName("y  ");
        String violationMessage = "Faculty name must have at least two symbols and start with non-white space";
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to create a new faculty: " + faculty + ".",
                "The faculty " + faculty + " is not valid when create. There are errors: " + violationMessage + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            facultyService.create(faculty);
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
        Faculty faculty = new Faculty();
        faculty.setName("Test Name");
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to create a new faculty: " + faculty + ".",
                "There is some error in repositories layer when create an object " + faculty + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        doThrow(RepositoryException.class).when(facultyRepository).save(faculty);

        try {
            facultyService.create(faculty);
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
        Faculty faculty = new Faculty();
        faculty.setName("Test Name");
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to create a new faculty: " + faculty + ".",
                "The object " + faculty + " was created."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        facultyService.create(faculty);

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

        facultyService.getAll();

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

        when(facultyRepository.findAll()).thenThrow(RepositoryException.class);

        try {
            facultyService.getAll();
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
        List<Faculty> expectedFaculties = new ArrayList<>(Arrays.asList(new Faculty(), new Faculty(), new Faculty()));

        for (int i = 0; i < expectedFaculties.size(); i++) {
            int index = i + 1;
            expectedFaculties.get(i).setId(index);
            expectedFaculties.get(i).setName("Test name: " + index);
        }

        when(facultyRepository.findAll()).thenReturn(expectedFaculties);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(
                Arrays.asList("Try to get all objects.", "The result is: " + expectedFaculties + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        facultyService.getAll();

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenFacultyIdIsNegativeWhileGetById() {
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
            facultyService.getById(negativeId);
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
        int testId = 9;

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to get an object by id: " + testId + ".",
                "The entity is not found when get object by id " + testId + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }
        RepositoryException repositoryException = new RepositoryException("The result is empty", new NullPointerException());
        when(facultyRepository.findById(testId)).thenThrow(repositoryException);

        try {
            facultyService.getById(testId);
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

        when(facultyRepository.findById(testId)).thenThrow(RepositoryException.class);

        try {
            facultyService.getById(testId);
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
        int testId = 5;

        Faculty expectedFaculty = new Faculty();
        expectedFaculty.setId(testId);
        expectedFaculty.setName("Test name");

        when(facultyRepository.findById(testId)).thenReturn(Optional.of(expectedFaculty));

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to get an object by id: " + testId + ".",
                "The result object with id " + testId + " is " + expectedFaculty + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        facultyService.getById(testId);

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenFacultyIsNullWhileUpdate() {
        Faculty testFaculty = null;

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to update a faculty: " + testFaculty + ".",
                "An updated faculty " + testFaculty + " is null."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            facultyService.update(testFaculty);
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
    void shouldGenerateLogsWhenFacultyIdIsZeroWhileUpdate() {
        Faculty testFaculty = new Faculty();
        testFaculty.setName("Test name");

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(
                Arrays.asList("Try to update a faculty: " + testFaculty + ".", "An updated faculty " + testFaculty
                        + " has wrong id " + testFaculty.getId() + " which is not positive."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            facultyService.update(testFaculty);
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
    void shouldGenerateLogsWhenFacultyNameIsNullWhileUpdate() {
        Faculty testFaculty = new Faculty();
        testFaculty.setId(9);
        String violationMessage = "Faculty name can't be null";

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(
                Arrays.asList("Try to update a faculty: " + testFaculty + ".", "The faculty " + testFaculty
                        + " is not valid when update. There are errors: " + violationMessage + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            facultyService.update(testFaculty);
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
    void shouldGenerateLogsWhenFacultyNameIsNotValidWhileUpdate() {
        Faculty testFaculty = new Faculty();
        testFaculty.setId(9);
        testFaculty.setName("      b   ");

        String violationMessage = "Faculty name must have at least two symbols and start with non-white space";

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(
                Arrays.asList("Try to update a faculty: " + testFaculty + ".", "The faculty " + testFaculty
                        + " is not valid when update. There are errors: " + violationMessage + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            facultyService.update(testFaculty);
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
        Faculty testFaculty = new Faculty();
        testFaculty.setId(9);
        testFaculty.setName("Test name");

        doThrow(RepositoryException.class).when(facultyRepository).save(testFaculty);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to update a faculty: " + testFaculty + ".",
                "There is some error in repositories layer when update an object " + testFaculty + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            facultyService.update(testFaculty);
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
        Faculty testFaculty = new Faculty();
        testFaculty.setId(12);
        testFaculty.setName("Test name");

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to update a faculty: " + testFaculty + ".",
                "The object " + testFaculty + " was updated."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        facultyService.update(testFaculty);

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
            facultyService.deleteById(testId);
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
        int testId = 7;

        doThrow(RepositoryException.class).when(facultyRepository).deleteById(testId);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to delete an object by id: " + testId + ".",
                "There is some error in repositories layer when delete an object by id " + testId + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            facultyService.deleteById(testId);
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
        int testId = 7;

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to delete an object by id: " + testId + ".",
                "An object was deleted by id " + testId + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        facultyService.deleteById(testId);

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }
}