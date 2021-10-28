package ua.com.foxminded.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import ua.com.foxminded.domain.Gender;
import ua.com.foxminded.domain.Lecturer;
import ua.com.foxminded.domain.Role;
import ua.com.foxminded.repositories.aspects.GeneralRepositoryAspect;
import ua.com.foxminded.repositories.exceptions.RepositoryException;

@DataJpaTest(showSql = true)
@Import({AopAutoConfiguration.class, GeneralRepositoryAspect.class})
@TestPropertySource("/application-test.properties")
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
class LecturerRepositoryTest {
private final String testData = "/Test data.sql";
    
    private ListAppender<ILoggingEvent> testAppender;
    
    @Autowired
    private GeneralRepositoryAspect generalRepositoryAspect;
    
    @Autowired
    private TestEntityManager testEntityManager;
    
    @MockBean
    private EntityManager mockedEntityManager;
    
    @Autowired
    @SpyBean
    private LecturerRepository lecturerRepository;
    
    private List<Lecturer> expectedLecturers;

    @BeforeEach
    void setUp() {
        Logger logger = (Logger) ReflectionTestUtils.getField(generalRepositoryAspect, "logger");
        testAppender = new ListAppender<>();
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        testAppender.setContext(loggerContext);
        testAppender.start();
        logger.addAppender(testAppender);
        
        expectedLecturers = new ArrayList<>(Arrays.asList(new Lecturer(), new Lecturer(), new Lecturer()));
        List<Integer> indexes = new ArrayList<>(Arrays.asList(1, 2, 3));
        List<String> firstNames = new ArrayList<>(Arrays.asList("Olena", "Ihor", "Vasyl"));
        List<String> lastNames = new ArrayList<>(Arrays.asList("Skladenko", "Zakharchuk", "Dudchenko"));
        List<Gender> gendersForLecturers = new ArrayList<>(Arrays.asList(Gender.FEMALE, Gender.MALE, Gender.MALE));
        List<String> phoneNumbers = new ArrayList<>(Arrays.asList("+380991111111", "+380125263741", "+3804578963"));
        List<String> emails = new ArrayList<>(Arrays.asList("oskladenko@gmail.com", "i.zakharchuk@gmail.com", "vdudchenko@test.com"));
        int startIndex = 3;
        for (int i = 0; i < expectedLecturers.size(); i++) {   
            expectedLecturers.get(i).setId(indexes.get(i) + startIndex);
            expectedLecturers.get(i).setFirstName(firstNames.get(i));
            expectedLecturers.get(i).setLastName(lastNames.get(i));
            expectedLecturers.get(i).setGender(gendersForLecturers.get(i));
            expectedLecturers.get(i).setPhoneNumber(phoneNumbers.get(i));
            expectedLecturers.get(i).setEmail(emails.get(i));
        }
        
        ReflectionTestUtils.setField(lecturerRepository, "entityManager", testEntityManager.getEntityManager());
    }

    @AfterEach
    void tearDown() throws Exception {
        testAppender.stop();
    }

    @Test
    void shouldCreateLecturer() {
        Lecturer expectedLecturer = new Lecturer();
        expectedLecturer.setId(1);
        expectedLecturer.setFirstName("First-name");
        expectedLecturer.setLastName("Last-name");
        expectedLecturer.setGender(Gender.MALE);
        expectedLecturer.setPhoneNumber("+380457896321");
        expectedLecturer.setEmail("test@email.com");

        Lecturer testLecturer = new Lecturer();
        testLecturer.setFirstName("First-name");
        testLecturer.setLastName("Last-name");
        testLecturer.setGender(Gender.MALE);
        testLecturer.setPhoneNumber("+380457896321");
        testLecturer.setEmail("test@email.com");
        
        lecturerRepository.create(testLecturer);
        Lecturer actualLecturer = lecturerRepository.findAll().stream().findFirst().get();
        assertEquals(expectedLecturer, actualLecturer);
    }

    @Test
    @Sql(testData)
    void shouldFindAllLecturers() {
        List<Lecturer> actualLecturers = lecturerRepository.findAll();
        assertTrue(expectedLecturers.containsAll(actualLecturers) && actualLecturers.containsAll(expectedLecturers));
    }

    @Test
    @Sql(testData)
    void shouldFindLecturerById() {
        int testId = 5;
        Lecturer expectedLecturer = expectedLecturers.stream().filter(lecturer -> lecturer.getId() == testId).findAny().get();
        assertEquals(expectedLecturer, lecturerRepository.findById(testId));
    }

    @Test
    @Sql(testData)
    void shouldUpdateLecturer() {
        int testId = 2;

        Lecturer testLecturer = lecturerRepository.findById(testId);
        testLecturer.setFirstName("Viacheslav");
        
        lecturerRepository.update(testLecturer);
        assertEquals(testLecturer, lecturerRepository.findById(testId));
    }

    @Test
    @Sql(testData)
    void shouldDeleteLecturer() {
        int deletedId = 2;
        Lecturer deletedLecturer = testEntityManager.find(Lecturer.class, deletedId);
        
        lecturerRepository.delete(deletedLecturer);
        
        Lecturer afterDeletingLecturer = testEntityManager.find(Lecturer.class, deletedId);
        
        assertThat(afterDeletingLecturer).isNull();
    }

    @Test
    void shouldThrowRepositoryExceptionWhenPersistenceExceptionWhileCreate() {
        int wrongId = 8;
        Lecturer lecturer = new Lecturer();
        lecturer.setId(wrongId);
        lecturer.setGender(Gender.MALE);
        assertThrows(RepositoryException.class, () -> lecturerRepository.create(lecturer));
    }

    @Test
    void shouldThrowRepositoryExceptionWhenPersistenceExceptionWhileFindAll() {
        when(lecturerRepository.findAll()).thenThrow(PersistenceException.class);
        assertThrows(RepositoryException.class, () -> lecturerRepository.findAll());
    }

    @Test
    void shouldThrowRepositoryExceptionWhenResultyIsNullPointerExceptionWhileFindById() {
        int testId = 1;
        assertThrows(RepositoryException.class, () -> lecturerRepository.findById(testId));
    }

    @Test
    @Sql(testData)
    void shouldThrowRepositoryExcpetionWhenPersistenceExceptionWhileFindById() {
        int testId = 6;
        when(lecturerRepository.findById(testId)).thenThrow(PersistenceException.class);
        assertThrows(RepositoryException.class, () -> lecturerRepository.findById(testId));
    }

    @Test
    void shouldThrowRepositoryExceptionWhenPersistenceExceptionWhileUpdate() {
        Lecturer lecturer = new Lecturer();
        lecturer.setId(5);
        lecturer.setFirstName("Alla");
        lecturer.setLastName("Matviichuk");
        lecturer.setGender(Gender.FEMALE);
        lecturer.setPhoneNumber("+380657485963");
        lecturer.setEmail("test@test.com");
        doThrow(PersistenceException.class).when(lecturerRepository).update(lecturer);
        assertThrows(RepositoryException.class, () -> lecturerRepository.update(lecturer));
    }

    @Test
    void shouldThrowRepositoryExceptionWhenPersistenceExceptionWhileDelete() {
        Lecturer lecturer = new Lecturer();
        lecturer.setId(1);
        lecturer.setFirstName("Petro");
        lecturer.setLastName("Petrov");
        lecturer.setGender(Gender.MALE);
        lecturer.setPhoneNumber("+380125478963");
        lecturer.setEmail("test@test.com");
        doThrow(PersistenceException.class).when(lecturerRepository).delete(lecturer);
        assertThrows(RepositoryException.class, () -> lecturerRepository.delete(lecturer));
    }

    @Test
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
                "Try to insert a new object: " + testLecturer + ".", "The object " + expectedLecturer + " was inserted."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lecturerRepository.create(testLecturer);
        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenThrowPersistenceExceptionWhileCreate() {
        int wrongId = 5;
        Lecturer testLecturer = new Lecturer();
        testLecturer.setGender(Gender.MALE);
        testLecturer.setId(wrongId);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to insert a new object: " + testLecturer + ".", "Can't insert the object: " + testLecturer + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lecturerRepository.create(testLecturer);
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

        lecturerRepository.findAll();

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
                Arrays.asList("Try to find all objects.", "The result is: " + expectedLecturers + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lecturerRepository.findAll();
        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenThrowPersistenceExceptionWhileFindAll() {
        ReflectionTestUtils.setField(lecturerRepository, "entityManager", mockedEntityManager);
        doThrow(PersistenceException.class).when(mockedEntityManager).createQuery("FROM Lecturer where role = '" + Role.LECTURER + "'", Lecturer.class);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(
                Arrays.asList("Try to find all objects.", "Can't find all objects."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lecturerRepository.findAll();
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
    void shouldGenerateLogsWhenFindById() {
        int testId = 4;
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

        lecturerRepository.findById(testId);
        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
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
            lecturerRepository.findById(testId);
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
    void shouldGenerateLogsWhenThrowPersistenceExceptionWhileFindById() {
        int testId = 2;

        ReflectionTestUtils.setField(lecturerRepository, "entityManager", mockedEntityManager);
        when(mockedEntityManager.find(Lecturer.class, testId)).thenThrow(PersistenceException.class);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to find an object by id: " + testId + ".",
                "Can't find an object by id " + testId + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lecturerRepository.findById(testId);
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
    void shouldGenerateLogsWhenUpdate() {
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

        lecturerRepository.update(lecturer);
        List<ILoggingEvent> actualLogs = testAppender.list;

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

        ReflectionTestUtils.setField(lecturerRepository, "entityManager", mockedEntityManager);
        when(mockedEntityManager.merge(lecturer)).thenThrow(PersistenceException.class);

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
            lecturerRepository.update(lecturer);
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
    void shouldGenerateLogsWhenDelete() {
        int testId = 3;
        Lecturer deletedLecturer = testEntityManager.find(Lecturer.class, testId);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to delete an object " + deletedLecturer + ".",
                "The object " + deletedLecturer + " was deleted."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lecturerRepository.delete(deletedLecturer);
        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenThrowPersistenceExceptionWhileDelete() {
        int testId = 6;
        Lecturer deletedLecturer = expectedLecturers.stream().filter(lecturer -> lecturer.getId() == testId).findAny().get();

        ReflectionTestUtils.setField(lecturerRepository, "entityManager", mockedEntityManager);
        when(mockedEntityManager.merge(deletedLecturer)).thenThrow(PersistenceException.class);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to delete an object " + deletedLecturer + ".",
                "Can't delete an object " + deletedLecturer + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }
        try {
            lecturerRepository.delete(deletedLecturer);
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
