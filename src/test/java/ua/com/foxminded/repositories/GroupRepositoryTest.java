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
import ua.com.foxminded.domain.Group;
import ua.com.foxminded.repositories.aspects.GeneralRepositoryAspect;
import ua.com.foxminded.repositories.exceptions.RepositoryException;
import ua.com.foxminded.repositories.interfaces.GroupRepository;

@DataJpaTest(showSql = true)
@Import({AopAutoConfiguration.class, GeneralRepositoryAspect.class})
@TestPropertySource("/application-test.properties")
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
class GroupRepositoryTest {
    private final String testData = "/Test data.sql";
    
    private ListAppender<ILoggingEvent> testAppender;
    
    @Autowired
    private GeneralRepositoryAspect generalRepositoryAspect;
    
    @Autowired
    private TestEntityManager testEntityManager;
    
    @Autowired
    @SpyBean
    private GroupRepository groupRepository;
    
    private List<Group> expectedGroups;

    @BeforeEach
    void setUp() {
        Logger logger = (Logger) ReflectionTestUtils.getField(generalRepositoryAspect, "logger");
        testAppender = new ListAppender<>();
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        testAppender.setContext(loggerContext);
        testAppender.start();
        logger.addAppender(testAppender);
        
        expectedGroups = new ArrayList<>(Arrays.asList(new Group(), new Group(), new Group()));
        List<String> groupNames = new ArrayList<>(Arrays.asList("TestGroup1", "TestGroup2", "TestGroup3"));
        List<Integer> groupIndexes = new ArrayList<>(Arrays.asList(1, 2, 3));
        
        Faculty faculty1 = new Faculty();
        faculty1.setId(1);
        faculty1.setName("TestFaculty1");
        Faculty faculty2 = new Faculty();
        faculty2.setId(2);
        faculty2.setName("TestFaculty2");
        
        List<Faculty> expectedFaculties = new ArrayList<>(Arrays.asList(faculty1, faculty2, faculty1));
        
        for (int i = 0; i < expectedGroups.size(); i++) {
            expectedGroups.get(i).setId(groupIndexes.get(i));
            expectedGroups.get(i).setName(groupNames.get(i));
            expectedGroups.get(i).setFaculty(expectedFaculties.get(i));
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        testAppender.stop();
    }

    @Test
    @Sql(testData)
    void shouldCreateGroup() {
        int facultyId = 1;
        Faculty faculty = testEntityManager.find(Faculty.class, facultyId);
        int maxGroupId = testEntityManager.getEntityManager().createQuery("FROM Group", Group.class)
                .getResultStream().max((group1, group2) -> Integer.compare(group1.getId(), group2.getId())).get().getId();
        
        Group testGroup = new Group();
        testGroup.setName("TestGroup");
        testGroup.setFaculty(faculty);
        
        int nextGroupId = maxGroupId + 1;
        
        Group expectedGroup = new Group();
        expectedGroup.setId(nextGroupId);
        expectedGroup.setName("TestGroup");
        expectedGroup.setFaculty(faculty);
        groupRepository.save(testGroup);
        Group actualGroup = testEntityManager.find(Group.class, nextGroupId);
        assertEquals(expectedGroup, actualGroup);
    }

    @Test
    @Sql(testData)
    void shouldFindAllGroups() {
        List<Group> actualGroups = groupRepository.findAll();
        assertTrue(expectedGroups.containsAll(actualGroups) && actualGroups.containsAll(expectedGroups));
    }

    @Test
    @Sql(testData)
    void shouldFindGroupById() {
        int testId = 2;
        Group expectedGroup = testEntityManager.find(Group.class, testId);
        Optional<Group> expectedResult = Optional.of(expectedGroup);
        assertEquals(expectedResult, groupRepository.findById(testId));
    }

    @Test
    @Sql(testData)
    void shouldUpdateGroup() {
        int testGroupId = 2;
        Group testGroup = groupRepository.findById(testGroupId).get();
        testGroup.setName("TestGroupUpdated");
        groupRepository.save(testGroup);
        assertEquals(testGroup, testEntityManager.find(Group.class, testGroupId));
    }

    @Test
    @Sql(testData)
    void shouldDeleteGroupById() {
        int deletedGroupId = 2;
        groupRepository.deleteById(deletedGroupId);
        
        Group afterDeletingGroup = testEntityManager.find(Group.class, deletedGroupId);
        
        assertThat(afterDeletingGroup).isNull();
    }

    @Test
    void shouldThrowRepositoryExceptionWhenDataAccessExceptionWhileSave() {
        Group group = null;
        RuntimeException exception = assertThrows(RepositoryException.class, () -> groupRepository.save(group));
    
        String message = "Can't save/update the object.";
        assertEquals(message, exception.getMessage());
    }

    @Test
    void shouldThrowRepositoryExceptionWhenResultIsNullPointerExceptionWhileFindById() {
        int testId = 1;
        RuntimeException exception = assertThrows(RepositoryException.class, () -> groupRepository.findById(testId));
    
        String message = "There is no object with specified id.";
        assertEquals(message, exception.getMessage());
    }

    @Test
    @Sql(testData)
    void shouldThrowRepositoryExceptionWhenDataAccessExceptionWhileFindById() {
        Integer testId = null;
        RuntimeException exception = assertThrows(RepositoryException.class, () -> groupRepository.findById(testId));
    
        String message = "Can't find an object by id.";
        assertEquals(message, exception.getMessage());
    }

    @Test
    void shouldThrowRepositoryExceptionWhenDataAccessExceptionWhileDeleteById() {
        Integer testId = null;
        RuntimeException exception = assertThrows(RepositoryException.class, () -> groupRepository.deleteById(testId));
    
        String message = "Can't delete an object by id.";
        assertEquals(message, exception.getMessage());
    }

    @Test
    @Sql(testData)
    void shouldGenerateLogsWhenSaveGroup() {
        Group testGroup = new Group();
        testGroup.setName("Test Group");
        
        int facultyId = 1;
        Faculty faculty = testEntityManager.find(Faculty.class, facultyId);
        
        testGroup.setFaculty(faculty);
        
        int nextId = testEntityManager.getEntityManager().createQuery("FROM Group", Group.class).getResultStream()
                .max((group1, group2) -> Integer.compare(group1.getId(), group2.getId())).get().getId();
        
        Group loggingResultGroup = new Group();
        loggingResultGroup.setId(++nextId);
        loggingResultGroup.setName("Test Group");
        loggingResultGroup.setFaculty(faculty);
        
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to save/update an object: " + testGroup + ".",
                "The object " + loggingResultGroup + " was saved/updated."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        groupRepository.save(testGroup);

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenThrowDataAccessExceptionWhileSave() {
        Group testGroup = null;
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to save/update an object: " + testGroup + ".",
                "Can't save/update the object: " + testGroup + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }
        try {
            groupRepository.save(testGroup);
        } catch (RepositoryException exception) {
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

        groupRepository.findAll();
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
                Arrays.asList("Try to find all objects.", "The result is: " + expectedGroups + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        groupRepository.findAll();

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
        Optional<Group> expectedGroup = expectedGroups.stream().filter(group -> group.getId() == testId).findFirst();
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to find an object by id: " + testId + ".",
                "The result object with id " + testId + " is " + expectedGroup + "."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        groupRepository.findById(testId);

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenResultIsNullPointerExceptionWhileFindById() {
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
            groupRepository.findById(testId);
        } catch (RepositoryException repositoryEcxeption) {
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
    void shouldGenerateLogsWhenDataAccessExceptionWhileFindById() {
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
            groupRepository.findById(testId);
        } catch (RepositoryException repositoryEcxeption) {
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
        int testId = 3;

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to delete an object by id " + testId+ ".",
                "The object with id " + testId + " was deleted."));
        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        groupRepository.deleteById(testId);

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
            groupRepository.deleteById(testId);
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
