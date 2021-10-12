package ua.com.foxminded.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

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
import ua.com.foxminded.dao.StudentDAO;
import ua.com.foxminded.dao.exceptions.DAOException;
import ua.com.foxminded.domain.Faculty;
import ua.com.foxminded.domain.Gender;
import ua.com.foxminded.domain.Group;
import ua.com.foxminded.domain.Student;
import ua.com.foxminded.service.exceptions.ServiceException;
import ua.com.foxminded.settings.SpringConfiguration;
import ua.com.foxminded.settings.SpringTestConfiguration;
import ua.com.foxminded.settings.TestAppender;

@ContextConfiguration(classes = { SpringConfiguration.class, SpringTestConfiguration.class})
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
class StudentServiceTest {
    private TestAppender testAppender = new TestAppender();

    @Autowired
    private StudentService studentService;

    @Mock
    private StudentDAO studentDAO;

    private Group group1;
    private Group group2;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(studentService, "studentDAO", studentDAO);
        Faculty faculty = new Faculty();
        faculty.setId(1);
        faculty.setName("Faculty");
        group1 = new Group();
        group1.setId(1);
        group1.setName("Group1");
        group1.setFaculty(faculty);
        group2 = new Group();
        group2.setId(2);
        group2.setName("Group2");
        group2.setFaculty(faculty);

    }

    @AfterEach
    void tearDown() {
        testAppender.cleanEventList();
    }

    @Test
    void shouldCreateStudent() {
        Student creatingStudent = new Student();
        creatingStudent.setFirstName("Valentyn");
        creatingStudent.setLastName("Lapisnkyi");
        creatingStudent.setGender(Gender.MALE);
        creatingStudent.setPhoneNumber("+380453698751");
        creatingStudent.setEmail("lapinskyi@test.com");
        creatingStudent.setGroup(group2);

        studentService.create(creatingStudent);
        verify(studentDAO).create(creatingStudent);
    }

    @Test
    void shouldGetAllStudents() {
        studentService.getAll();
        verify(studentDAO).findAll();
    }

    @Test
    void shouldGetStudentById() {
        int studentId = 1;
        studentService.getById(studentId);
        verify(studentDAO).findById(studentId);
    }

    @Test
    void shouldUpdateStudent() {
        Student student = new Student();
        student.setId(3);
        student.setFirstName("Mykyta");
        student.setLastName("Kozhumiaka");
        student.setGender(Gender.MALE);
        student.setPhoneNumber("+380455247985");
        student.setEmail("kozhumiaka@test.com");
        student.setGroup(group2);
        studentService.update(student);
        verify(studentDAO).update(student);
    }

    @Test
    void shouldDeleteStudentById() {
        int testId = 1;
        Student student = new Student();
        student.setId(testId);
        
        when(studentDAO.findById(testId)).thenReturn(student);
        studentService.deleteById(testId);
        verify(studentDAO).findById(testId);
        verify(studentDAO).delete(student);
    }

    @Test
    void shouldThrowServiceExceptionWhenStudentIsNullWhileCreate() {
        Student student = null;
        assertThrows(ServiceException.class, () -> studentService.create(student));
    }

    @Test
    void shouldThrowServiceExceptionWhenStudentIdIsNotZeroWhileCreate() {
        Student student = new Student();
        student.setId(1);
        student.setFirstName("Ivan");
        student.setLastName("Zakharchuk");
        student.setGender(Gender.MALE);
        student.setPhoneNumber("+380963852741");
        student.setEmail("ivan@test.com");
        student.setGroup(group1);

        assertThrows(ServiceException.class, () -> studentService.create(student));
    }

    @Test
    void shouldThrowServiceExceptionWhenStudentFirstNameIsNullWhileCreate() {
        Student student = new Student();
        student.setLastName("Smith");
        student.setGender(Gender.FEMALE);
        student.setPhoneNumber("+380741852963");
        student.setEmail("smith@test.com");
        student.setGroup(group2);

        assertThrows(ServiceException.class, () -> studentService.create(student));
    }

    @Test
    void shouldThrowServiceExceptionWhenStudentFirstNameIsShortWhileCreate() {
        Student student = new Student();
        student.setFirstName("r   ");
        student.setLastName("Bond");
        student.setGender(Gender.MALE);
        student.setPhoneNumber("+380659754321");
        student.setEmail("bond@test.com");
        student.setGroup(group1);

        assertThrows(ServiceException.class, () -> studentService.create(student));
    }

    @Test
    void shouldThrowServiceExceptionWhenStudentFirstNameStartsWithWhiteSpaceWhileCreate() {
        Student student = new Student();
        student.setFirstName(" John");
        student.setLastName("Jonson");
        student.setGender(Gender.MALE);
        student.setPhoneNumber("+380123456749");
        student.setEmail("jonson@test.com");
        student.setGroup(group2);

        assertThrows(ServiceException.class, () -> studentService.create(student));
    }

    @Test
    void shouldThrowServiceExceptionWhenStudentLastNameIsNullWhileCreate() {
        Student student = new Student();
        student.setFirstName("John");
        student.setGender(Gender.FEMALE);
        student.setPhoneNumber("+380741852963");
        student.setEmail("smith@test.com");
        student.setGroup(group1);

        assertThrows(ServiceException.class, () -> studentService.create(student));
    }

    @Test
    void shouldThrowServiceExceptionWhenStudentLastNameIsShortWhileCreate() {
        Student student = new Student();
        student.setFirstName("Abraham");
        student.setLastName("g   ");
        student.setGender(Gender.MALE);
        student.setPhoneNumber("+380786352741");
        student.setEmail("English@test.com");
        student.setGroup(group1);

        assertThrows(ServiceException.class, () -> studentService.create(student));
    }

    @Test
    void shouldThrowServiceExceptionWhenStudentLastNameStartsWithWhiteSpaceWhileCreate() {
        Student student = new Student();
        student.setFirstName("Ivan");
        student.setLastName(" Petruk");
        student.setGender(Gender.MALE);
        student.setPhoneNumber("+380459624785");
        student.setEmail("petruk@test.com");
        student.setGroup(group1);

        assertThrows(ServiceException.class, () -> studentService.create(student));
    }

    @Test
    void shouldThrowServiceExceptionWhenStudentGenderIsNullWhileCreate() {
        Student student = new Student();
        student.setFirstName("Olha");
        student.setLastName("Skladenko");
        student.setPhoneNumber("+380987654321");
        student.setEmail("olhaskladenko@gmail.com");
        student.setGroup(group1);

        assertThrows(ServiceException.class, () -> studentService.create(student));
    }

    @Test
    void shouldThrowServiceExceptionWhenStudentPhoneNumberIsNullWhileCreate() {
        Student student = new Student();
        student.setFirstName("Natalia");
        student.setLastName("Syrhiienko");
        student.setGender(Gender.FEMALE);
        student.setEmail("NSyrhiienko@gmail.com");
        student.setGroup(group2);

        assertThrows(ServiceException.class, () -> studentService.create(student));
    }

    @Test
    void shouldThrowServiceExceptionWhenStudentPhoneNumberIsInvalidWhileCreate() {
        Student student = new Student();
        student.setFirstName("Natalia");
        student.setLastName("Syrhiienko");
        student.setGender(Gender.FEMALE);
        student.setPhoneNumber("+380475");
        student.setEmail("NSyrhiienko@gmail.com");
        student.setGroup(group1);

        assertThrows(ServiceException.class, () -> studentService.create(student));
    }

    @Test
    void shouldThrowServiceExceptionWhenStudentEmailIsNullWhileCreate() {
        Student student = new Student();
        student.setFirstName("Vadym");
        student.setLastName("Matviichuk");
        student.setGender(Gender.MALE);
        student.setPhoneNumber("+301234567846");
        student.setGroup(group2);

        assertThrows(ServiceException.class, () -> studentService.create(student));
    }

    @Test
    void shouldThrowServiceExceptionWhenStudentEmailIsInvalidWhileCreate() {
        Student student = new Student();
        student.setFirstName("Vadym");
        student.setLastName("Matviichuk");
        student.setGender(Gender.MALE);
        student.setPhoneNumber("+301234567846");
        student.setEmail("matviichuk");
        student.setGroup(group2);

        assertThrows(ServiceException.class, () -> studentService.create(student));
    }

    @Test
    void shouldThrowServiceExceptionWhenStudentGroupIsNullWhileCreate() {
        Student student = new Student();
        student.setFirstName("Vadym");
        student.setLastName("Matviichuk");
        student.setGender(Gender.MALE);
        student.setPhoneNumber("+301234567846");
        student.setEmail("matviichuk@test.com");

        assertThrows(ServiceException.class, () -> studentService.create(student));
    }

    @Test
    void shouldThrowServiceExceptionWhenStudentGroupIsInvalidWhileCreate() {
        Student student = new Student();
        student.setFirstName("Vadym");
        student.setLastName("Matviichuk");
        student.setGender(Gender.MALE);
        student.setPhoneNumber("+301234567846");
        student.setEmail("matviichuk@test.com");
        group2.setName(" Wrong name");
        student.setGroup(group2);

        assertThrows(ServiceException.class, () -> studentService.create(student));
    }

    @Test
    void shouldThrowServiceExceptionWhenDAOExceptionWhileCreate() {
        Student student = new Student();
        student.setFirstName("Viacheslav");
        student.setLastName("Iaremenko");
        student.setGender(Gender.MALE);
        student.setPhoneNumber("+380961234567");
        student.setEmail("VIaremenko@gmail.com");
        student.setGroup(group1);

        doThrow(DAOException.class).when(studentDAO).create(student);

        assertThrows(ServiceException.class, () -> studentService.create(student));
    }

    @Test
    void shouldThrowServiceExceptionWhenDAOExceptionWhileGetAll() {
        when(studentDAO.findAll()).thenThrow(DAOException.class);
        assertThrows(ServiceException.class, () -> studentService.getAll());
    }

    @Test
    void shouldThrowServiceExceptionWhenStudentIdIsZeroWhileGetById() {
        int testId = 0;
        assertThrows(ServiceException.class, () -> studentService.getById(testId));
    }

    @Test
    void shouldThrowServiceExceptionWhenDAOExceptionWhileGetById() {
        int testId = 3;
        when(studentDAO.findById(testId)).thenThrow(DAOException.class);
        assertThrows(ServiceException.class, () -> studentService.getById(testId));
    }

    @Test
    void shouldThrowServiceExceptionWhenStudentIsNullWhileUpdate() {
        Student student = null;
        assertThrows(ServiceException.class, () -> studentService.update(student));
    }

    @Test
    void shouldThrowServiceExceptionWhenStudentIsInvalidWhileUpdate() {
        Student student = new Student();
        student.setId(-10);
        student.setFirstName("Olha");
        student.setLastName("Skladenko");
        student.setGender(Gender.FEMALE);
        student.setPhoneNumber("+380123456789");
        student.setEmail("olhaskladenko@gmail.com");
        student.setGroup(group1);

        assertThrows(ServiceException.class, () -> studentService.update(student));
    }

    @Test
    void shouldThrowServiceExceptionWhenDAOExceptionWhileUpdate() {
        Student student = new Student();
        student.setId(1);
        student.setFirstName("Ivan");
        student.setLastName("Zakharchuk");
        student.setGender(Gender.MALE);
        student.setPhoneNumber("+380981234567");
        student.setEmail("izakharchuk@gmail.com");
        student.setGroup(group1);

        doThrow(DAOException.class).when(studentDAO).update(student);

        assertThrows(ServiceException.class, () -> studentService.update(student));
    }

    @Test
    void shouldThrowServiceExceptionWhenStudentIdIsZeroWhileDeleteById() {
        int testId = 0;
        assertThrows(ServiceException.class, () -> studentService.deleteById(testId));
    }

    @Test
    void shouldThrowServiceExceptioinWhenDAOExceptionWhileDeleteById() {
        int testId = 5;
        doThrow(DAOException.class).when(studentDAO).findById(testId);
        assertThrows(ServiceException.class, () -> studentService.deleteById(testId));
    }

    @Test
    void shouldGenerateLogsWhenStudentIsNullWhileCreate() {
        Student student = null;
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to create a new person: " + student + ".",
                "A person " + student + " can't be null when create."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            studentService.create(student);
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
    void shouldGenerateLogsWhenStudentIdIsNotZeroWhileCreate() {
        int testId = 4;
        Student student = new Student();
        student.setId(testId);
        student.setFirstName("Oleksandr");
        student.setLastName("Bezsinnyi");
        student.setGender(Gender.MALE);
        student.setPhoneNumber("+380324569871");
        student.setEmail("OBezsinnyio@gmail.com");
        student.setGroup(group1);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to create a new person: " + student + ".",
                "A person " + student + " has wrong id " + testId + " which is not equal zero when create."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            studentService.create(student);
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
    void shouldGenerateLogsWhenStudentIsInvalidWhileCreate() {
        Student student = new Student();
        student.setFirstName("Natalia");
        student.setLastName("Kohan");
        student.setGender(Gender.FEMALE);
        student.setPhoneNumber("0961275963");
        student.setEmail("test@test.com");
        student.setGroup(group2);

        String violationMessage = "Person phone number should starts from symbol \"+\" and additional twelve numbers";

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to create a new person: " + student + ".",
                "The student " + student + " is not valid when create. There are errors: " + violationMessage + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            studentService.create(student);
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
        Student student = new Student();
        student.setFirstName("Natalia");
        student.setLastName("Fedoriaka");
        student.setGender(Gender.FEMALE);
        student.setPhoneNumber("+380569742356");
        student.setEmail("fedoriaka@test.com");
        student.setGroup(group2);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to create a new person: " + student + ".",
                "There is some error in dao layer when create an object " + student + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        doThrow(DAOException.class).when(studentDAO).create(student);

        try {
            studentService.create(student);
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
        Student student = new Student();
        student.setFirstName("Roman");
        student.setLastName("Kolomiiets");
        student.setGender(Gender.MALE);
        student.setPhoneNumber("+380645876321");
        student.setEmail("kolomiiets@test.com");
        student.setGroup(group2);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to create a new person: " + student + ".",
                "The object " + student + " was created."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        studentService.create(student);

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

        studentService.getAll();

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

        when(studentDAO.findAll()).thenThrow(DAOException.class);

        try {
            studentService.getAll();
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
        List<Student> expectedStudents = new ArrayList<>(Arrays.asList(new Student(), new Student(), new Student()));
        List<String> firstNames = new ArrayList<>(Arrays.asList("Olena", "Alina", "Olha"));
        List<String> lastNames = new ArrayList<>(Arrays.asList("Perebyinis", "Melnyk", "Televna"));
        List<Gender> genders = new ArrayList<>(Arrays.asList(Gender.FEMALE, Gender.FEMALE, Gender.FEMALE));
        List<String> phoneNumbers = new ArrayList<>(Arrays.asList("+380359874521", "+380421365879", "+380159765432"));
        List<String> emails = new ArrayList<>(
                Arrays.asList("perebyinis@gmail.com", "melnyk@gmail.com", "televna@gmail.com"));
        List<Group> groups = new ArrayList<>(Arrays.asList(group1, group2, group1));

        for (int i = 0; i < expectedStudents.size(); i++) {
            int index = i + 1;
            expectedStudents.get(i).setId(index);
            expectedStudents.get(i).setFirstName(firstNames.get(i));
            expectedStudents.get(i).setLastName(lastNames.get(i));
            expectedStudents.get(i).setGender(genders.get(i));
            expectedStudents.get(i).setPhoneNumber(phoneNumbers.get(i));
            expectedStudents.get(i).setEmail(emails.get(i));
            expectedStudents.get(i).setGroup(groups.get(i));
        }

        when(studentDAO.findAll()).thenReturn(expectedStudents);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(
                Arrays.asList("Try to get all objects.", "The result is: " + expectedStudents + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        studentService.getAll();

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenStudentIdIsNegativeWhileGetById() {
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
            studentService.getById(negativeId);
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
        int testId = 4;

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to get an object by id: " + testId + ".",
                "The entity is not found when get object by id " + testId + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        DAOException daoException = new DAOException("The result is empty", new NullPointerException());
        when(studentDAO.findById(testId)).thenThrow(daoException);

        try {
            studentService.getById(testId);
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

        when(studentDAO.findById(testId)).thenThrow(DAOException.class);

        try {
            studentService.getById(testId);
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

        Student student = new Student();
        student.setId(testId);
        student.setFirstName("Iryna");
        student.setLastName("Krasnoshtan");
        student.setGender(Gender.FEMALE);
        student.setPhoneNumber("+380569543578");
        student.setEmail("krasnoshtan@test.com");
        student.setGroup(group1);

        when(studentDAO.findById(testId)).thenReturn(student);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to get an object by id: " + testId + ".",
                "The result object with id " + testId + " is " + student + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        studentService.getById(testId);

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenStudentIdIsZeroWhileUpdate() {
        Student student = new Student();
        student.setFirstName("Natalia");
        student.setLastName("Shvets");
        student.setGender(Gender.FEMALE);
        student.setPhoneNumber("+380659841235");
        student.setEmail("shvets@test.com");
        student.setGroup(group2);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to update a person: " + student + ".",
                "An updated person " + student + " has wrong id " + student.getId() + " which is not positive."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            studentService.update(student);
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
    void shouldGenerateLogsWhenStudentIsInvalidWhileUpdate() {
        Student student = new Student();
        student.setId(2);
        student.setFirstName("Liliia");
        student.setLastName(" Mohyla");
        student.setGender(Gender.FEMALE);
        student.setEmail("mohyla@test.com");
        student.setPhoneNumber("+380568742354");
        student.setGroup(group1);

        String violationMessage = "Person lastname must have at least two symbols and start with non-white space";

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to update a person: " + student + ".",
                "The student " + student + " is not valid when update. There are errors: " + violationMessage + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            studentService.update(student);
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
        Student student = new Student();
        student.setId(4);
        student.setFirstName("Solomiia");
        student.setLastName("Vatsiak");
        student.setGender(Gender.FEMALE);
        student.setPhoneNumber("+380657412365");
        student.setEmail("vatsiak@test.com");
        student.setGroup(group2);

        doThrow(DAOException.class).when(studentDAO).update(student);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to update a person: " + student + ".",
                "There is some error in dao layer when update an object " + student + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            studentService.update(student);
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
        Student student = new Student();
        student.setId(7);
        student.setFirstName("Viktoriia");
        student.setLastName("Sontse");
        student.setGender(Gender.FEMALE);
        student.setPhoneNumber("+380456987523");
        student.setEmail("sontse@test.com");
        student.setGroup(group1);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(
                Arrays.asList("Try to update a person: " + student + ".", "The object " + student + " was updated."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        studentService.update(student);

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
            studentService.deleteById(testId);
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

        doThrow(DAOException.class).when(studentDAO).findById(testId);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to delete an object by id: " + testId + ".",
                "There is some error in dao layer when delete an object by id " + testId + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            studentService.deleteById(testId);
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
        int testId = 2;

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to delete an object by id: " + testId + ".",
                "An object was deleted by id " + testId + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        studentService.deleteById(testId);

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }
}