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
import ua.com.foxminded.domain.Gender;
import ua.com.foxminded.domain.Group;
import ua.com.foxminded.domain.Student;
import ua.com.foxminded.repositories.exceptions.RepositoryException;
import ua.com.foxminded.repositories.interfaces.StudentRepository;
import ua.com.foxminded.service.aspects.GeneralServiceAspect;
import ua.com.foxminded.service.aspects.PersonAspect;
import ua.com.foxminded.service.aspects.StudentAspect;
import ua.com.foxminded.service.exceptions.ServiceException;
import ua.com.foxminded.settings.SpringTestConfiguration;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = SpringTestConfiguration.class)
class StudentServiceTest {
    private ListAppender<ILoggingEvent> testAppender;

    @Autowired
    private GeneralServiceAspect generalServiceAspect;

    @Autowired
    private StudentAspect studentAspect;

    @Autowired
    private PersonAspect personAspect;

    @Autowired
    private StudentService studentService;

    @MockBean
    private StudentRepository studentRepository;

    private Group group1;
    private Group group2;

    @BeforeEach
    void init() {
        ReflectionTestUtils.setField(studentService, "studentRepository", studentRepository);

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger generalLogger = (Logger) ReflectionTestUtils.getField(generalServiceAspect, "logger");
        Logger studentLogger = (Logger) ReflectionTestUtils.getField(studentAspect, "logger");
        Logger personLogger = (Logger) ReflectionTestUtils.getField(personAspect, "logger");

        testAppender = new ListAppender<>();
        testAppender.setContext(loggerContext);
        testAppender.start();

        generalLogger.addAppender(testAppender);
        studentLogger.addAppender(testAppender);
        personLogger.addAppender(testAppender);

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
        testAppender.stop();
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
        verify(studentRepository).save(creatingStudent);
    }

    @Test
    void shouldGetAllStudents() {
        studentService.getAll();
        verify(studentRepository).findAll();
    }

    @Test
    void shouldGetStudentById() {
        int studentId = 1;

        Faculty faculty = new Faculty();
        faculty.setId(studentId);
        faculty.setName("Faculty");

        Group group = new Group();
        group.setId(1);
        group.setName("Group");
        group.setFaculty(faculty);

        Student student = new Student();
        student.setId(studentId);
        student.setFirstName("Student");
        student.setLastName("Student");
        student.setGender(Gender.MALE);
        student.setEmail("test@test.com");
        student.setPhoneNumber("+380111111111");
        student.setGroup(group);

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        studentService.getById(studentId);
        verify(studentRepository).findById(studentId);
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
        verify(studentRepository).save(student);
    }

    @Test
    void shouldDeleteStudentById() {
        int testId = 1;
        studentService.deleteById(testId);
        verify(studentRepository).deleteById(testId);
    }

    @Test
    void shouldThrowServiceExceptionWhenStudentIsNullWhileCreate() {
        Student student = null;
        RuntimeException exception = assertThrows(ServiceException.class, () -> studentService.create(student));
        
        String message = "A given person isn't legal when create.";
        assertEquals(message, exception.getMessage());
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

        RuntimeException exception = assertThrows(ServiceException.class, () -> studentService.create(student));
    
        String message = "A given person isn't legal when create.";
        assertEquals(message, exception.getMessage());
    }

    @Test
    void shouldThrowServiceExceptionWhenStudentFirstNameIsNullWhileCreate() {
        Student student = new Student();
        student.setLastName("Smith");
        student.setGender(Gender.FEMALE);
        student.setPhoneNumber("+380741852963");
        student.setEmail("smith@test.com");
        student.setGroup(group2);

        RuntimeException exception = assertThrows(ServiceException.class, () -> studentService.create(student));
    
        String message = "A given student isn't valid when create.";
        assertEquals(message, exception.getMessage());
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

        RuntimeException exception = assertThrows(ServiceException.class, () -> studentService.create(student));
    
        String message = "A given student isn't valid when create.";
        assertEquals(message, exception.getMessage());
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

        RuntimeException exception = assertThrows(ServiceException.class, () -> studentService.create(student));
    
        String message = "A given student isn't valid when create.";
        assertEquals(message, exception.getMessage());
    }

    @Test
    void shouldThrowServiceExceptionWhenStudentLastNameIsNullWhileCreate() {
        Student student = new Student();
        student.setFirstName("John");
        student.setGender(Gender.FEMALE);
        student.setPhoneNumber("+380741852963");
        student.setEmail("smith@test.com");
        student.setGroup(group1);

        RuntimeException exception = assertThrows(ServiceException.class, () -> studentService.create(student));
    
        String message = "A given student isn't valid when create.";
        assertEquals(message, exception.getMessage());
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

        RuntimeException exception = assertThrows(ServiceException.class, () -> studentService.create(student));
    
        String message = "A given student isn't valid when create.";
        assertEquals(message, exception.getMessage());
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

        RuntimeException exception = assertThrows(ServiceException.class, () -> studentService.create(student));
    
        String message = "A given student isn't valid when create.";
        assertEquals(message, exception.getMessage());
    }

    @Test
    void shouldThrowServiceExceptionWhenStudentGenderIsNullWhileCreate() {
        Student student = new Student();
        student.setFirstName("Olha");
        student.setLastName("Skladenko");
        student.setPhoneNumber("+380987654321");
        student.setEmail("olhaskladenko@gmail.com");
        student.setGroup(group1);

        RuntimeException exception = assertThrows(ServiceException.class, () -> studentService.create(student));
    
        String message = "A given student isn't valid when create.";
        assertEquals(message, exception.getMessage());
    }

    @Test
    void shouldThrowServiceExceptionWhenStudentPhoneNumberIsNullWhileCreate() {
        Student student = new Student();
        student.setFirstName("Natalia");
        student.setLastName("Syrhiienko");
        student.setGender(Gender.FEMALE);
        student.setEmail("NSyrhiienko@gmail.com");
        student.setGroup(group2);

        RuntimeException exception = assertThrows(ServiceException.class, () -> studentService.create(student));
    
        String message = "A given student isn't valid when create.";
        assertEquals(message, exception.getMessage());
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

        RuntimeException exception = assertThrows(ServiceException.class, () -> studentService.create(student));
    
        String message = "A given student isn't valid when create.";
        assertEquals(message, exception.getMessage());
    }

    @Test
    void shouldThrowServiceExceptionWhenStudentEmailIsNullWhileCreate() {
        Student student = new Student();
        student.setFirstName("Vadym");
        student.setLastName("Matviichuk");
        student.setGender(Gender.MALE);
        student.setPhoneNumber("+301234567846");
        student.setGroup(group2);

        RuntimeException exception = assertThrows(ServiceException.class, () -> studentService.create(student));
    
        String message = "A given student isn't valid when create.";
        assertEquals(message, exception.getMessage());
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

        RuntimeException exception = assertThrows(ServiceException.class, () -> studentService.create(student));
    
        String message = "A given student isn't valid when create.";
        assertEquals(message, exception.getMessage());
    }

    @Test
    void shouldThrowServiceExceptionWhenStudentGroupIsNullWhileCreate() {
        Student student = new Student();
        student.setFirstName("Vadym");
        student.setLastName("Matviichuk");
        student.setGender(Gender.MALE);
        student.setPhoneNumber("+301234567846");
        student.setEmail("matviichuk@test.com");

        RuntimeException exception = assertThrows(ServiceException.class, () -> studentService.create(student));
    
        String message = "A given student is wrong.";
        assertEquals(message, exception.getMessage());
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

        RuntimeException exception = assertThrows(ServiceException.class, () -> studentService.create(student));
    
        String message = "A given student isn't valid when create.";
        assertEquals(message, exception.getMessage());
    }

    @Test
    void shouldThrowServiceExceptionWhenRepositoryExceptionWhileCreate() {
        Student student = new Student();
        student.setFirstName("Viacheslav");
        student.setLastName("Iaremenko");
        student.setGender(Gender.MALE);
        student.setPhoneNumber("+380961234567");
        student.setEmail("VIaremenko@gmail.com");
        student.setGroup(group1);

        doThrow(RepositoryException.class).when(studentRepository).save(student);

        RuntimeException exception = assertThrows(ServiceException.class, () -> studentService.create(student));
    
        String message = "There is some error in repositories layer when create object.";
        assertEquals(message, exception.getMessage());
    }

    @Test
    void shouldThrowServiceExceptionWhenRepositoryExceptionWhileGetAll() {
        when(studentRepository.findAll()).thenThrow(RepositoryException.class);
        RuntimeException exception = assertThrows(ServiceException.class, () -> studentService.getAll());
    
        String message = "There is some error in repositories layer when getAll.";
        assertEquals(message, exception.getMessage());
    }

    @Test
    void shouldThrowServiceExceptionWhenStudentIdIsZeroWhileGetById() {
        int testId = 0;
        RuntimeException exception = assertThrows(ServiceException.class, () -> studentService.getById(testId));
    
        String message = "A given id is incorrect when getById.";
        assertEquals(message, exception.getMessage());
    }

    @Test
    void shouldThrowServiceExceptionWhenRepositoryExceptionWhileGetById() {
        int testId = 3;
        when(studentRepository.findById(testId)).thenThrow(RepositoryException.class);
        RuntimeException exception = assertThrows(ServiceException.class, () -> studentService.getById(testId));
    
        String message = "There is some error in repositories layer when get object by id.";
        assertEquals(message, exception.getMessage());
    }

    @Test
    void shouldThrowServiceExceptionWhenStudentIsNullWhileUpdate() {
        Student student = null;
        RuntimeException exception = assertThrows(ServiceException.class, () -> studentService.update(student));
    
        String message = "A given person isn't legal when update.";
        assertEquals(message, exception.getMessage());
    }

    @Test
    void shouldThrowServiceExceptionWhenStudentIsInvalidWhileUpdate() {
        Student student = new Student();
        student.setId(23);
        student.setFirstName(" ");
        student.setLastName("Skladenko");
        student.setGender(Gender.FEMALE);
        student.setPhoneNumber("+380123456789");
        student.setEmail("olhaskladenko@gmail.com");
        student.setGroup(group1);

        RuntimeException exception = assertThrows(ServiceException.class, () -> studentService.update(student));
    
        String message = "A given student isn't valid when update.";
        assertEquals(message, exception.getMessage());
    }

    @Test
    void shouldThrowServiceExceptionWhenRepositoryExceptionWhileUpdate() {
        Student student = new Student();
        student.setId(1);
        student.setFirstName("Ivan");
        student.setLastName("Zakharchuk");
        student.setGender(Gender.MALE);
        student.setPhoneNumber("+380981234567");
        student.setEmail("izakharchuk@gmail.com");
        student.setGroup(group1);

        doThrow(RepositoryException.class).when(studentRepository).save(student);

        RuntimeException exception = assertThrows(ServiceException.class, () -> studentService.update(student));
    
        String message = "Can't update an object because of repositoryException.";
        assertEquals(message, exception.getMessage());
    }

    @Test
    void shouldThrowServiceExceptionWhenStudentIdIsZeroWhileDeleteById() {
        int testId = 0;
        RuntimeException exception = assertThrows(ServiceException.class, () -> studentService.deleteById(testId));
    
        String message = "A given id is less than 1 when deleteById.";
        assertEquals(message, exception.getMessage());
    }

    @Test
    void shouldThrowServiceExceptioinWhenRepositoryExceptionWhileDeleteById() {
        int testId = 5;
        doThrow(RepositoryException.class).when(studentRepository).deleteById(testId);
        RuntimeException exception = assertThrows(ServiceException.class, () -> studentService.deleteById(testId));
    
        String message = "There is some error in repositories layer when delete an object by id.";
        assertEquals(message, exception.getMessage());
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

        List<ILoggingEvent> actualLogs = testAppender.list;

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

        List<ILoggingEvent> actualLogs = testAppender.list;

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

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenRepositoryExceptionWhileCreate() {
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
                "There is some error in repositories layer when create an object " + student + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        doThrow(RepositoryException.class).when(studentRepository).save(student);

        try {
            studentService.create(student);
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

        studentService.getAll();

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

        when(studentRepository.findAll()).thenThrow(RepositoryException.class);

        try {
            studentService.getAll();
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

        when(studentRepository.findAll()).thenReturn(expectedStudents);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(
                Arrays.asList("Try to get all objects.", "The result is: " + expectedStudents + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        studentService.getAll();

        List<ILoggingEvent> actualLogs = testAppender.list;

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

        List<ILoggingEvent> actualLogs = testAppender.list;

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

        RepositoryException repositoryException = new RepositoryException("The result is empty", new NullPointerException());
        when(studentRepository.findById(testId)).thenThrow(repositoryException);

        try {
            studentService.getById(testId);
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
        int testId = 6;

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to get an object by id: " + testId + ".",
                "There is some error in repositories layer when get object by id " + testId + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        when(studentRepository.findById(testId)).thenThrow(RepositoryException.class);

        try {
            studentService.getById(testId);
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
        int testId = 1;

        Student student = new Student();
        student.setId(testId);
        student.setFirstName("Iryna");
        student.setLastName("Krasnoshtan");
        student.setGender(Gender.FEMALE);
        student.setPhoneNumber("+380569543578");
        student.setEmail("krasnoshtan@test.com");
        student.setGroup(group1);

        when(studentRepository.findById(testId)).thenReturn(Optional.of(student));

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to get an object by id: " + testId + ".",
                "The result object with id " + testId + " is " + student + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        studentService.getById(testId);

        List<ILoggingEvent> actualLogs = testAppender.list;

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

        List<ILoggingEvent> actualLogs = testAppender.list;

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

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenRepositoryExceptionWhileUpdate() {
        Student student = new Student();
        student.setId(4);
        student.setFirstName("Solomiia");
        student.setLastName("Vatsiak");
        student.setGender(Gender.FEMALE);
        student.setPhoneNumber("+380657412365");
        student.setEmail("vatsiak@test.com");
        student.setGroup(group2);

        doThrow(RepositoryException.class).when(studentRepository).save(student);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to update a person: " + student + ".",
                "There is some error in repositories layer when update an object " + student + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            studentService.update(student);
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

        List<ILoggingEvent> actualLogs = testAppender.list;

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

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenRepositoryExceptionWhileDeleteById() {
        int testId = 10;

        doThrow(RepositoryException.class).when(studentRepository).deleteById(testId);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to delete an object by id: " + testId + ".",
                "There is some error in repositories layer when delete an object by id " + testId + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            studentService.deleteById(testId);
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

        studentService.deleteById(testId);

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }
}