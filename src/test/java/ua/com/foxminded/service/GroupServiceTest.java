package ua.com.foxminded.service;

import static org.junit.jupiter.api.Assertions.*;
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
import ua.com.foxminded.domain.Group;
import ua.com.foxminded.repositories.exceptions.RepositoryException;
import ua.com.foxminded.repositories.interfaces.GroupRepository;
import ua.com.foxminded.service.aspects.GeneralServiceAspect;
import ua.com.foxminded.service.aspects.GroupAspect;
import ua.com.foxminded.service.exceptions.ServiceException;
import ua.com.foxminded.settings.SpringTestConfiguration;

@ContextConfiguration(classes = SpringTestConfiguration.class)
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
class GroupServiceTest {
    private ListAppender<ILoggingEvent> testAppender;
    
    @Autowired
    private GeneralServiceAspect generalServiceAspect;
    
    @Autowired
    private GroupAspect groupAspect;
    
    @Autowired
    private GroupService groupService;

    @MockBean
    private GroupRepository groupRepository;

    @BeforeEach
    void init() {
        ReflectionTestUtils.setField(groupService, "groupRepository", groupRepository);
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger generalLogger = (Logger) ReflectionTestUtils.getField(generalServiceAspect, "logger");
        Logger groupLogger = (Logger) ReflectionTestUtils.getField(groupAspect, "logger");
        
        testAppender = new ListAppender<>();
        testAppender.setContext(loggerContext);
        testAppender.start();
        
        generalLogger.addAppender(testAppender);
        groupLogger.addAppender(testAppender);
    }

    @AfterEach
    void tearDown() {
        testAppender.stop();
    }

    @Test
    void shouldCreateGroup() {
        Faculty faculty = new Faculty();
        faculty.setId(2);
        faculty.setName("Test faculty");
        Group testGroup = new Group();
        testGroup.setName("Forth group");
        testGroup.setFaculty(faculty);

        groupService.create(testGroup);
        verify(groupRepository).save(testGroup);
    }

    @Test
    void shouldGetAllGroups() {
        groupService.getAll();
        verify(groupRepository).findAll();
    }

    @Test
    void shouldGetGroupById() {
        int testGroupId = 1;
        Faculty faculty = new Faculty();
        faculty.setId(1);
        faculty.setName("Faculty");
        Group group = new Group();
        group.setId(testGroupId);
        group.setName("Group");
        group.setFaculty(faculty);
        
        when(groupRepository.findById(testGroupId)).thenReturn(Optional.of(group));
        
        groupService.getById(testGroupId);
        verify(groupRepository).findById(testGroupId);
    }

    @Test
    void shouldUpdateGroup() {
        int facultyId = 2;
        Faculty faculty = new Faculty();
        faculty.setId(facultyId);
        faculty.setName("Faculty");

        int groupId = 3;
        Group group = new Group();
        group.setId(groupId);
        group.setName("Group");
        group.setFaculty(faculty);

        groupService.update(group);
        verify(groupRepository).save(group);
    }

    @Test
    void shouldDeleteById() {
        int groupId = 100;
        Group group = new Group();
        group.setId(groupId);
        group.setName("Test");
        groupService.deleteById(groupId);
        verify(groupRepository).deleteById(groupId);
    }

    
    @Test
    void shouldThrowServiceExceptionWhenGroupIsNullWhileCreate() {
        Group group = null;
        assertThrows(ServiceException.class, () -> groupService.create(group));
    }

    @Test
    void shouldThrowServiceExceptionWhenGroupIdIsNotZeroWhileCreate() {
        Group group = new Group();
        group.setId(2);
        group.setName("Test name");
        Faculty faculty = new Faculty();
        faculty.setId(5);
        faculty.setName("Faculty 01");
        group.setFaculty(faculty);

        assertThrows(ServiceException.class, () -> groupService.create(group));
    }

    @Test
    void shouldThrowServiceExceptionWhenGroupNameIsNullWhileCreate() {
        Group group = new Group();
        Faculty faculty = new Faculty();
        faculty.setId(7);
        faculty.setName("Faculty name");
        group.setFaculty(faculty);

        assertThrows(ServiceException.class, () -> groupService.create(group));
    }

    @Test
    void shouldThrowServiceExceptionWhenGroupNameIsShortWhileCreate() {
        Group group = new Group();
        group.setName("e   ");
        Faculty faculty = new Faculty();
        faculty.setId(5);
        faculty.setName("Faculty name");

        assertThrows(ServiceException.class, () -> groupService.create(group));
    }

    @Test
    void shouldThrowServiceExceptionWhenGroupNameStartsWithWhiteSpaceWhileCreate() {
        Group group = new Group();
        group.setName(" Test");
        Faculty faculty = new Faculty();
        faculty.setId(7);
        faculty.setName("Faculty");

        assertThrows(ServiceException.class, () -> groupService.create(group));
    }

    @Test
    void shouldThrowServiceExceptionWhenRepositoryExceptionWhileCreate() {
        Group group = new Group();
        group.setName("Group");
        Faculty faculty = new Faculty();
        faculty.setId(7);
        faculty.setName("Faculty");
        group.setFaculty(faculty);

        doThrow(RepositoryException.class).when(groupRepository).save(group);
        assertThrows(ServiceException.class, () -> groupService.create(group));
    }

    @Test
    void shouldThrowServiceExceptionWhenRepositoryExceptionWhileGetAll() {
        when(groupRepository.findAll()).thenThrow(RepositoryException.class);
        assertThrows(ServiceException.class, () -> groupService.getAll());
    }

    @Test
    void shouldThrowServiceExceptionWhenFacultyIdIsZeroWhileGetById() {
        int testId = 0;
        assertThrows(ServiceException.class, () -> groupService.getById(testId));
    }

    @Test
    void shouldThrowServiceExceptionWhenRepositoryExceptionWhileGetById() {
        int testId = 2;
        when(groupRepository.findById(testId)).thenThrow(RepositoryException.class);
        assertThrows(ServiceException.class, () -> groupService.getById(testId));
    }

    @Test
    void shouldThrowServiceExceptionWhenGroupIsNullWhileUpdate() {
        Group group = null;
        assertThrows(ServiceException.class, () -> groupService.update(group));
    }

    @Test
    void shouldThrowServiceExceptionWhenGroupIsInvalidWhileUpdate() {
        Group group = new Group();
        group.setId(-54);
        group.setName("Group");
        Faculty faculty = new Faculty();
        faculty.setId(6);
        faculty.setName("Faculty");
        group.setFaculty(faculty);

        assertThrows(ServiceException.class, () -> groupService.update(group));
    }

    @Test
    void shouldThrowServiceExceptionWhenRepositoryExceptionWhileUpdate() {
        Group group = new Group();
        group.setId(5);
        group.setName("Group");
        Faculty faculty = new Faculty();
        faculty.setId(8);
        faculty.setName("Faculty");
        doThrow(RepositoryException.class).when(groupRepository).save(group);
        assertThrows(ServiceException.class, () -> groupService.update(group));
    }

    @Test
    void shouldThrowServiceExceptionWhenGroupIdIsZeroWhileDeleteById() {
        int testId = 0;
        assertThrows(ServiceException.class, () -> groupService.deleteById(testId));
    }

    @Test
    void shouldThrowServiceExceptioinWhenRepositoryExceptionWhileDeleteById() {
        int testId = 5;
        doThrow(RepositoryException.class).when(groupRepository).deleteById(testId);
        assertThrows(ServiceException.class, () -> groupService.deleteById(testId));
    }


    @Test
    void shouldGenerateLogsWhenGroupIsNullWhileCreate() {
        Group group = null;
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to create a new group: " + group + ".",
                "A group " + group + " can't be null when create."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            groupService.create(group);
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
    void shouldGenerateLogsWhenGroupIdIsNotZeroWhileCreate() {
        int testId = 4;
        Group group = new Group();
        group.setId(testId);
        group.setName("Group");
        Faculty faculty = new Faculty();
        faculty.setId(1);
        faculty.setName("Faculty");
        group.setFaculty(faculty);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to create a new group: " + group + ".",
                "A group " + group + " has wrong id " + testId + " which is not equal zero when create."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            groupService.create(group);
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
    void shouldGenerateLogsWhenGroupNameIsNullWhileCreate() {
        Group group = new Group();
        Faculty faculty = new Faculty();
        faculty.setId(2);
        faculty.setName("Facutly");
        group.setFaculty(faculty);

        String violationMessage = "Group name can't be null";

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to create a new group: " + group + ".",
                "The group " + group + " is not valid when create. There are errors: " + violationMessage + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            groupService.create(group);
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
    void shouldGenerateLogsWhenGroupNameIsNotValidWhileCreate() {
        Group group = new Group();
        group.setName(" Group");
        Faculty faculty = new Faculty();
        faculty.setId(5);
        faculty.setName("Facutly");
        group.setFaculty(faculty);

        String violationMessage = "Group name must have at least two symbols and start with non-white space";

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to create a new group: " + group + ".",
                "The group " + group + " is not valid when create. There are errors: " + violationMessage + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            groupService.create(group);
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
        Group group = new Group();
        group.setName("Group");
        Faculty faculty = new Faculty();
        faculty.setId(1);
        faculty.setName("Facutly");
        group.setFaculty(faculty);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to create a new group: " + group + ".",
                "There is some error in repositories layer when create an object " + group + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        doThrow(RepositoryException.class).when(groupRepository).save(group);

        try {
            groupService.create(group);
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
        Group group = new Group();
        group.setName("Group");
        Faculty faculty = new Faculty();
        faculty.setId(9);
        faculty.setName("Facutly");
        group.setFaculty(faculty);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(
                Arrays.asList("Try to create a new group: " + group + ".", "The object " + group + " was created."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        groupService.create(group);

        List<ILoggingEvent> actualLogs = testAppender.list;
        
        System.out.println(actualLogs);

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

        groupService.getAll();

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

        when(groupRepository.findAll()).thenThrow(RepositoryException.class);

        try {
            groupService.getAll();
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
        List<Group> expectedGroups = new ArrayList<>(Arrays.asList(new Group(), new Group(), new Group()));

        Faculty faculty1 = new Faculty();
        faculty1.setId(1);
        faculty1.setName("Faculty1");

        Faculty faculty2 = new Faculty();
        faculty2.setId(2);
        faculty2.setName("Facutly2");

        expectedGroups.get(0).setFaculty(faculty1);
        expectedGroups.get(1).setFaculty(faculty1);
        expectedGroups.get(2).setFaculty(faculty2);

        for (int i = 0; i < expectedGroups.size(); i++) {
            int index = i + 1;
            expectedGroups.get(i).setId(index);
            expectedGroups.get(i).setName("Test name: " + index);
        }

        when(groupRepository.findAll()).thenReturn(expectedGroups);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(
                Arrays.asList("Try to get all objects.", "The result is: " + expectedGroups + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        groupService.getAll();

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenGroupIdIsNegativeWhileGetById() {
        int negativeId = -3;

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to get an object by id: " + negativeId + ".",
                "A given id " + negativeId + " is less than 1 when getById."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            groupService.getById(negativeId);
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
        int testId = 7;

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to get an object by id: " + testId + ".",
                "There is some error in repositories layer when get object by id " + testId + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        when(groupRepository.findById(testId)).thenThrow(RepositoryException.class);

        try {
            groupService.getById(testId);
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
        int testId = 5;

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to get an object by id: " + testId + ".",
                "The entity is not found when get object by id " + testId + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        RepositoryException repositoryException = new RepositoryException("The result is empty", new NullPointerException());

        when(groupRepository.findById(testId)).thenThrow(repositoryException);

        try {
            groupService.getById(testId);
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

        Group group = new Group();
        group.setId(testId);
        group.setName("Test name");
        Faculty faculty = new Faculty();
        faculty.setId(2);
        faculty.setName("Faculty");
        group.setFaculty(faculty);

        when(groupRepository.findById(testId)).thenReturn(Optional.of(group));

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to get an object by id: " + testId + ".",
                "The result object with id " + testId + " is " + group + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        groupService.getById(testId);

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenGroupIsNullWhileUpdate() {
        Group group = null;

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(
                Arrays.asList("Try to update a group: " + group + ".", "An updated group " + group + " is null."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            groupService.update(group);
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
    void shouldGenerateLogsWhenGroupIdIsZeroWhileUpdate() {
        Group group = new Group();
        group.setName("Group");
        Faculty faculty = new Faculty();
        faculty.setId(4);
        faculty.setName("Faculty");
        group.setFaculty(faculty);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to update a group: " + group + ".",
                "An updated group " + group + " has wrong id " + group.getId() + " which is not positive."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            groupService.update(group);
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
    void shouldGenerateLogsWhenGroupHasInvalidFacultyWhileUpdate() {
        Group group = new Group();
        group.setId(4);
        group.setName("Group");
        Faculty faculty = new Faculty();
        faculty.setId(4);
        group.setFaculty(faculty);

        String violationMessage = "Faculty name can't be null";

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to update a group: " + group + ".",
                "The group " + group + " is not valid when update. There are errors: " + violationMessage + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            groupService.update(group);
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
        Group group = new Group();
        group.setId(4);
        group.setName("Group");
        Faculty faculty = new Faculty();
        faculty.setId(8);
        faculty.setName("Faculty");
        group.setFaculty(faculty);

        doThrow(RepositoryException.class).when(groupRepository).save(group);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to update a group: " + group + ".",
                "There is some error in repositories layer when update an object " + group + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            groupService.update(group);
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
        Group group = new Group();
        group.setId(6);
        group.setName("Group");
        Faculty faculty = new Faculty();
        faculty.setId(7);
        faculty.setName("Faculty");
        group.setFaculty(faculty);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(
                Arrays.asList("Try to update a group: " + group + ".", "The object " + group + " was updated."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        groupService.update(group);

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenGroupIdIsNegativeWhileDeleteById() {
        int testId = -6;

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to delete an object by id: " + testId + ".",
                "A given id " + testId + " is less than 1 when deleteById."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            groupService.deleteById(testId);
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

        doThrow(RepositoryException.class).when(groupRepository).deleteById(testId);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to delete an object by id: " + testId + ".",
                "There is some error in repositories layer when delete an object by id " + testId + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            groupService.deleteById(testId);
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
        int testId = 99;

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to delete an object by id: " + testId + ".",
                "An object was deleted by id " + testId + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        groupService.deleteById(testId);

        List<ILoggingEvent> actualLogs = testAppender.list;

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }
}