package ua.com.foxminded.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.doNothing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ua.com.foxminded.dao.FacultyDAO;
import ua.com.foxminded.dao.exceptions.DAOException;
import ua.com.foxminded.domain.Faculty;
import ua.com.foxminded.service.exceptions.NotValidObjectException;
import ua.com.foxminded.service.exceptions.ServiceException;
import ua.com.foxminded.settings.TestAppender;

class FacultyServiceTest {
    private TestAppender testAppender = new TestAppender();
    @Spy
    @InjectMocks
    private FacultyService facultyService;
    @Mock
    private FacultyDAO facultyDAO;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown () {
        testAppender.cleanEventList();
    }

    @Test
    void shouldCreateFaculty() {
        String facultyName = "TestName";
        Faculty faculty = new Faculty();
        faculty.setName(facultyName);
        facultyService.createFaculty(faculty);
        verify(facultyDAO).create(faculty);
    }

    @Test
    void shouldGetAllFaculties() {
        facultyService.getAllFaculties();
        verify(facultyDAO).findAll();
    }

    @Test
    void shouldFindFacultyById() {
        int facultyId = 5;
        facultyService.getFacultyById(facultyId);
        verify(facultyDAO).findById(facultyId);
    }

    @Test
    void shouldUpdateFaculty() {
        int facultyId = 3;
        String testName = "Faculty Name";
        Faculty faculty = new Faculty();
        faculty.setName(testName);
        faculty.setId(facultyId);
        facultyService.updateFaculty(faculty);
        verify(facultyDAO).update(facultyId, faculty);
    }

    @Test
    void shouldDeleteFacultyById() {
        int facultyId = 1;
        facultyService.deleteFacultyById(facultyId);
        verify(facultyDAO).deleteById(facultyId);
    }

    @Test
    void shouldThrowNotValidObjectExceptionWhenValidateFacultyIsNull() {
        Faculty faculty = null;
        assertThrows(NotValidObjectException.class, () -> facultyService.validateFaculty(faculty));
    }

    @Test
    void shouldThrowNotValidObjectExceptionWhenFacultyNameIsNotValid() {
        Faculty faculty = new Faculty();
        assertThrows(NotValidObjectException.class, () -> facultyService.validateFaculty(faculty));
        faculty.setName("  1   ");
        assertThrows(NotValidObjectException.class, () -> facultyService.validateFaculty(faculty));
    }

    @Test
    void shouldGenerateLogsWhenFacultyIsNull() {
        Faculty faculty = null;
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent()));
        expectedLogs.get(0).setLevel(Level.ERROR);
        expectedLogs.get(0).setMessage("The faculty " + faculty + " is null.");

        try {
            facultyService.validateFaculty(faculty);
        } catch (NotValidObjectException notValidObjectException) {
            //do nothing
        }

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }    
    }

    @Test
    void shouldGenerateLogsWhenFacultyNameIsNull() {
        Faculty faculty = new Faculty();
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent()));
        expectedLogs.get(0).setLevel(Level.ERROR);
        expectedLogs.get(0).setMessage("The faculty's name " + faculty.getName() + " is not valid.");

        try {
            facultyService.validateFaculty(faculty);
        } catch (NotValidObjectException notValidObjectException) {
            //do nothing
        }

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }    
    }

    @Test
    void shouldGenerateLogsWhenFacultyHasNotValidName() {
        Faculty faculty = new Faculty();
        faculty.setName("  h ");
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent()));
        expectedLogs.get(0).setLevel(Level.ERROR);
        expectedLogs.get(0).setMessage("The faculty's name " + faculty.getName() + " is not valid.");

        try {
            facultyService.validateFaculty(faculty);
        } catch (NotValidObjectException notValidObjectException) {
            //do nothing
        }

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }    
    }

    @Test
    void shouldGenerateLogsWhenCreateFaculty() {
        Faculty testFaculty = new Faculty();
        doNothing().when(facultyService).validateFaculty(testFaculty);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to create new faculty: " + testFaculty + ".",
                "The faculty " + testFaculty + " was created."));

        for (int a = 0; a < expectedLogs.size(); a++) {
            expectedLogs.get(a).setLevel(expectedLevels.get(a));
            expectedLogs.get(a).setMessage(expectedMessages.get(a));
        }

        facultyService.createFaculty(testFaculty);

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int a = 0; a < actualLogs.size(); a++) {
            assertEquals(expectedLogs.get(a).getLevel(), actualLogs.get(a).getLevel());
            assertEquals(expectedLogs.get(a).getFormattedMessage(), actualLogs.get(a).getFormattedMessage());
        }    
    }

    @Test
    void shouldGenerateLogsWhenCreateFacultyIdIsNotZero() {
        Faculty faculty = new Faculty();
        faculty.setName("TestName");
        int[] idNumbers = new int [] {-5, 3, 1, 9, -1, 2, 7, 15, 100, -1000};
        for (int i = 0 ; i < idNumbers.length; i++) {
            faculty.setId(idNumbers[i]);
            List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                    new LoggingEvent(), new LoggingEvent()));
            List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                    Level.DEBUG, Level.ERROR));
            List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                    "Try to create new faculty: " + faculty + ".",
                    "The faculty " + faculty + " has setted id " + faculty.getId() + " and it is different from zero."));

            for (int a = 0; a < expectedLogs.size(); a++) {
                expectedLogs.get(a).setLevel(expectedLevels.get(a));
                expectedLogs.get(a).setMessage(expectedMessages.get(a));
            }

            try {
                facultyService.createFaculty(faculty);
            } catch (ServiceException serviceException) {
                //do nothing
            }

            List<ILoggingEvent> actualLogs = testAppender.getEvents();

            assertEquals(expectedLogs.size(), actualLogs.size());
            for (int a = 0; a < actualLogs.size(); a++) {
                assertEquals(expectedLogs.get(a).getLevel(), actualLogs.get(a).getLevel());
                assertEquals(expectedLogs.get(a).getFormattedMessage(), actualLogs.get(a).getFormattedMessage());
            }    
            testAppender.cleanEventList();
        }
    }

    @Test
    void shouldGenerateLogsWhenThrowDAOExceptionWhileCreateFaculty() {
        Faculty faculty = new Faculty();
        doNothing().when(facultyService).validateFaculty(faculty);
        doThrow(DAOException.class).when(facultyDAO).create(faculty);
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to create new faculty: " + faculty + ".",
                "Can't create faculty " + faculty + "."));

        for (int a = 0; a < expectedLogs.size(); a++) {
            expectedLogs.get(a).setLevel(expectedLevels.get(a));
            expectedLogs.get(a).setMessage(expectedMessages.get(a));
        }

        try {
            facultyService.createFaculty(faculty);
        } catch (ServiceException serviceException) {
            //do nothing
        }

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }    
    }

    @Test
    void shouldGenerateLogsWhenGetAllFaculties() {
        List<Faculty> expectedFaculties = new ArrayList<>(Arrays.asList(
                new Faculty(), new Faculty()));
        List<String> facultyNames = new ArrayList<>(Arrays.asList(
                "Faculty-1", "Faculty-2"));

        for (int i = 0; i < expectedFaculties.size(); i++) {
            expectedFaculties.get(i).setId(++i);
            expectedFaculties.get(i).setName(facultyNames.get(i));
        }

        when(facultyDAO.findAll()).thenReturn(expectedFaculties);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to get all faculties.",
                "The result is: " + expectedFaculties + "."));

        for (int a = 0; a < expectedLogs.size(); a++) {
            expectedLogs.get(a).setLevel(expectedLevels.get(a));
            expectedLogs.get(a).setMessage(expectedMessages.get(a));
        }

        facultyService.getAllFaculties();

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int a = 0; a < actualLogs.size(); a++) {
            assertEquals(expectedLogs.get(a).getLevel(), actualLogs.get(a).getLevel());
            assertEquals(expectedLogs.get(a).getFormattedMessage(), actualLogs.get(a).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenEmptyListWhileGetAllFaculties() {
        List<Faculty> expectedFaculties = new ArrayList<>();

        when(facultyDAO.findAll()).thenReturn(expectedFaculties);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.WARN));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to get all faculties.",
                "There are not any faculties in the result."));

        for (int a = 0; a < expectedLogs.size(); a++) {
            expectedLogs.get(a).setLevel(expectedLevels.get(a));
            expectedLogs.get(a).setMessage(expectedMessages.get(a));
        }

        facultyService.getAllFaculties();

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int a = 0; a < actualLogs.size(); a++) {
            assertEquals(expectedLogs.get(a).getLevel(), actualLogs.get(a).getLevel());
            assertEquals(expectedLogs.get(a).getFormattedMessage(), actualLogs.get(a).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenThrowDAOExceptionWhileGetAllFaculties() {
        when(facultyDAO.findAll()).thenThrow(DAOException.class);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to get all faculties.",
                "Can't get all faculties."));

        for (int a = 0; a < expectedLogs.size(); a++) {
            expectedLogs.get(a).setLevel(expectedLevels.get(a));
            expectedLogs.get(a).setMessage(expectedMessages.get(a));
        }

        try {
            facultyService.getAllFaculties();
        } catch (ServiceException serviceException) {
            ///do nothing
        }

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int a = 0; a < actualLogs.size(); a++) {
            assertEquals(expectedLogs.get(a).getLevel(), actualLogs.get(a).getLevel());
            assertEquals(expectedLogs.get(a).getFormattedMessage(), actualLogs.get(a).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenGetFacultyById() {
        int facultyId = 1;
        Faculty expectedFaculty = new Faculty();
        expectedFaculty.setId(facultyId);
        expectedFaculty.setName("Test faculty");
        when(facultyDAO.findById(facultyId)).thenReturn(expectedFaculty);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to get faculty by id " + facultyId + ".",
                "The result faculty with id " + facultyId + " is " + expectedFaculty + "."));

        for (int a = 0; a < expectedLogs.size(); a++) {
            expectedLogs.get(a).setLevel(expectedLevels.get(a));
            expectedLogs.get(a).setMessage(expectedMessages.get(a));
        }

        facultyService.getFacultyById(facultyId);        
        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int a = 0; a < actualLogs.size(); a++) {
            assertEquals(expectedLogs.get(a).getLevel(), actualLogs.get(a).getLevel());
            assertEquals(expectedLogs.get(a).getFormattedMessage(), actualLogs.get(a).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenThrowDAOExceptionWhileGetFacultyById() {
        int facultyId = 1;
        when(facultyDAO.findById(facultyId)).thenThrow(DAOException.class);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to get faculty by id " + facultyId + ".",
                "Can't get faculty by id " + facultyId + "."));

        for (int a = 0; a < expectedLogs.size(); a++) {
            expectedLogs.get(a).setLevel(expectedLevels.get(a));
            expectedLogs.get(a).setMessage(expectedMessages.get(a));
        }

        try {
            facultyService.getFacultyById(facultyId);
        } catch (ServiceException serviceException) {
            //do nothing
        }

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int a = 0; a < actualLogs.size(); a++) {
            assertEquals(expectedLogs.get(a).getLevel(), actualLogs.get(a).getLevel());
            assertEquals(expectedLogs.get(a).getFormattedMessage(), actualLogs.get(a).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenUpdateFaculty() {
        Faculty faculty = new Faculty();
        faculty.setId(1);
        faculty.setName("Test");

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to update faculty " + faculty + ".",
                "The faculty " + faculty + " was updated."));

        for (int a = 0; a < expectedLogs.size(); a++) {
            expectedLogs.get(a).setLevel(expectedLevels.get(a));
            expectedLogs.get(a).setMessage(expectedMessages.get(a));
        }

        facultyService.updateFaculty(faculty);

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int a = 0; a < actualLogs.size(); a++) {
            assertEquals(expectedLogs.get(a).getLevel(), actualLogs.get(a).getLevel());
            assertEquals(expectedLogs.get(a).getFormattedMessage(), actualLogs.get(a).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenUpdateFacultyWithIncorrectId() {
        Faculty faculty = new Faculty();
        faculty.setId(0);
        faculty.setName("Test");

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to update faculty " + faculty + ".",
                "The updated faculty " + faculty + " has incorrect id " + faculty.getId() + "."));

        for (int a = 0; a < expectedLogs.size(); a++) {
            expectedLogs.get(a).setLevel(expectedLevels.get(a));
            expectedLogs.get(a).setMessage(expectedMessages.get(a));
        }

        try {
            facultyService.updateFaculty(faculty);
        } catch (ServiceException serviceException) {
            // do nothing
        }

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int a = 0; a < actualLogs.size(); a++) {
            assertEquals(expectedLogs.get(a).getLevel(), actualLogs.get(a).getLevel());
            assertEquals(expectedLogs.get(a).getFormattedMessage(), actualLogs.get(a).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenThrowDAOExceptionWhileUpdateFaculty() {
        Faculty faculty = new Faculty();
        faculty.setId(2);
        doNothing().when(facultyService).validateFaculty(faculty);

        doThrow(DAOException.class).when(facultyDAO).update(faculty.getId(), faculty);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to update faculty " + faculty + ".",
                "Can't update faculty " + faculty + "."));

        for (int a = 0; a < expectedLogs.size(); a++) {
            expectedLogs.get(a).setLevel(expectedLevels.get(a));
            expectedLogs.get(a).setMessage(expectedMessages.get(a));
        }

        try {
            facultyService.updateFaculty(faculty);
        } catch (ServiceException serviceException) {
            // do nothing
        }

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int a = 0; a < actualLogs.size(); a++) {
            assertEquals(expectedLogs.get(a).getLevel(), actualLogs.get(a).getLevel());
            assertEquals(expectedLogs.get(a).getFormattedMessage(), actualLogs.get(a).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenDeleteFacultyById() {
        int facultyId = 1;

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to delete faculty by id " + facultyId + ".",
                "The faculty with id " + facultyId + " was deleted."));

        for (int a = 0; a < expectedLogs.size(); a++) {
            expectedLogs.get(a).setLevel(expectedLevels.get(a));
            expectedLogs.get(a).setMessage(expectedMessages.get(a));
        }

        facultyService.deleteFacultyById(facultyId);

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int a = 0; a < actualLogs.size(); a++) {
            assertEquals(expectedLogs.get(a).getLevel(), actualLogs.get(a).getLevel());
            assertEquals(expectedLogs.get(a).getFormattedMessage(), actualLogs.get(a).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenThrowDAOExceptionWhileDeleteFacultyById() {
        int facultyId = 1;

        doThrow(DAOException.class).when(facultyDAO).deleteById(facultyId);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to delete faculty by id " + facultyId + ".",
                "Can't delete faculty by id " + facultyId + "."));

        for (int a = 0; a < expectedLogs.size(); a++) {
            expectedLogs.get(a).setLevel(expectedLevels.get(a));
            expectedLogs.get(a).setMessage(expectedMessages.get(a));
        }

        try {
            facultyService.deleteFacultyById(facultyId);
        } catch (ServiceException serviceException) {
            //do nothing
        }

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int a = 0; a < actualLogs.size(); a++) {
            assertEquals(expectedLogs.get(a).getLevel(), actualLogs.get(a).getLevel());
            assertEquals(expectedLogs.get(a).getFormattedMessage(), actualLogs.get(a).getFormattedMessage());
        }
    }
}
