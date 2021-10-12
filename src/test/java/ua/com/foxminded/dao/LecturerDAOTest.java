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
import ua.com.foxminded.domain.Gender;
import ua.com.foxminded.domain.Lecturer;
import ua.com.foxminded.settings.SpringTestConfiguration;
import ua.com.foxminded.settings.TestAppender;

@ContextConfiguration(classes = { SpringTestConfiguration.class })
@ExtendWith(SpringExtension.class)
class LecturerDAOTest {
    private final ClassPathResource testData = new ClassPathResource("/Test data.sql");
    private final ClassPathResource testTablesCreator = new ClassPathResource("/Creating tables.sql");
    private final ClassPathResource testDatabaseCleaner = new ClassPathResource("/Clearing database.sql");

    private TestAppender testAppender = new TestAppender();
    
    @Autowired
    private LecturerDAO lecturerDAO;
    
    @Autowired
    private SessionFactory sessionFactory;
    
    private List<Lecturer> expectedLecturers;
    private Connection connection;
    
    @Mock
    private SessionFactory mockedSessionFactory;

    @BeforeEach
    @Transactional
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        connection = ((SessionImpl)sessionFactory.getCurrentSession()).connection();
        ScriptUtils.executeSqlScript(connection, testTablesCreator);

        expectedLecturers = new ArrayList<>(Arrays.asList(new Lecturer(), new Lecturer(), new Lecturer()));
        List<Integer> indexes = new ArrayList<>(Arrays.asList(1, 2, 3));
        List<String> firstNames = new ArrayList<>(Arrays.asList("Olena", "Ihor", "Vasyl"));
        List<String> lastNames = new ArrayList<>(Arrays.asList("Skladenko", "Zakharchuk", "Dudchenko"));
        List<Gender> gendersForLecturers = new ArrayList<>(Arrays.asList(Gender.FEMALE, Gender.MALE, Gender.MALE));
        List<String> phoneNumbers = new ArrayList<>(Arrays.asList("+380991111111", null, null));
        List<String> emails = new ArrayList<>(Arrays.asList("oskladenko@gmail.com", "i.zakharchuk@gmail.com", null));
        for (int i = 0; i < expectedLecturers.size(); i++) {
            expectedLecturers.get(i).setId(indexes.get(i));
            expectedLecturers.get(i).setFirstName(firstNames.get(i));
            expectedLecturers.get(i).setLastName(lastNames.get(i));
            expectedLecturers.get(i).setGender(gendersForLecturers.get(i));
            expectedLecturers.get(i).setPhoneNumber(phoneNumbers.get(i));
            expectedLecturers.get(i).setEmail(emails.get(i));
        }
    }

    @AfterEach
    @Transactional
    void tearDown() throws Exception {
        testAppender.cleanEventList();
        ReflectionTestUtils.setField(lecturerDAO, "sessionFactory", sessionFactory);
        ScriptUtils.executeSqlScript(connection, testDatabaseCleaner);
    }

    @Test
    @Transactional
    void shouldCreateLecturer() {
        Lecturer expectedLecturer = new Lecturer();
        expectedLecturer.setId(1);
        expectedLecturer.setFirstName("First-name");
        expectedLecturer.setLastName("Last-name");
        expectedLecturer.setGender(Gender.MALE);
        expectedLecturer.setPhoneNumber("1233");

        Lecturer testLecturer = new Lecturer();
        testLecturer.setFirstName("First-name");
        testLecturer.setLastName("Last-name");
        testLecturer.setGender(Gender.MALE);
        testLecturer.setPhoneNumber("1233");

        lecturerDAO.create(testLecturer);
        Lecturer actualLecturer = lecturerDAO.findAll().stream().findFirst().get();
        assertEquals(expectedLecturer, actualLecturer);
    }

    @Test
    @Transactional
    void shouldFindAllLecturers() {
        ScriptUtils.executeSqlScript(connection, testData);
        List<Lecturer> actualLecturers = lecturerDAO.findAll();
        assertTrue(expectedLecturers.containsAll(actualLecturers) && actualLecturers.containsAll(expectedLecturers));
    }

    @Test
    @Transactional
    void shouldFindLecturerById() {
        ScriptUtils.executeSqlScript(connection, testData);
        int testId = 2;
        Lecturer expectedLecturer = expectedLecturers.stream().filter(lecturer -> lecturer.getId() == testId).findAny().get();
        assertEquals(expectedLecturer, lecturerDAO.findById(testId));
    }

    @Test
    @Transactional
    void shouldUpdateLecturer() {
        ScriptUtils.executeSqlScript(connection, testData);
        int testId = 2;

        Lecturer testLecturer = lecturerDAO.findById(testId);
        testLecturer.setFirstName("Viacheslav");
        
        lecturerDAO.update(testLecturer);
        assertEquals(testLecturer, lecturerDAO.findById(testId));
    }

    @Test
    @Transactional
    void shouldDeleteLecturer() {
        ScriptUtils.executeSqlScript(connection, testData);
        int deletedId = 2;
        Lecturer deletedLecturer = new Lecturer();
        for (int i = 0; i < expectedLecturers.size(); i++) {
            if (expectedLecturers.get(i).getId() == deletedId) {
                Lecturer lecturerFromList = expectedLecturers.get(i);
                deletedLecturer.setFirstName(lecturerFromList.getFirstName());
                deletedLecturer.setLastName(lecturerFromList.getLastName());
                deletedLecturer.setGender(lecturerFromList.getGender());
                deletedLecturer.setId(lecturerFromList.getId());
                deletedLecturer.setEmail(lecturerFromList.getEmail());
                deletedLecturer.setPhoneNumber(lecturerFromList.getPhoneNumber());
                
                expectedLecturers.remove(i);
                i--;
            }
        }
        lecturerDAO.delete(deletedLecturer);
        List<Lecturer> actualLecturers = lecturerDAO.findAll();
        assertTrue(expectedLecturers.containsAll(actualLecturers) && actualLecturers.containsAll(expectedLecturers));
    }

    @Test
    @Transactional
    void shouldThrowDAOExceptionWhenPersistenceExceptionWhileCreate() {
        Lecturer lecturer = new Lecturer();
        lecturer.setId(8);
        lecturer.setGender(Gender.MALE);
        assertThrows(DAOException.class, () -> lecturerDAO.create(lecturer));
    }

    @Test
    @Transactional
    void shouldThrowDAOExceptionWhenPersistenceExceptionWhileFindAll() {
        ReflectionTestUtils.setField(lecturerDAO, "sessionFactory", mockedSessionFactory);
        when(mockedSessionFactory.getCurrentSession()).thenThrow(PersistenceException.class);
        assertThrows(DAOException.class, () -> lecturerDAO.findAll());
    }

    @Test
    @Transactional
    void shouldThrowDAOExceptionWhenResultyIsNullPointerExceptionWhileFindById() {
        int testId = 1;
        assertThrows(DAOException.class, () -> lecturerDAO.findById(testId));
    }

    @Test
    @Transactional
    void shouldThrowDAOExcpetionWhenPersistenceExceptionWhileFindById() {
        int testId = 1;
        ReflectionTestUtils.setField(lecturerDAO, "sessionFactory", mockedSessionFactory);
        when(mockedSessionFactory.getCurrentSession()).thenThrow(PersistenceException.class);
        assertThrows(DAOException.class, () -> lecturerDAO.findById(testId));
    }

    @Test
    @Transactional
    void shouldThrowDAOExceptionWhenPersistenceExceptionWhileUpdate() {
        Lecturer lecturer = new Lecturer();
        lecturer.setId(5);
        lecturer.setFirstName("Alla");
        lecturer.setLastName("Matviichuk");
        lecturer.setGender(Gender.FEMALE);
        lecturer.setPhoneNumber("+380657485963");
        lecturer.setEmail("test@test.com");
        ReflectionTestUtils.setField(lecturerDAO, "sessionFactory", mockedSessionFactory);
        when(mockedSessionFactory.getCurrentSession()).thenThrow(PersistenceException.class);
        assertThrows(DAOException.class, () -> lecturerDAO.update(lecturer));
    }

    @Test
    @Transactional
    void shouldThrowDAOExceptionWhenPersistenceExceptionWhileDelete() {
        Lecturer lecturer = new Lecturer();
        lecturer.setId(1);
        lecturer.setFirstName("Petro");
        lecturer.setLastName("Petrov");
        lecturer.setGender(Gender.MALE);
        lecturer.setPhoneNumber("+380125478963");
        lecturer.setEmail("test@test.com");
        ReflectionTestUtils.setField(lecturerDAO, "sessionFactory", mockedSessionFactory);
        when(mockedSessionFactory.getCurrentSession()).thenThrow(PersistenceException.class);
        assertThrows(DAOException.class, () -> lecturerDAO.delete(lecturer));
    }

    @Test
    @Transactional
    void shouldGenerateLogsWhenCreateLecturer() {
        Lecturer testLecturer = new Lecturer();
        testLecturer.setFirstName("Roman");
        testLecturer.setLastName("Dudchenko");
        testLecturer.setGender(Gender.MALE);
        testLecturer.setPhoneNumber("+380998765432");
        testLecturer.setEmail("test@test.com");

        Lecturer expectedLecturer = new Lecturer();
        expectedLecturer.setId(1);
        expectedLecturer.setFirstName("Roman");
        expectedLecturer.setLastName("Dudchenko");
        expectedLecturer.setGender(Gender.MALE);
        expectedLecturer.setPhoneNumber("+380998765432");
        expectedLecturer.setEmail("test@test.com");
        
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to insert a new object: " + expectedLecturer + ".", "The object " + expectedLecturer + " was inserted."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lecturerDAO.create(testLecturer);
        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    @Transactional
    void shouldGenerateLogsWhenThrowPersistenceExceptionWhileCreate() {
        Lecturer testLecturer = new Lecturer();
        testLecturer.setGender(Gender.MALE);
        testLecturer.setId(5);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to insert a new object: " + testLecturer + ".", "Can't insert the object: " + testLecturer + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lecturerDAO.create(testLecturer);
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

        lecturerDAO.findAll();

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
                Arrays.asList("Try to find all objects.", "The result is: " + expectedLecturers + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lecturerDAO.findAll();
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
        ReflectionTestUtils.setField(lecturerDAO, "sessionFactory", mockedSessionFactory);
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
            lecturerDAO.findAll();
            verify(mockedSessionFactory.getCurrentSession()).createQuery(anyString(), Lecturer.class);
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
        int testId = 2;
        Lecturer expectedLecturer = expectedLecturers.stream().filter(lecturer -> lecturer.getId() == testId)
                .findFirst().get();
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to find an object by id: " + testId + ".",
                "The result object with id " + testId + " is " + expectedLecturer + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lecturerDAO.findById(testId);
        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    @Transactional
    void shouldGenerateLogsWhenResultIsNullPointerExceptionWhileFindById() {
        int testId = 2;
        
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to find an object by id: " + testId + ".",
                "There is no result when find an object by id " + testId + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lecturerDAO.findById(testId);
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
        int testId = 2;

        ReflectionTestUtils.setField(lecturerDAO, "sessionFactory", mockedSessionFactory);
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
            lecturerDAO.findById(testId);
            verify(mockedSessionFactory.getCurrentSession()).get(Lecturer.class, testId);
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
        Lecturer lecturer = new Lecturer();
        lecturer.setId(1);
        lecturer.setFirstName("Roman");
        lecturer.setLastName("Dudchenko");
        lecturer.setGender(Gender.MALE);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(
                Arrays.asList("Try to update an object " + lecturer + ".",
                        "The object " + lecturer + " was updated."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lecturerDAO.update(lecturer);
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
        Lecturer lecturer = new Lecturer();
        lecturer.setId(2);
        lecturer.setFirstName("Roman");
        lecturer.setLastName("Dudchenko");
        lecturer.setGender(Gender.MALE);

        ReflectionTestUtils.setField(lecturerDAO, "sessionFactory", mockedSessionFactory);
        when(mockedSessionFactory.getCurrentSession()).thenThrow(PersistenceException.class);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(
                Arrays.asList("Try to update an object " + lecturer + ".",
                        "Can't update an object " + lecturer + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lecturerDAO.update(lecturer);
            verify(mockedSessionFactory.getCurrentSession()).update(lecturer);
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
        int testId = 3;
        Lecturer deletedLecturer = expectedLecturers.stream().filter(lecturer -> lecturer.getId() == testId).findAny().get();

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to delete an object " + deletedLecturer + ".",
                "The object " + deletedLecturer + " was deleted."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lecturerDAO.delete(deletedLecturer);
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
        int testId = 3;
        Lecturer deletedLecturer = expectedLecturers.stream().filter(lecturer -> lecturer.getId() == testId).findAny().get();

        ReflectionTestUtils.setField(lecturerDAO, "sessionFactory", mockedSessionFactory);
        when(mockedSessionFactory.getCurrentSession()).thenThrow(PersistenceException.class);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to delete an object " + deletedLecturer + ".",
                "Can't delete an object " + deletedLecturer + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }
        try {
            lecturerDAO.delete(deletedLecturer);
            verify(mockedSessionFactory.getCurrentSession()).delete(deletedLecturer);
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
