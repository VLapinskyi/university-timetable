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
import ua.com.foxminded.domain.Gender;
import ua.com.foxminded.domain.Lecturer;
import ua.com.foxminded.repositories.aspects.GeneralRepositoryAspect;
import ua.com.foxminded.repositories.exceptions.RepositoryException;
import ua.com.foxminded.repositories.interfaces.LecturerRepository;

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
        List<String> phoneNumbers = new ArrayList<>(Arrays.asList("+380991111111", "+380125263741", "+380457895263"));
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
    }

    @AfterEach
    void tearDown() throws Exception {
        testAppender.stop();
    }

    @Test
    void shouldCreateLecturer() {
        int expectedId = 1;
        Lecturer expectedLecturer = new Lecturer();
        expectedLecturer.setId(expectedId);
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
        
        lecturerRepository.save(testLecturer);
        Lecturer actualLecturer = testEntityManager.find(Lecturer.class, expectedId);
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
        Optional<Lecturer> expectedLecturer = expectedLecturers.stream().filter(lecturer -> lecturer.getId() == testId).findAny();
        assertEquals(expectedLecturer, lecturerRepository.findById(testId));
    }

    @Test
    @Sql(testData)
    void shouldUpdateLecturer() {
        int testId = 6;

        Lecturer testLecturer = testEntityManager.find(Lecturer.class, testId);
        testLecturer.setFirstName("Viacheslav");
        
        lecturerRepository.save(testLecturer);
        assertEquals(testLecturer, testEntityManager.find(Lecturer.class, testId));
    }

    @Test
    @Sql(testData)
    void shouldDeleteById() {
        int deletedId = 4;
        
        lecturerRepository.deleteById(deletedId);
        
        Lecturer afterDeletingLecturer = testEntityManager.find(Lecturer.class, deletedId);
        
        assertThat(afterDeletingLecturer).isNull();
    }

    @Test
    void shouldThrowRepositoryExceptionWhenDataAccessExceptionWhileSave() {
        Lecturer lecturer = null;
        RuntimeException exception = assertThrows(RepositoryException.class, () -> lecturerRepository.save(lecturer));
    
        String message = "Can't save/update the object.";
        assertEquals(message, exception.getMessage());
    }

    @Test
    void shouldThrowRepositoryExceptionWhenResultyIsNullPointerExceptionWhileFindById() {
        int testId = 1;
        RuntimeException exception = assertThrows(RepositoryException.class, () -> lecturerRepository.findById(testId));
    
        String message = "There is no object with specified id.";
        assertEquals(message, exception.getMessage());
    }

    @Test
    @Sql(testData)
    void shouldThrowRepositoryExcpetionWhenDataAccessExceptionWhileFindById() {
        Integer testId = null;
        RuntimeException exception = assertThrows(RepositoryException.class, () -> lecturerRepository.findById(testId));
    
        String message = "Can't find an object by id.";
        assertEquals(message, exception.getMessage());
    }

    @Test
    void shouldThrowRepositoryExceptionWhenDataAccessExceptionWhileDeleteById() {
        int testId = 232;
        RuntimeException exception = assertThrows(RepositoryException.class, () -> lecturerRepository.deleteById(testId));
    
        String message = "Can't delete an object by id.";
        assertEquals(message, exception.getMessage());
    }

    @Test
    void shouldGenerateLogsWhenSaveLecturer() {
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
                "Try to save/update an object: " + testLecturer + ".", "The object " + expectedLecturer + " was saved/updated."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lecturerRepository.save(testLecturer);
        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenThrowDataAccessExceptionWhileSave() {
        Lecturer testLecturer = null;

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to save/update an object: " + testLecturer + ".", "Can't save/update the object: " + testLecturer + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            lecturerRepository.save(testLecturer);
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
    @Sql(testData)
    void shouldGenerateLogsWhenFindById() {
        int testId = 4;
        Optional<Lecturer> expectedLecturer = expectedLecturers.stream().filter(lecturer -> lecturer.getId() == testId)
                .findFirst();
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
    void shouldGenerateLogsWhenDeleteById() {
        int testId = 5;

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to delete an object by id " + testId + ".",
                "The object with id " + testId + " was deleted."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        lecturerRepository.deleteById(testId);
        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenThrowDataAccessExceptionWhileDeleteById() {
        int testId = 6;

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to delete an object by id " + testId + ".",
                "Can't delete an object by id " + testId + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }
        try {
            lecturerRepository.deleteById(testId);
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
