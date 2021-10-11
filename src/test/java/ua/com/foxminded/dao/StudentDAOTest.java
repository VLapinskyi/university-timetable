package ua.com.foxminded.dao;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.anyString;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.PersistenceException;

import org.hibernate.SessionFactory;
import org.hibernate.internal.SessionImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ua.com.foxminded.dao.exceptions.DAOException;
import ua.com.foxminded.domain.Faculty;
import ua.com.foxminded.domain.Gender;
import ua.com.foxminded.domain.Group;
import ua.com.foxminded.domain.Student;
import ua.com.foxminded.settings.SpringDAOTestConfiguration;
import ua.com.foxminded.settings.TestAppender;

@ContextConfiguration(classes = { SpringDAOTestConfiguration.class })
@ExtendWith(SpringExtension.class)
class StudentDAOTest {
    private final ClassPathResource testData = new ClassPathResource("/Test data.sql");
    private final ClassPathResource testTablesCreator = new ClassPathResource("/Creating tables.sql");
    private final ClassPathResource testDatabaseCleaner = new ClassPathResource("/Clearing database.sql");

    private TestAppender testAppender = new TestAppender();
    
    @Autowired
    private StudentDAO studentDAO;
    
    @Autowired
    SessionFactory sessionFactory;
    
    private ArrayList<Student> expectedStudents;
    private Connection connection;
    
    @Mock
    private SessionFactory mockedSessionFactory;

    @BeforeEach
    @Transactional
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        connection = ((SessionImpl)sessionFactory.getCurrentSession()).connection();
        ScriptUtils.executeSqlScript(connection, testTablesCreator);

        expectedStudents = new ArrayList<>(Arrays.asList(new Student(), new Student(), new Student()));
        ArrayList<Integer> studentIndexes = new ArrayList<>(Arrays.asList(4, 5, 6));
        ArrayList<String> firstNames = new ArrayList<>(Arrays.asList("Daria", "Illia", "Mykhailo"));
        ArrayList<String> lastNames = new ArrayList<>(Arrays.asList("Hrynchuk", "Misiats", "Mazur"));
        ArrayList<Gender> gendersForLecturers = new ArrayList<>(Arrays.asList(Gender.FEMALE, Gender.MALE, Gender.MALE));
        ArrayList<String> phoneNumbers = new ArrayList<>(Arrays.asList("+380992222222", null, null));
        ArrayList<String> emails = new ArrayList<>(
                Arrays.asList("d.hrynchuk@gmail.com", "illiamisiats@gmail.com", null));
        for (int i = 0; i < expectedStudents.size(); i++) {
            expectedStudents.get(i).setId(studentIndexes.get(i));
            expectedStudents.get(i).setFirstName(firstNames.get(i));
            expectedStudents.get(i).setLastName(lastNames.get(i));
            expectedStudents.get(i).setGender(gendersForLecturers.get(i));
            expectedStudents.get(i).setPhoneNumber(phoneNumbers.get(i));
            expectedStudents.get(i).setEmail(emails.get(i));
        }
        Faculty faculty1 = new Faculty();
        faculty1.setId(1);
        faculty1.setName("TestFaculty1");
        
        Faculty faculty2 = new Faculty();
        faculty2.setId(2);
        faculty2.setName("TestFaculty2");
        
        Group group1 = new Group();
        group1.setId(1);
        group1.setName("TestGroup1");
        group1.setFaculty(faculty1);
        
        Group group2 = new Group();
        group2.setId(2);
        group2.setName("TestGroup2");
        group2.setFaculty(faculty2);
        
        expectedStudents.get(0).setGroup(group1);
        expectedStudents.get(1).setGroup(group1);
        expectedStudents.get(2).setGroup(group2);
    }

    @AfterEach
    @Transactional
    void tearDown() throws Exception {
        testAppender.cleanEventList();
        ReflectionTestUtils.setField(studentDAO, "sessionFactory", sessionFactory);
        ScriptUtils.executeSqlScript(connection, testDatabaseCleaner);
    }

    @Test
    @Transactional
    void shouldCreateStudent() {
        Student expectedStudent = new Student();
        expectedStudent.setId(1);
        expectedStudent.setFirstName("First-name");
        expectedStudent.setLastName("Last-name");
        expectedStudent.setGender(Gender.MALE);
        expectedStudent.setPhoneNumber("1233");

        Student testStudent = new Student();
        testStudent.setFirstName("First-name");
        testStudent.setLastName("Last-name");
        testStudent.setGender(Gender.MALE);
        testStudent.setPhoneNumber("1233");
        studentDAO.create(testStudent);

        Student actualStudent = studentDAO.findAll().stream().findFirst().get();
        assertEquals(expectedStudent, actualStudent);
    }

    @Test
    @Transactional
    void shouldFindAllStudents() {
        ScriptUtils.executeSqlScript(connection, testData);
        ArrayList<Student> actualStudents = (ArrayList<Student>) studentDAO.findAll();
        assertTrue(expectedStudents.containsAll(actualStudents) && actualStudents.containsAll(expectedStudents));
    }

    @Test
    @Transactional
    void shouldFindStudentById() {
        ScriptUtils.executeSqlScript(connection, testData);
        int testId = 5;
        Student expectedStudent = expectedStudents.stream().filter(student -> student.getId() == testId).findFirst().get();
        assertEquals(expectedStudent, studentDAO.findById(testId));
    }

    @Test
    @Transactional
    void shouldUpdateStudent() {
        ScriptUtils.executeSqlScript(connection, testData);
        int testId = 6;
        Student testStudent = studentDAO.findById(testId);
        testStudent.setFirstName("Vasyl");
        
        studentDAO.update(testStudent);
        assertEquals(testStudent, studentDAO.findById(testId));
    }

    @Test
    @Transactional
    void shouldDeleteStudent() {
        ScriptUtils.executeSqlScript(connection, testData);
        int deletedStudentId = 5;
        Student deletedStudent = studentDAO.findById(deletedStudentId);
        for (int i = 0; i < expectedStudents.size(); i++) {
            if (expectedStudents.get(i).getId() == deletedStudentId) {
                Student studentFromList = expectedStudents.get(i);
                deletedStudent.setId(studentFromList.getId());
                deletedStudent.setFirstName(studentFromList.getFirstName());
                deletedStudent.setLastName(studentFromList.getLastName());
                deletedStudent.setGender(studentFromList.getGender());
                deletedStudent.setPhoneNumber(studentFromList.getPhoneNumber());
                deletedStudent.setEmail(studentFromList.getEmail());
                deletedStudent.setGroup(studentFromList.getGroup());
                
                expectedStudents.remove(i);
                i--;
            }
        }
        studentDAO.delete(deletedStudent);
        ArrayList<Student> actualStudents = (ArrayList<Student>) studentDAO.findAll();
        assertTrue(expectedStudents.containsAll(actualStudents) && actualStudents.containsAll(expectedStudents));
    }

    @Test
    @Transactional
    void shouldThrowDAOExceptionWhenPersistenceExceptionWhileCreate() {
        Student student = new Student();
        student.setId(1);
        student.setFirstName("Olha");
        student.setLastName("Skladenko");
        student.setPhoneNumber("+3803241569");
        student.setEmail("oskladenko@test.com");
        student.setGender(Gender.FEMALE);
        student.setGroup(expectedStudents.get(0).getGroup());
        assertThrows(DAOException.class, () -> studentDAO.create(student));
    }

    @Test
    @Transactional
    void shouldThrowDAOExceptionWhenPersistanceExceptionWhileFindAll() {
        ReflectionTestUtils.setField(studentDAO, "sessionFactory", mockedSessionFactory);
        when(mockedSessionFactory.getCurrentSession()).thenThrow(PersistenceException.class);
        assertThrows(DAOException.class, () -> studentDAO.findAll());
    }

    @Test
    @Transactional
    void shouldThrowDAOExceptionWhenResultIsNullPointerExceptionWhileFindById() {
        int testId = 1;
        assertThrows(DAOException.class, () -> studentDAO.findById(testId));
    }

    @Test
    @Transactional
    void shouldThrowDAOExceptionWhenPersistenceExceptionWhileFindById() {
        int testId = 1;
        ReflectionTestUtils.setField(studentDAO, "sessionFactory", mockedSessionFactory);
        when(mockedSessionFactory.getCurrentSession()).thenThrow(PersistenceException.class);
        assertThrows(DAOException.class, () -> studentDAO.findById(testId));
    }

    @Test
    @Transactional
    void shouldThrowDAOExceptionWhenPersistenceExceptionWhileUpdate() {
        Student testStudent = new Student();
        testStudent.setId(1);
        testStudent.setFirstName("Ivan");
        testStudent.setLastName("Ivanov");
        testStudent.setGender(Gender.FEMALE);
        testStudent.setPhoneNumber("+3801236547");
        testStudent.setEmail("iivanov@test.com");
        ReflectionTestUtils.setField(studentDAO, "sessionFactory", mockedSessionFactory);
        when(mockedSessionFactory.getCurrentSession()).thenThrow(PersistenceException.class);
        assertThrows(DAOException.class, () -> studentDAO.update(testStudent));
    }

    @Test
    @Transactional
    void shouldThrowDAOExceptionWhenPersistenceExceptionWhileDelete() {
        Student student = new Student();
        student.setId(1);
        student.setFirstName("Vasyl");
        student.setLastName("Vasyliev");
        student.setGender(Gender.MALE);
        student.setPhoneNumber("+380547896321");
        student.setEmail("vvasyliev@test.com");
        ReflectionTestUtils.setField(studentDAO, "sessionFactory", mockedSessionFactory);
        when(mockedSessionFactory.getCurrentSession()).thenThrow(PersistenceException.class);
        assertThrows(DAOException.class, () -> studentDAO.delete(student));
    }

    @Test
    @Transactional
    void shouldGenerateLogsWhenCreateStudent() {
        Student testStudent = new Student();
        testStudent.setFirstName("Vasyl");
        testStudent.setLastName("Iaremenko");
        testStudent.setGender(Gender.MALE);
        
        Student loggingResultStudent = new Student();
        loggingResultStudent.setFirstName("Vasyl");
        loggingResultStudent.setLastName("Iaremenko");
        loggingResultStudent.setGender(Gender.MALE);
        loggingResultStudent.setId(1);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to insert a new object: " + loggingResultStudent + ".", "The object " + loggingResultStudent + " was inserted."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        studentDAO.create(testStudent);

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    @Transactional
    void shouldGenerateLogsWhenThrowPersistenceExceptionWhileCreateStudent() {
        Student testStudent = new Student();
        testStudent.setGender(Gender.MALE);
        testStudent.setId(1);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to insert a new object: " + testStudent + ".", "Can't insert the object: " + testStudent + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            studentDAO.create(testStudent);
        } catch (DAOException daoException) {
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
    @Transactional
    void shouldGenerateLogsWhenFindAllIsEmpty() {
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.WARN));
        List<String> expectedMessages = new ArrayList<>(
                Arrays.asList("Try to find all objects.", "There are not any objects in the result when findAll."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        studentDAO.findAll();
        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    @Transactional
    void shouldGenerateLogsWhenFindAllHasResult() {
        ScriptUtils.executeSqlScript(connection, testData);
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(
                Arrays.asList("Try to find all objects.", "The result is: " + expectedStudents + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        studentDAO.findAll();

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    @Transactional
    void shouldGenerateLogsWhenThrowPersistenceExceptionWhileFindAll() {
        ReflectionTestUtils.setField(studentDAO, "sessionFactory", mockedSessionFactory);
        when(mockedSessionFactory.getCurrentSession()).thenThrow(PersistenceException.class);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(
                Arrays.asList("Try to find all objects.", "Can't find all objects."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            studentDAO.findAll();
            verify(mockedSessionFactory.getCurrentSession()).createQuery(anyString(), Student.class);
        } catch (DAOException daoException) {
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
    @Transactional
    void shouldGenerateLogsWhenFindById() {
        ScriptUtils.executeSqlScript(connection, testData);
        int testId = 5;
        Student expectedStudent = expectedStudents.stream().filter(student -> student.getId() == testId).findFirst()
                .get();

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to find an object by id: " + testId + ".",
                "The result object with id " + testId + " is " + expectedStudent + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        studentDAO.findById(testId);

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    @Transactional
    void shouldGenerateLogsWhenThrowResultIsNullPointerExceptionWhileFindById() {
        int testId = 5;

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to find an object by id: " + testId + ".",
                "There is no result when find an object by id " + testId + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            studentDAO.findById(testId);
        } catch (DAOException daoException) {
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
    @Transactional
    void shouldGenerateLogsWhenThrowPersistenceExceptionWhileFindById() {
        int testId = 5;

        ReflectionTestUtils.setField(studentDAO, "sessionFactory", mockedSessionFactory);
        when(mockedSessionFactory.getCurrentSession()).thenThrow(PersistenceException.class);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to find an object by id: " + testId + ".",
                "Can't find an object by id " + testId + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            studentDAO.findById(testId);
            verify(mockedSessionFactory.getCurrentSession()).get(Student.class, testId);
        } catch (DAOException daoException) {
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
    @Transactional
    void shouldGenerateLogsWhenUpdate() {
        ScriptUtils.executeSqlScript(connection, testData);
        Student testStudent = new Student();
        testStudent.setId(5);
        testStudent.setFirstName("Test");
        testStudent.setLastName("Test");
        testStudent.setGender(Gender.FEMALE);
        testStudent.setEmail("test@test.com");

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(
                Arrays.asList("Try to update an object " + testStudent + ".",
                        "The object " + testStudent + " was updated."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        studentDAO.update(testStudent);

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    @Transactional
    void shouldGenerateLogsWhenThrowPersistenceExceptionWhileUpdate() {
        Student testStudent = new Student();
        testStudent.setId(6);
        testStudent.setFirstName("Iryna");
        testStudent.setLastName("Hevel");
        testStudent.setGender(Gender.FEMALE);
        testStudent.setEmail("test2@test.com");

        ReflectionTestUtils.setField(studentDAO, "sessionFactory", mockedSessionFactory);
        when(mockedSessionFactory.getCurrentSession()).thenThrow(PersistenceException.class);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(
                Arrays.asList("Try to update an object " + testStudent + ".",
                        "Can't update an object " + testStudent + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            studentDAO.update(testStudent);
            verify(mockedSessionFactory.getCurrentSession()).update(testStudent);
        } catch (DAOException daoException) {
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
    @Transactional
    void shouldGenerateLogsWhenDelete() {
        ScriptUtils.executeSqlScript(connection, testData);
        int testId = 6;
        Student deletedStudent = expectedStudents.stream().filter(student -> student.getId() == testId).findFirst().get();

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to delete an object " + deletedStudent + ".",
                "The object " + deletedStudent + " was deleted."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        studentDAO.delete(deletedStudent);

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    @Transactional
    void shouldGenerateLogsWhenThrowPersistenceExceptionWhileDelete() {
        int testId = 6;
        Student deletedStudent = expectedStudents.stream().filter(student -> student.getId() == testId).findFirst().get();

        ReflectionTestUtils.setField(studentDAO, "sessionFactory", mockedSessionFactory);
        when(mockedSessionFactory.getCurrentSession()).thenThrow(PersistenceException.class);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to delete an object " + deletedStudent + ".",
                "Can't delete an object " + deletedStudent + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            studentDAO.delete(deletedStudent);
            verify(mockedSessionFactory.getCurrentSession()).update(deletedStudent);
        } catch (DAOException daoException) {
            // do nothing
        }

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }
}