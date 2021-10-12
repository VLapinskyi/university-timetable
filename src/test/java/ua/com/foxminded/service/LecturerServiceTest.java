package ua.com.foxminded.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ua.com.foxminded.dao.LecturerDAO;
import ua.com.foxminded.dao.exceptions.DAOException;
import ua.com.foxminded.domain.Gender;
import ua.com.foxminded.domain.Lecturer;
import ua.com.foxminded.service.exceptions.ServiceException;
import ua.com.foxminded.settings.SpringConfiguration;
import ua.com.foxminded.settings.SpringTestConfiguration;
import ua.com.foxminded.settings.TestAppender;

@ContextConfiguration(classes = { SpringConfiguration.class, SpringTestConfiguration.class})
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
class LecturerServiceTest {
    private TestAppender testAppender = new TestAppender();

    @Autowired
    private LecturerService lecturerService;

    @Mock
    private LecturerDAO lecturerDAO;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(lecturerService, "lecturerDAO", lecturerDAO);
    }

    @AfterEach
    void tearDown() {
        testAppender.cleanEventList();
    }

    @Test
    void shouldCreateLecturer() {
        Lecturer lecturer = new Lecturer();
        lecturer.setFirstName("Valentyn");
        lecturer.setLastName("Lapinskyi");
        lecturer.setGender(Gender.MALE);
        lecturer.setPhoneNumber("+380671234567");
        lecturer.setEmail("valentinlapinskiy@gmail.com");
        lecturerService.create(lecturer);
        verify(lecturerDAO).create(lecturer);
    }

    @Test
    void shouldGetAllLecturers() {
        lecturerService.getAll();
        verify(lecturerDAO).findAll();
    }

    @Test
    void shouldGetLecturerById() {
        int lecturerId = 2;

        lecturerService.getById(lecturerId);
        verify(lecturerDAO).findById(lecturerId);
    }

    @Test
    void shouldUpdateLecturer() {
        Lecturer lecturer = new Lecturer();
        lecturer.setId(3);
        lecturer.setFirstName("Valentyn");
        lecturer.setLastName("Lapinskyi");
        lecturer.setGender(Gender.MALE);
        lecturer.setPhoneNumber("+380671234567");
        lecturer.setEmail("valentinlapiskiy@gmail.com");
        lecturerService.update(lecturer);
        verify(lecturerDAO).update(lecturer);
    }

    @Test
    void shouldDeleteLecturerById() {
        int testLecturerId = 5;
        Lecturer testLecturer = new Lecturer();
        testLecturer.setId(testLecturerId);
        when(lecturerDAO.findById(testLecturerId)).thenReturn(testLecturer);
        lecturerService.deleteById(testLecturerId);
        verify(lecturerDAO).findById(testLecturerId);
        verify(lecturerDAO).delete(testLecturer);
    }

    @Test
    void shouldThrowServiceExceptionWhenLecturerIsNullWhileCreate() {
        Lecturer lecturer = null;
        assertThrows(ServiceException.class, () -> lecturerService.create(lecturer));
    }

    @Test
    void shouldThrowServiceExceptionWhenLecturerIdIsNotZeroWhileCreate() {
        Lecturer lecturer = new Lecturer();
        lecturer.setId(4);
        lecturer.setFirstName("Ivan");
        lecturer.setLastName("Zakharchuk");
        lecturer.setGender(Gender.MALE);
        lecturer.setPhoneNumber("+380963852741");
        lecturer.setEmail("ivan@test.com");

        assertThrows(ServiceException.class, () -> lecturerService.create(lecturer));
    }

    @Test
    void shouldThrowServiceExceptionWhenLecturerFirstNameIsNullWhileCreate() {
        Lecturer lecturer = new Lecturer();
        lecturer.setLastName("Smith");
        lecturer.setGender(Gender.FEMALE);
        lecturer.setPhoneNumber("+380741852963");
        lecturer.setEmail("smith@test.com");

        assertThrows(ServiceException.class, () -> lecturerService.create(lecturer));
    }

    @Test
    void shouldThrowServiceExceptionWhenLecturerFirstNameIsShortWhileCreate() {
        Lecturer lecturer = new Lecturer();
        lecturer.setFirstName("h   ");
        lecturer.setLastName("English");
        lecturer.setGender(Gender.MALE);
        lecturer.setPhoneNumber("+380123456789");
        lecturer.setEmail("English@test.com");

        assertThrows(ServiceException.class, () -> lecturerService.create(lecturer));
    }

    @Test
    void shouldThrowServiceExceptionWhenLecturerFirstNameStartsWithWhiteSpaceWhileCreate() {
        Lecturer lecturer = new Lecturer();
        lecturer.setFirstName(" Test");
        lecturer.setLastName("Jonson");
        lecturer.setGender(Gender.MALE);
        lecturer.setPhoneNumber("+380123456789");
        lecturer.setEmail("jonson@test.com");

        assertThrows(ServiceException.class, () -> lecturerService.create(lecturer));
    }

    @Test
    void shouldThrowServiceExceptionWhenLecturerLastNameIsNullWhileCreate() {
        Lecturer lecturer = new Lecturer();
        lecturer.setFirstName("John");
        lecturer.setGender(Gender.FEMALE);
        lecturer.setPhoneNumber("+380741852963");
        lecturer.setEmail("smith@test.com");

        assertThrows(ServiceException.class, () -> lecturerService.create(lecturer));
    }

    @Test
    void shouldThrowServiceExceptionWhenLecturerLastNameIsShortWhileCreate() {
        Lecturer lecturer = new Lecturer();
        lecturer.setFirstName("Abraham");
        lecturer.setLastName("g   ");
        lecturer.setGender(Gender.MALE);
        lecturer.setPhoneNumber("+380123456789");
        lecturer.setEmail("English@test.com");

        assertThrows(ServiceException.class, () -> lecturerService.create(lecturer));
    }

    @Test
    void shouldThrowServiceExceptionWhenLecturerLastNameStartsWithWhiteSpaceWhileCreate() {
        Lecturer lecturer = new Lecturer();
        lecturer.setFirstName("Ivan");
        lecturer.setLastName(" Jonson");
        lecturer.setGender(Gender.MALE);
        lecturer.setPhoneNumber("+380123456789");
        lecturer.setEmail("jonson@test.com");

        assertThrows(ServiceException.class, () -> lecturerService.create(lecturer));
    }

    @Test
    void shouldThrowServiceExceptionWhenLecturerGenderIsNullWhileCreate() {
        Lecturer lecturer = new Lecturer();
        lecturer.setFirstName("Olha");
        lecturer.setLastName("Skladenko");
        lecturer.setPhoneNumber("+380987654321");
        lecturer.setEmail("olhaskladenko@gmail.com");

        assertThrows(ServiceException.class, () -> lecturerService.create(lecturer));
    }

    @Test
    void shouldThrowServiceExceptionWhenLecturerPhoneNumberIsNullWhileCreate() {
        Lecturer lecturer = new Lecturer();
        lecturer.setFirstName("Natalia");
        lecturer.setLastName("Syrhiienko");
        lecturer.setGender(Gender.FEMALE);
        lecturer.setEmail("NSyrhiienko@gmail.com");

        assertThrows(ServiceException.class, () -> lecturerService.create(lecturer));
    }

    @Test
    void shouldThrowServiceExceptionWhenLecturerPhoneNumberIsInvalidWhileCreate() {
        Lecturer lecturer = new Lecturer();
        lecturer.setFirstName("Natalia");
        lecturer.setLastName("Syrhiienko");
        lecturer.setGender(Gender.FEMALE);
        lecturer.setPhoneNumber("+380475");
        lecturer.setEmail("NSyrhiienko@gmail.com");

        assertThrows(ServiceException.class, () -> lecturerService.create(lecturer));
    }

    @Test
    void shouldThrowServiceExceptionWhenLecturerEmailIsNullWhileCreate() {
        Lecturer lecturer = new Lecturer();
        lecturer.setFirstName("Vadym");
        lecturer.setLastName("Matviichuk");
        lecturer.setGender(Gender.MALE);
        lecturer.setPhoneNumber("+301234567846");

        assertThrows(ServiceException.class, () -> lecturerService.create(lecturer));
    }

    @Test
    void shouldThrowServiceExceptionWhenLecturerEmailIsInvalidWhileCreate() {
        Lecturer lecturer = new Lecturer();
        lecturer.setFirstName("Vadym");
        lecturer.setLastName("Matviichuk");
        lecturer.setGender(Gender.MALE);
        lecturer.setPhoneNumber("+301234567846");
        lecturer.setEmail("matviichuk");

        assertThrows(ServiceException.class, () -> lecturerService.create(lecturer));
    }

    @Test
    void shouldThrowServiceExceptionWhenDAOExceptionWhileCreate() {
        Lecturer lecturer = new Lecturer();
        lecturer.setFirstName("Viacheslav");
        lecturer.setLastName("Iaremenko");
        lecturer.setGender(Gender.MALE);
        lecturer.setPhoneNumber("+380961234567");
        lecturer.setEmail("VIaremenko@gmail.com");

        doThrow(DAOException.class).when(lecturerDAO).create(lecturer);

        assertThrows(ServiceException.class, () -> lecturerService.create(lecturer));
    }

    @Test
    void shouldThrowServiceExceptionWhenDAOExceptionWhileGetAll() {
        when(lecturerDAO.findAll()).thenThrow(DAOException.class);
        assertThrows(ServiceException.class, () -> lecturerService.getAll());
    }

    @Test
    void shouldThrowServiceExceptionWhenLecturerIdIsZeroWhileGetById() {
        int testId = 0;
        assertThrows(ServiceException.class, () -> lecturerService.getById(testId));
    }

    @Test
    void shouldThrowServiceExceptionWhenDAOExceptionWhileGetById() {
        int testId = 3;
        when(lecturerDAO.findById(testId)).thenThrow(DAOException.class);
        assertThrows(ServiceException.class, () -> lecturerService.getById(testId));
    }

    @Test
    void shouldThrowServiceExceptionWhenLecturerIsNullWhileUpdate() {
        Lecturer lecturer = null;
        assertThrows(ServiceException.class, () -> lecturerService.update(lecturer));
    }

    @Test
    void shouldThrowServiceExceptionWhenLecturerIsInvalidWhileUpdate() {
        Lecturer lecturer = new Lecturer();
        lecturer.setId(-40);
        lecturer.setFirstName("Olha");
        lecturer.setLastName("Skladenko");
        lecturer.setGender(Gender.FEMALE);
        lecturer.setPhoneNumber("+380123456789");
        lecturer.setEmail("olhaskladenko@gmail.com");

        assertThrows(ServiceException.class, () -> lecturerService.update(lecturer));
    }

    @Test
    void shouldThrowServiceExceptionWhenDAOExceptionWhileUpdate() {
        Lecturer lecturer = new Lecturer();
        lecturer.setId(1);
        lecturer.setFirstName("Ivan");
        lecturer.setLastName("Zakharchuk");
        lecturer.setGender(Gender.MALE);
        lecturer.setPhoneNumber("+380981234567");
        lecturer.setEmail("izakharchuk@gmail.com");

        doThrow(DAOException.class).when(lecturerDAO).update(lecturer);

        assertThrows(ServiceException.class, () -> lecturerService.update(lecturer));
    }

    @Test
    void shouldThrowServiceExceptionWhenLecturerIdIsZeroWhileDeleteById() {
        int testId = 0;
        assertThrows(ServiceException.class, () -> lecturerService.deleteById(testId));
    }

    @Test
    void shouldThrowServiceExceptioinWhenDAOExceptionWhileDeleteById() {
        int testId = 5;
        doThrow(DAOException.class).when(lecturerDAO).findById(testId);
        assertThrows(ServiceException.class, () -> lecturerService.deleteById(testId));
    }

    @Test
    void shouldGenerateLogsWhenLecturerIsNullWhileCreate() {
        Lecturer lecturer = null;
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to create a new person: " + lecturer + ".",
                "A person " + lecturer + " can't be null when create."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lecturerService.create(lecturer);
        } catch (ServiceException serviceException) {
            // do nothing
        }

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenLecturerIdIsNotZeroWhileCreate() {
        int testId = 4;
        Lecturer lecturer = new Lecturer();
        lecturer.setId(testId);
        lecturer.setFirstName("Oleksandra");
        lecturer.setLastName("Feshchenko");
        lecturer.setGender(Gender.FEMALE);
        lecturer.setPhoneNumber("+380459876543");
        lecturer.setEmail("OFeshenko@gmail.com");

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to create a new person: " + lecturer + ".",
                "A person " + lecturer + " has wrong id " + testId + " which is not equal zero when create."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lecturerService.create(lecturer);
        } catch (ServiceException serviceException) {
            // do nothing
        }

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenLecturerIsInvalidWhileCreate() {
        Lecturer lecturer = new Lecturer();
        lecturer.setFirstName("Natalia");
        lecturer.setLastName("Syrhiienko");
        lecturer.setGender(Gender.FEMALE);
        lecturer.setPhoneNumber("0961275963");
        lecturer.setEmail("test@test.com");

        String violationMessage = "Person phone number should starts from symbol \"+\" and additional twelve numbers";

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to create a new person: " + lecturer + ".",
                "The lecturer " + lecturer + " is not valid when create. There are errors: " + violationMessage + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lecturerService.create(lecturer);
        } catch (ServiceException serviceException) {
            // do nothing
        }

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenDAOExceptionWhileCreate() {
        Lecturer lecturer = new Lecturer();
        lecturer.setFirstName("Natalia");
        lecturer.setLastName("Kohan");
        lecturer.setGender(Gender.FEMALE);
        lecturer.setPhoneNumber("+380759514235");
        lecturer.setEmail("nkohan@test.com");

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to create a new person: " + lecturer + ".",
                "There is some error in dao layer when create an object " + lecturer + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        doThrow(DAOException.class).when(lecturerDAO).create(lecturer);

        try {
            lecturerService.create(lecturer);
        } catch (ServiceException serviceException) {
            // do nothing
        }

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenCreate() {
        Lecturer lecturer = new Lecturer();
        lecturer.setFirstName("Roman");
        lecturer.setLastName("Dudchenko");
        lecturer.setGender(Gender.MALE);
        lecturer.setPhoneNumber("+380426479245");
        lecturer.setEmail("roma@test.com");

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to create a new person: " + lecturer + ".",
                "The object " + lecturer + " was created."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lecturerService.create(lecturer);

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

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

        lecturerService.getAll();

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenDAOExceptionWhileGetAll() {
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(
                Arrays.asList("Try to get all objects.", "There is some error in dao layer when getAll."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        when(lecturerDAO.findAll()).thenThrow(DAOException.class);

        try {
            lecturerService.getAll();
        } catch (ServiceException serviceException) {
            // do nothing
        }

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenGetAll() {
        List<Lecturer> expectedLecturers = new ArrayList<>(
                Arrays.asList(new Lecturer(), new Lecturer(), new Lecturer()));
        List<String> firstNames = new ArrayList<>(Arrays.asList("Alla", "Inna", "Oleksii"));
        List<String> lastNames = new ArrayList<>(Arrays.asList("Matviichuk", "Khimoroda", "Burlachenko"));
        List<Gender> genders = new ArrayList<>(Arrays.asList(Gender.FEMALE, Gender.FEMALE, Gender.MALE));
        List<String> phoneNumbers = new ArrayList<>(Arrays.asList("+380159635741", "+380657964256", "+380436942675"));
        List<String> emails = new ArrayList<>(
                Arrays.asList("matviichuk@gmail.com", "khimoroda@gmail.com", "burlachenko@gmail.com"));

        for (int i = 0; i < expectedLecturers.size(); i++) {
            int index = i + 1;
            expectedLecturers.get(i).setId(index);
            expectedLecturers.get(i).setFirstName(firstNames.get(i));
            expectedLecturers.get(i).setLastName(lastNames.get(i));
            expectedLecturers.get(i).setGender(genders.get(i));
            expectedLecturers.get(i).setPhoneNumber(phoneNumbers.get(i));
            expectedLecturers.get(i).setEmail(emails.get(i));
        }

        when(lecturerDAO.findAll()).thenReturn(expectedLecturers);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(
                Arrays.asList("Try to get all objects.", "The result is: " + expectedLecturers + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lecturerService.getAll();

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenLecturerIdIsNegativeWhileGetById() {
        int negativeId = -6;

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to get an object by id: " + negativeId + ".",
                "A given id " + negativeId + " is less than 1 when getById."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lecturerService.getById(negativeId);
        } catch (ServiceException serviceException) {
            // do nothing
        }

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenEntityIsNotFoundInDatabaseWhileGetById() {
        int testId = 3;

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to get an object by id: " + testId + ".",
                "The entity is not found when get object by id " + testId + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        DAOException daoException = new DAOException("The result is empty", new NullPointerException());
        when(lecturerDAO.findById(testId)).thenThrow(daoException);

        try {
            lecturerService.getById(testId);
        } catch (ServiceException serviceException) {
            // do nothing
        }

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenDAOExceptionWhileGetById() {
        int testId = 6;

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to get an object by id: " + testId + ".",
                "There is some error in dao layer when get object by id " + testId + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        when(lecturerDAO.findById(testId)).thenThrow(DAOException.class);

        try {
            lecturerService.getById(testId);
        } catch (ServiceException serviceException) {
            // do nothing
        }

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenGetById() {
        int testId = 1;

        Lecturer lecturer = new Lecturer();
        lecturer.setId(testId);
        lecturer.setFirstName("Iryna");
        lecturer.setLastName("Kasian");
        lecturer.setGender(Gender.FEMALE);
        lecturer.setPhoneNumber("+380453982147");
        lecturer.setEmail("irak@test.com");

        when(lecturerDAO.findById(testId)).thenReturn(lecturer);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to get an object by id: " + testId + ".",
                "The result object with id " + testId + " is " + lecturer + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lecturerService.getById(testId);

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenLecturerIsNullWhileUpdate() {
        Lecturer lecturer = null;

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to update a person: " + lecturer + ".",
                "An updated person " + lecturer + " is null."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lecturerService.update(lecturer);
        } catch (ServiceException serviceException) {
            // do nothing
        }

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenLecturerIdIsZeroWhileUpdate() {
        Lecturer lecturer = new Lecturer();
        lecturer.setFirstName("Natalia");
        lecturer.setLastName("Gudym");
        lecturer.setGender(Gender.FEMALE);
        lecturer.setPhoneNumber("+380657832145");
        lecturer.setEmail("gudym@test.com");

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to update a person: " + lecturer + ".",
                "An updated person " + lecturer + " has wrong id " + lecturer.getId() + " which is not positive."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lecturerService.update(lecturer);
        } catch (ServiceException serviceException) {
            // do nothing
        }

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenLecturerIsInvalidWhileUpdate() {
        Lecturer lecturer = new Lecturer();
        lecturer.setId(2);
        lecturer.setFirstName("Liliia");
        lecturer.setLastName(" Leonenko");
        lecturer.setGender(Gender.FEMALE);
        lecturer.setEmail("leonenko@test.com");
        lecturer.setPhoneNumber("+380126945321");

        String violationMessage = "Person lastname must have at least two symbols and start with non-white space";

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to update a person: " + lecturer + ".",
                "The lecturer " + lecturer + " is not valid when update. There are errors: " + violationMessage + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lecturerService.update(lecturer);
        } catch (ServiceException serviceException) {
            // do nothing
        }

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenDAOExceptionWhileUpdate() {
        Lecturer lecturer = new Lecturer();
        lecturer.setId(4);
        lecturer.setFirstName("Nataliia");
        lecturer.setLastName("Khodorkovska");
        lecturer.setGender(Gender.FEMALE);
        lecturer.setPhoneNumber("+380459621567");
        lecturer.setEmail("khodorkovska@test.com");

        doThrow(DAOException.class).when(lecturerDAO).update(lecturer);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to update a person: " + lecturer + ".",
                "There is some error in dao layer when update an object " + lecturer + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lecturerService.update(lecturer);
        } catch (ServiceException serviceException) {
            // do nothing
        }

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenUpdate() {
        Lecturer lecturer = new Lecturer();
        lecturer.setId(7);
        lecturer.setFirstName("Viktoriia");
        lecturer.setLastName("Vovk");
        lecturer.setGender(Gender.FEMALE);
        lecturer.setPhoneNumber("+380695478321");
        lecturer.setEmail("vovk@test.com");

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(
                Arrays.asList("Try to update a person: " + lecturer + ".", "The object " + lecturer + " was updated."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lecturerService.update(lecturer);

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenGroupIdIsNegativeWhileDeleteById() {
        int testId = -1;

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to delete an object by id: " + testId + ".",
                "A given id " + testId + " is less than 1 when deleteById."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lecturerService.deleteById(testId);
        } catch (ServiceException serviceException) {
            // do nothing
        }

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenDAOExceptionWhileDeleteById() {
        int testId = 10;

        doThrow(DAOException.class).when(lecturerDAO).findById(testId);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to delete an object by id: " + testId + ".",
                "There is some error in dao layer when delete an object by id " + testId + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lecturerService.deleteById(testId);
        } catch (ServiceException serviceException) {
            // do nothing
        }

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenDeleteById() {
        int testId = 64;

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to delete an object by id: " + testId + ".",
                "An object was deleted by id " + testId + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lecturerService.deleteById(testId);

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }
}