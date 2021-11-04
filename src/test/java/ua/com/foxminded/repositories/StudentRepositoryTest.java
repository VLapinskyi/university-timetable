package ua.com.foxminded.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
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
import ua.com.foxminded.domain.Person;
import ua.com.foxminded.domain.Student;
import ua.com.foxminded.repositories.aspects.GeneralRepositoryAspect;
import ua.com.foxminded.repositories.exceptions.RepositoryException;
import ua.com.foxminded.repositories.interfaces.StudentRepository;

@DataJpaTest(showSql = true)
@Import({AopAutoConfiguration.class, GeneralRepositoryAspect.class})
@TestPropertySource("/application-test.properties")
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
class StudentRepositoryTest {
    private final String testData = "/Test data.sql";
    
    private ListAppender<ILoggingEvent> testAppender;
    
    @Autowired
    private GeneralRepositoryAspect generalRepositoryAspect;
    
    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    @SpyBean
    private StudentRepository studentRepository;
    
    private ArrayList<Student> expectedStudents;

    @BeforeEach
    void setUp() throws Exception {
        Logger logger = (Logger) ReflectionTestUtils.getField(generalRepositoryAspect, "logger");
        testAppender = new ListAppender<>();
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        testAppender.setContext(loggerContext);
        testAppender.start();
        logger.addAppender(testAppender);

        expectedStudents = new ArrayList<>(Arrays.asList(new Student(), new Student(), new Student()));
        ArrayList<Integer> studentIndexes = new ArrayList<>(Arrays.asList(1, 2, 3));
        ArrayList<String> firstNames = new ArrayList<>(Arrays.asList("Daria", "Illia", "Mykhailo"));
        ArrayList<String> lastNames = new ArrayList<>(Arrays.asList("Hrynchuk", "Misiats", "Mazur"));
        ArrayList<Gender> gendersForLecturers = new ArrayList<>(Arrays.asList(Gender.FEMALE, Gender.MALE, Gender.MALE));
        ArrayList<String> phoneNumbers = new ArrayList<>(Arrays.asList("+380992222222", "+380323659872", "+380327485963"));
        ArrayList<String> emails = new ArrayList<>(
                Arrays.asList("d.hrynchuk@gmail.com", "illiamisiats@gmail.com", "m.mazur@test.com"));
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
    void tearDown() throws Exception {
        testAppender.stop();
    }

    @Test
    @Sql(testData)
    void shouldCreateStudent() {
        int groupId = 2;
        Group group = testEntityManager.find(Group.class, groupId);
        int maxPeopleId = testEntityManager.getEntityManager().createQuery("from Person", Person.class)
                .getResultStream().max((person1, person2) -> Integer.compare(person1.getId(), person2.getId())).get().getId();
        
        int nextId = maxPeopleId + 1;
        
        Student expectedStudent = new Student();
        expectedStudent.setId(nextId);
        expectedStudent.setFirstName("First-name");
        expectedStudent.setLastName("Last-name");
        expectedStudent.setGender(Gender.MALE);
        expectedStudent.setPhoneNumber("+380741236547");
        expectedStudent.setEmail("test@test.com");
        expectedStudent.setGroup(group);

        Student testStudent = new Student();
        testStudent.setFirstName("First-name");
        testStudent.setLastName("Last-name");
        testStudent.setGender(Gender.MALE);
        testStudent.setPhoneNumber("+380741236547");
        testStudent.setGroup(group);
        testStudent.setEmail("test@test.com");
        
        studentRepository.save(testStudent);

        Student actualStudent = testEntityManager.find(Student.class, nextId);
        assertEquals(expectedStudent, actualStudent);
    }

    @Test
    @Sql(testData)
    void shouldFindAllStudents() {
        ArrayList<Student> actualStudents = (ArrayList<Student>) studentRepository.findAll();
        assertTrue(expectedStudents.containsAll(actualStudents) && actualStudents.containsAll(expectedStudents));
    }

    @Test
    @Sql(testData)
    void shouldFindStudentById() {
        int testId = 3;
        Optional<Student> expectedStudent = expectedStudents.stream().filter(student -> student.getId() == testId).findFirst();
        assertEquals(expectedStudent, studentRepository.findById(testId));
    }

    @Test
    @Sql(testData)
    void shouldUpdateStudent() {
        int testId = 2;
        Student testStudent = testEntityManager.find(Student.class, testId);
        testStudent.setFirstName("Vasyl");
        
        studentRepository.save(testStudent);
        assertEquals(testStudent, testEntityManager.find(Student.class, testId));
    }

    @Test
    @Sql(testData)
    void shouldDeleteById() {
        int deletedStudentId = 1;
        
        studentRepository.deleteById(deletedStudentId);
        Student actualStudent = testEntityManager.find(Student.class, deletedStudentId);
        assertThat(actualStudent).isNull();
    }

    @Test
    void shouldThrowRepositoryExceptionWhenDataAccessExceptionWhileSave() {
        Student student = null;        
        assertThrows(RepositoryException.class, () -> studentRepository.save(student));
    }

    @Test
    void shouldThrowRepositoryExceptionWhenResultIsNullPointerExceptionWhileFindById() {
        int testId = 1;
        assertThrows(RepositoryException.class, () -> studentRepository.findById(testId));
    }

    @Test
    @Sql(testData)
    void shouldThrowRepositoryExceptionWhenDataAccessExceptionWhileFindById() {
        Integer testId = null;
        assertThrows(RepositoryException.class, () -> studentRepository.findById(testId));
    }

    @Test
    void shouldThrowRepositoryExceptionWhenDataAccessExceptionWhileDeleteById() {
        Integer testId = null;
        assertThrows(RepositoryException.class, () -> studentRepository.deleteById(testId));
    }

    @Test
    @Sql(testData)
    void shouldGenerateLogsWhenSaveStudent() {
        int groupId = 2;
        Group group = testEntityManager.find(Group.class, groupId);
        
        Student testStudent = new Student();
        testStudent.setFirstName("Vasyl");
        testStudent.setLastName("Iaremenko");
        testStudent.setGender(Gender.MALE);
        testStudent.setGroup(group);
        testStudent.setEmail("test@gmail.com");
        testStudent.setPhoneNumber("+380968574123");
        
        int maxPeopleId = testEntityManager.getEntityManager().createQuery("from Person", Person.class)
                .getResultStream().max((person1, person2) -> Integer.compare(person1.getId(), person2.getId())).get().getId();
        
        int nextId = maxPeopleId + 1;
        
        Student loggingResultStudent = new Student();
        loggingResultStudent.setFirstName("Vasyl");
        loggingResultStudent.setLastName("Iaremenko");
        loggingResultStudent.setGender(Gender.MALE);
        loggingResultStudent.setGroup(group);
        loggingResultStudent.setEmail("test@gmail.com");
        loggingResultStudent.setPhoneNumber("+380968574123");
        loggingResultStudent.setId(nextId);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to save/update an object: " + testStudent + ".", "The object " + loggingResultStudent + " was saved/updated."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        studentRepository.save(testStudent);

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test   
    void shouldGenerateLogsWhenThrowDataAccessExceptionWhileSaveStudent() {
        Student testStudent = null;

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to save/update an object: " + testStudent + ".", "Can't save/update the object: " + testStudent + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            studentRepository.save(testStudent);
        } catch (RepositoryException repositoryException) {
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
    void shouldGenerateLogsWhenFindAllIsEmpty() {
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.WARN));
        List<String> expectedMessages = new ArrayList<>(
                Arrays.asList("Try to find all objects.", "There are not any objects in the result when findAll."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        studentRepository.findAll();
        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    @Sql(testData)
    void shouldGenerateLogsWhenFindAllHasResult() {
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(
                Arrays.asList("Try to find all objects.", "The result is: " + expectedStudents + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        studentRepository.findAll();

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    @Sql(testData)
    void shouldGenerateLogsWhenFindById() {
        int testId = 2;
        Optional<Student> expectedStudent = expectedStudents.stream().filter(student -> student.getId() == testId).findFirst();

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to find an object by id: " + testId + ".",
                "The result object with id " + testId + " is " + expectedStudent + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        studentRepository.findById(testId);

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenThrowResultIsNullPointerExceptionWhileFindById() {
        int testId = 1;

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to find an object by id: " + testId + ".",
                "There is no result when find an object by id " + testId + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            studentRepository.findById(testId);
        } catch (RepositoryException repositoryException) {
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
    void shouldGenerateLogsWhenThrowDataAccessExceptionWhileFindById() {
        Integer testId = null;

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to find an object by id: " + testId + ".",
                "Can't find an object by id " + testId + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            studentRepository.findById(testId);
        } catch (RepositoryException repositoryException) {
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
    @Sql(testData)
    void shouldGenerateLogsWhenDeleteById() {
        int testId = 2;

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to delete an object by id " + testId + ".",
                "The object with id " + testId + " was deleted."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        studentRepository.deleteById(testId);

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenThrowDataAccessExceptionWhileDeleteById() {
        Integer testId = null;
        
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to delete an object by id " + testId + ".",
                "Can't delete an object by id " + testId + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            studentRepository.deleteById(testId);
        } catch (RepositoryException repositoryException) {
            // do nothing
        }

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }
}