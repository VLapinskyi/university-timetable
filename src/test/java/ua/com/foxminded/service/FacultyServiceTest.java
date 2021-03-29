package ua.com.foxminded.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.NestedRuntimeException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ua.com.foxminded.dao.FacultyDAO;
import ua.com.foxminded.domain.Faculty;
import ua.com.foxminded.service.exceptions.NotValidObjectException;
import ua.com.foxminded.settings.TestAppender;

class FacultyServiceTest {
    private TestAppender testAppender = new TestAppender();
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
        facultyService.updateFaculty(facultyId, faculty);
        verify(facultyDAO).update(facultyId, faculty);
    }

    @Test
    void shouldDeleteFacultyById() {
        int facultyId = 1;
        facultyService.deleteFacultyById(facultyId);
        verify(facultyDAO).deleteById(facultyId);
    }

    @Test
    void shouldThrowNotValidObjectExceptionWhenCreatedFacultyIsNull() {
        Faculty faculty = null;
        assertThrows(NotValidObjectException.class, () -> facultyService.createFaculty(faculty));
    }

    @Test
    void shouldThrowNotValidObjectExceptionWhenCreateFacultyWithIdIsNotZero() {
        Faculty faculty = new Faculty();
        faculty.setName("TestName");
        int[] idNumbers = new int [] {-5, 3, 1, 9, -1, 2, 7, 15, 100, -1000};
        for (int i = 0 ; i < idNumbers.length; i++) {
            faculty.setId(idNumbers[i]);
            assertThrows(NotValidObjectException.class, () -> facultyService.createFaculty(faculty));
        }
    }

    @Test
    void shouldThrowNotValidObjectExceptionWhenCreateFacultyWithIncorectName() {
        Faculty faculty = new Faculty();
        faculty.setName(null);
        assertThrows(NotValidObjectException.class, () -> facultyService.createFaculty(faculty));
        faculty.setName("     q");
        assertThrows(NotValidObjectException.class, () -> facultyService.createFaculty(faculty));
    }
    
    @Test
    void shouldThrowDAOExceptionWhenSQLException() {
        Faculty faculty = new Faculty();
        faculty.setName("TestName");
        doThrow(DataIntegrityViolationException.class).when(facultyDAO).create(faculty);
        //assertThrows(DAOException.class, () -> facultyService.createFaculty(faculty));
    }

    @Test
    void shouldGenerateLogsWhenCreatedFacultyIsNull() {
        Faculty faculty = null;
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to create new faculty: " + faculty + ".",
                "The faculty " + faculty + " is null."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }
        
        try {
            facultyService.createFaculty(faculty);
        } catch (NotValidObjectException exception) {
            //do nothing
        }

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(actualLogs.size(), expectedLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }    
    }

    @Test
    void shouldGenerateLogsWhenCreateFacultyWithIdIsNotZero() {
        Faculty faculty = new Faculty();
        faculty.setName("TestName");
        int[] idNumbers = new int [] {-5, 3, 1, 9, -1, 2, 7, 15, 100, -1000};
        for (int i = 0 ; i < idNumbers.length; i++) {
            faculty.setId(idNumbers[i]);
            List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                    new LoggingEvent(), new LoggingEvent()));
            List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                    Level.DEBUG, Level.WARN));
            List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                    "Try to create new faculty: " + faculty + ".",
                    "The faculty has setted id " + faculty.getId() + " and it is different from zero."));

            for (int a = 0; a < expectedLogs.size(); a++) {
                expectedLogs.get(a).setLevel(expectedLevels.get(a));
                expectedLogs.get(a).setMessage(expectedMessages.get(a));
            }

            try {
                facultyService.createFaculty(faculty);
            } catch (NotValidObjectException exception) {
                //do nothing
            }

            List<ILoggingEvent> actualLogs = testAppender.getEvents();

            assertEquals(actualLogs.size(), expectedLogs.size());
            for (int a = 0; a < actualLogs.size(); a++) {
                assertEquals(expectedLogs.get(a).getLevel(), actualLogs.get(a).getLevel());
                assertEquals(expectedLogs.get(a).getFormattedMessage(), actualLogs.get(a).getFormattedMessage());
            }    
            testAppender.cleanEventList();
        }
    }

    @Test
    void shouldGenerateLogsWhenCreateFacultyWithIncorectName() {
        Faculty faculty = new Faculty();
        faculty.setName("a  ");
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to create new faculty: " + faculty + ".",
                "The faculty's name " + faculty.getName() + " is not valid."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            facultyService.createFaculty(faculty);
        } catch (NotValidObjectException exception) {
            //do nothing
        }

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(actualLogs.size(), expectedLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }
}
