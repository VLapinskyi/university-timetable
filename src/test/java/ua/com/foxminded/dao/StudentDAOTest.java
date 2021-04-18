package ua.com.foxminded.dao;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;

import java.sql.Connection;
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
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ua.com.foxminded.dao.exceptions.DAOException;
import ua.com.foxminded.domain.Gender;
import ua.com.foxminded.domain.Group;
import ua.com.foxminded.domain.Student;
import ua.com.foxminded.mapper.GroupMapper;
import ua.com.foxminded.mapper.StudentMapper;
import ua.com.foxminded.settings.SpringTestConfiguration;
import ua.com.foxminded.settings.TestAppender;
@ContextConfiguration(classes = {SpringTestConfiguration.class})
@ExtendWith(SpringExtension.class)
class StudentDAOTest {
    private final ClassPathResource testData = new ClassPathResource("/Test data.sql");
    private final ClassPathResource testTablesCreator = new ClassPathResource("/Creating tables.sql");
    private final ClassPathResource testDatabaseCleaner = new ClassPathResource("/Clearing database.sql");

    private TestAppender testAppender = new TestAppender();
    @Autowired
    private StudentDAO studentDAO;
    @Autowired
    private JdbcTemplate jdbcTemplate;    
    private ArrayList<Student> expectedStudents;
    private Connection connection;
    @Mock
    private JdbcTemplate mockedJdbcTemplate;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        connection = jdbcTemplate.getDataSource().getConnection();
        ScriptUtils.executeSqlScript(connection, testTablesCreator);

        expectedStudents = new ArrayList<>(Arrays.asList(
                new Student(), new Student(), new Student()));
        ArrayList<Integer> studentIndexes = new ArrayList<>(Arrays.asList(
                4, 5, 6));
        ArrayList<String> firstNames = new ArrayList<>(Arrays.asList(
                "Daria", "Illia", "Mykhailo"));
        ArrayList<String> lastNames = new ArrayList<>(Arrays.asList(
                "Hrynchuk", "Misiats", "Mazur"));
        ArrayList<Gender> gendersForLecturers = new ArrayList<>(Arrays.asList(
                Gender.FEMALE, Gender.MALE, Gender.MALE));
        ArrayList<String> phoneNumbers = new ArrayList<>(Arrays.asList(
                "+380992222222", null, null));
        ArrayList<String> emails = new ArrayList<>(Arrays.asList(
                "d.hrynchuk@gmail.com", "illiamisiats@gmail.com", null));
        for (int i = 0; i < expectedStudents.size(); i++) {
            expectedStudents.get(i).setId(studentIndexes.get(i));
            expectedStudents.get(i).setFirstName(firstNames.get(i));
            expectedStudents.get(i).setLastName(lastNames.get(i));
            expectedStudents.get(i).setGender(gendersForLecturers.get(i));
            expectedStudents.get(i).setPhoneNumber(phoneNumbers.get(i));
            expectedStudents.get(i).setEmail(emails.get(i));
        }        
    }

    @AfterEach
    void tearDown() throws Exception {
        testAppender.cleanEventList();
        ReflectionTestUtils.setField(studentDAO, "jdbcTemplate", jdbcTemplate);
        ScriptUtils.executeSqlScript(connection, testDatabaseCleaner);
    }

    @Test
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
    void shouldFindAllStudents() {
        ScriptUtils.executeSqlScript(connection, testData);
        ArrayList<Student> actualStudents = (ArrayList<Student>) studentDAO.findAll();
        assertTrue(expectedStudents.containsAll(actualStudents) && actualStudents.containsAll(expectedStudents));
    }

    @Test
    void shouldFindStudentById() {
        ScriptUtils.executeSqlScript(connection, testData);
        int checkedId = 5;
        Student expectedStudent = new Student();
        expectedStudent.setId(checkedId);
        expectedStudent.setFirstName("Illia");
        expectedStudent.setLastName("Misiats");
        expectedStudent.setGender(Gender.MALE);
        expectedStudent.setEmail("illiamisiats@gmail.com");
        assertEquals(expectedStudent, studentDAO.findById(checkedId));
    }

    @Test
    void shouldUpdateStudent() {
        ScriptUtils.executeSqlScript(connection, testData);
        int testId = 6;
        Student testStudent = new Student();
        testStudent.setFirstName("Tetiana");
        testStudent.setLastName("Lytvynenko");
        testStudent.setGender(Gender.FEMALE);

        Student expectedStudent = new Student();
        expectedStudent.setId(testId);
        expectedStudent.setFirstName("Tetiana");
        expectedStudent.setLastName("Lytvynenko");
        expectedStudent.setGender(Gender.FEMALE);  
        studentDAO.update(testId, testStudent);
        assertEquals(expectedStudent, studentDAO.findById(testId));
    }

    @Test
    void shouldDeleteStudentById() {
        ScriptUtils.executeSqlScript(connection, testData);
        int deletedId = 5;
        for (int i = 0; i < expectedStudents.size(); i++) {
            if (expectedStudents.get(i).getId() == deletedId) {
                expectedStudents.remove(i);
                i--;
            }
        }
        studentDAO.deleteById(deletedId);
        ArrayList<Student> actualStudents = (ArrayList<Student>) studentDAO.findAll();
        assertTrue(expectedStudents.containsAll(actualStudents) && actualStudents.containsAll(expectedStudents));
    }

    @Test
    void shouldSetStudentGroup() {
        ScriptUtils.executeSqlScript(connection, testData);
        int groupId = 2;
        Group group = new Group();
        group.setId(groupId);
        group.setName("TestGroup2");

        int studentId = 5;
        Student expectedStudent = expectedStudents.stream().filter(student -> student.getId() == studentId).findFirst().get();
        expectedStudent.setGroup(group);

        studentDAO.setStudentGroup(groupId, studentId);
        Student actualStudent = studentDAO.findById(studentId);
        actualStudent.setGroup(studentDAO.getStudentGroup(studentId));
        assertEquals(expectedStudent, actualStudent);
    }

    @Test
    void shouldGetStudentGroup() {
        ScriptUtils.executeSqlScript(connection, testData);
        int groupId = 3;
        Group expectedGroup = new Group();
        expectedGroup.setId(groupId);
        expectedGroup.setName("TestGroup3");

        int studentId = 6;
        studentDAO.setStudentGroup(groupId, studentId);
        Group actualGroup = studentDAO.getStudentGroup(studentId);
        assertEquals(expectedGroup, actualGroup);
    }
    
    @Test
    void shouldThrowDAOExceptionWhenDataAccessExceptionWhileCreate() {
        Student student = new Student();
        student.setGender(Gender.MALE);
        assertThrows(DAOException.class, () -> studentDAO.create(student));
    }
    
    @Test
    void shouldThrowDAOExceptionWhenDataAccessExceptionWhileFindAll() {
        ReflectionTestUtils.setField(studentDAO, "jdbcTemplate", mockedJdbcTemplate);
        when(mockedJdbcTemplate.query(anyString(), any(StudentMapper.class), anyString())).thenThrow(QueryTimeoutException.class);
        assertThrows(DAOException.class, () -> studentDAO.findAll());
    }
    
    @Test
    void shouldThrowDAOExceptionWhenEmptyResultDataAccessExceptionWhileFindById() {
        int testId = 1;
        assertThrows(DAOException.class, () -> studentDAO.findById(testId));
    }
    
    @Test
    void shouldThrowDAOExceptionWhenDataAccessExceptionWhileFindById() {
        int testId = 1;
        ReflectionTestUtils.setField(studentDAO, "jdbcTemplate", mockedJdbcTemplate);
        when(mockedJdbcTemplate.queryForObject(anyString(), any(StudentMapper.class), anyInt(), anyString())).thenThrow(QueryTimeoutException.class);
        assertThrows(DAOException.class, () -> studentDAO.findById(testId));
    }
    
    @Test
    void shouldThrowDAOExceptionWhenDataAccessExceptionWhileUpdate() {
        int testId = 1;
        Student testStudent= new Student();
        testStudent.setGender(Gender.FEMALE);
        ReflectionTestUtils.setField(studentDAO, "jdbcTemplate", mockedJdbcTemplate);
        doThrow(QueryTimeoutException.class).when(mockedJdbcTemplate).update(anyString(), (Object) any());
        assertThrows(DAOException.class, () -> studentDAO.update(testId, testStudent));
    }
    
    @Test
    void shouldThrowDAOExceptionWhenDataAccessExceptionWhileDeleteById() {
        int testId = 1;
        ReflectionTestUtils.setField(studentDAO, "jdbcTemplate", mockedJdbcTemplate);
        doThrow(QueryTimeoutException.class).when(mockedJdbcTemplate).update(anyString(), anyInt(), anyString());
        assertThrows(DAOException.class, () -> studentDAO.deleteById(testId));
    }
    
    @Test
    void shouldThrowDAOExceptionWhenDataAccessExeceptionWhileSetStudentGroup() {
        int groupId = 1;
        int studentId = 2;
        ReflectionTestUtils.setField(studentDAO, "jdbcTemplate", mockedJdbcTemplate);
        doThrow(QueryTimeoutException.class).when(mockedJdbcTemplate).update(anyString(), anyInt(), anyInt());
        assertThrows(DAOException.class, () -> studentDAO.setStudentGroup(groupId, studentId));
    }
    
    @Test
    void shouldThrowDAOExceptionWhenDataAccessExceptionWhileGetStudentGroup() {
        int studentId = 1;
        ReflectionTestUtils.setField(studentDAO, "jdbcTemplate", mockedJdbcTemplate);
        when(mockedJdbcTemplate.queryForObject(anyString(), any(GroupMapper.class), anyInt())).thenThrow(QueryTimeoutException.class);
        assertThrows(DAOException.class, () -> studentDAO.getStudentGroup(studentId));
    }
    
    @Test
    void shouldDAOExceptionWhenEmptyResultDataAccessExceptionWhileGetStudentGroup() {
        int studentId = 1;
        assertThrows(DAOException.class, () -> studentDAO.getStudentGroup(studentId));
    }

    @Test
    void shouldGenerateLogsWhenCreateStudent() {
        Student testStudent = new Student();
        testStudent.setFirstName("Vasyl");
        testStudent.setLastName("Iaremenko");
        testStudent.setGender(Gender.MALE);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to insert a new object: " + testStudent + ".",
                "The object " + testStudent + " was inserted."));
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
    void shouldGenerateLogsWhenThrowDataAccessExceptionWhileCreateStudent() {
        Student testStudent = new Student();
        testStudent.setGender(Gender.MALE);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to insert a new object: " + testStudent + ".",
                "Can't insert the object: " + testStudent + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            studentDAO.create(testStudent);
        } catch (DAOException daoException) {
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
    void shouldGenerateLogsWhenFindAllIsEmpty() {
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.WARN));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to find all objects.",
                "There are not any objects in the result when findAll."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        studentDAO.findAll();
        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i ++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }        
    }

    @Test
    void shouldGenerateLogsWhenFindAllHasResult() {
        ScriptUtils.executeSqlScript(connection, testData);
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to find all objects.",
                "The result is: " + expectedStudents + "."));
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
    void shouldGenerateLogsWhenThrowDataAccessExceptionWhileFindAll() {
        ReflectionTestUtils.setField(studentDAO, "jdbcTemplate", mockedJdbcTemplate);
        when(mockedJdbcTemplate.query(anyString(), any(StudentMapper.class), anyString())).thenThrow(QueryTimeoutException.class);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to find all objects.",
                "Can't find all objects."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            studentDAO.findAll();
            verify(mockedJdbcTemplate).query(anyString(), any(StudentMapper.class), anyString());
        } catch (DAOException daoException) {
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
    void shouldGenerateLogsWhenFindById() {
        ScriptUtils.executeSqlScript(connection, testData);
        int testId = 5;
        Student expectedStudent = expectedStudents.stream().filter(student -> student.getId() == testId).findFirst().get();

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to find an object by id: " + testId + ".",
                "The result object with id " + testId + " is " + expectedStudent + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        studentDAO.findById(testId);

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for(int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenThrowEmptyResultDataAccessExceptionWhileFindById() {
        int testId = 5;

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to find an object by id: " + testId + ".",
                "There is no result when find an object by id " + testId + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            studentDAO.findById(testId);
        } catch (DAOException daoException) {
            //do nothing
        }

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for(int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenThrowDataAccessExceptionWhileFindById() {
        int testId = 5;

        ReflectionTestUtils.setField(studentDAO, "jdbcTemplate", mockedJdbcTemplate);
        when(mockedJdbcTemplate.queryForObject(anyString(), any(StudentMapper.class), any())).thenThrow(QueryTimeoutException.class);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to find an object by id: " + testId + ".",
                "Can't find an object by id " + testId + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            studentDAO.findById(testId);
            verify(mockedJdbcTemplate).queryForObject(anyString(), any(StudentMapper.class), any());
        } catch (DAOException daoException) {
            //do nothing
        }

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for(int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenUpdate() {
        ScriptUtils.executeSqlScript(connection, testData);
        int testId = 4;
        Student testStudent = new Student();
        testStudent.setFirstName("Nataliia");
        testStudent.setLastName("Kohan");
        testStudent.setGender(Gender.FEMALE);
        testStudent.setEmail("test@test.com");

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to update an object " + testStudent + " with id " + testId + ".",
                "The object " + testStudent + " with id " + testId + " was updated."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        studentDAO.update(testId, testStudent);

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenThrowDataAccessExceptionWhileUpdate() {
        int testId = 4;
        Student testStudent = new Student();
        testStudent.setFirstName("Iryna");
        testStudent.setLastName("Kasian");
        testStudent.setGender(Gender.FEMALE);
        testStudent.setEmail("test2@test.com");

        ReflectionTestUtils.setField(studentDAO, "jdbcTemplate", mockedJdbcTemplate);
        doThrow(QueryTimeoutException.class).when(mockedJdbcTemplate).update(anyString(), (Object) any());

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to update an object " + testStudent + " with id " + testId + ".",
                "Can't update an object " + testStudent + " with id " + testId + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            studentDAO.update(testId, testStudent);
            verify(mockedJdbcTemplate).update(anyString(), (Object) any());
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
    void shouldGenerateLogsWhenDeleteById() {
        ScriptUtils.executeSqlScript(connection, testData);
        int testId = 6;

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to delete an object by id " + testId + ".",
                "The object was deleted by id " + testId + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        studentDAO.deleteById(testId);

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenThrowDataAccessExceptionWhileDeleteById() {
        int testId = 6;

        ReflectionTestUtils.setField(studentDAO, "jdbcTemplate", mockedJdbcTemplate);
        doThrow(QueryTimeoutException.class).when(mockedJdbcTemplate).update(anyString(), (Object) any());

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to delete an object by id " + testId + ".",
                "Can't delete an object by id " + testId + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            studentDAO.deleteById(testId);
            verify(mockedJdbcTemplate).update(anyString(), (Object) any());
        } catch (DAOException daoException) {
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
    void shouldGenerateLogsWhenSetStudentGroup() {
        ScriptUtils.executeSqlScript(connection, testData);
        int groupId = 2;
        int studentId = 5;

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to set a group with id " + groupId + " for a student with id " + studentId + ".",
                "The group with id " + groupId + " was setted for the student with id " + studentId + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        studentDAO.setStudentGroup(groupId, studentId);

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for(int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenThrowDataAccessExceptionWhileSetStudentGroup() {
        int groupId = 2;
        int studentId = 5;

        ReflectionTestUtils.setField(studentDAO, "jdbcTemplate", mockedJdbcTemplate);
        doThrow(QueryTimeoutException.class).when(mockedJdbcTemplate).update(anyString(), anyInt(), anyInt());

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to set a group with id " + groupId + " for a student with id " + studentId + ".",
                "Can't set a group with id " + groupId + " for a student with id " + studentId + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            studentDAO.setStudentGroup(groupId, studentId);
            verify(mockedJdbcTemplate).update(anyString(), anyInt(), anyInt());
        } catch (DAOException daoException) {
            //do nothing
        }

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for(int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenGetStudentGroup() {
        ScriptUtils.executeSqlScript(connection, testData);
        int studentId = 6;
        Group expectedGroup = new Group();
        expectedGroup.setId(2);
        expectedGroup.setName("TestGroup2");

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to get a group for a student with id " + studentId + ".",
                "The result group for the student with id " + studentId + " is " + expectedGroup + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        studentDAO.getStudentGroup(studentId);

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenThrowEmptyResultDataAccessExceptionGetStudentGroup() {
        int studentId = 6;

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to get a group for a student with id " + studentId + ".",
                "There is no result when find a group for a student by student id " + studentId + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            studentDAO.getStudentGroup(studentId);
        } catch (DAOException daoException) {
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
    void shouldGenerateLogsWhenThrowDataAccessExceptionGetStudentGroup() {
        int studentId = 6;

        ReflectionTestUtils.setField(studentDAO, "jdbcTemplate", mockedJdbcTemplate);
        when(mockedJdbcTemplate.queryForObject(anyString(), any(GroupMapper.class), any())).thenThrow(QueryTimeoutException.class);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to get a group for a student with id " + studentId + ".",
                "Can't get a group for a student with id " + studentId + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            studentDAO.getStudentGroup(studentId);
            verify(mockedJdbcTemplate).queryForObject(anyString(), any(GroupMapper.class), any());
        } catch (DAOException daoException) {
            //do nothing
        }

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }
}