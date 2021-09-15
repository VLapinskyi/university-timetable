package ua.com.foxminded.service;

import static org.junit.jupiter.api.Assertions.*;
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
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ua.com.foxminded.dao.GroupDAO;
import ua.com.foxminded.dao.exceptions.DAOException;
import ua.com.foxminded.domain.Faculty;
import ua.com.foxminded.domain.Group;
import ua.com.foxminded.service.exceptions.ServiceException;
import ua.com.foxminded.settings.SpringConfiguration;
import ua.com.foxminded.settings.TestAppender;

@ContextConfiguration(classes = { SpringConfiguration.class })
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
class GroupServiceTest {
    private TestAppender testAppender = new TestAppender();

    @Autowired
    private GroupService groupService;

    @Mock
    private GroupDAO groupDAO;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(groupService, "groupDAO", groupDAO);
    }

    @AfterEach
    void tearDown() {
        testAppender.cleanEventList();
    }

    @Test
    void shouldCreateGroup() {
        Faculty faculty = new Faculty();
        faculty.setId(2);
        faculty.setName("Test faculty");
        Group testGroup = new Group();
        testGroup.setName("Forth group");
        testGroup.setFaculty(faculty);

        Group savedGroup = new Group();
        savedGroup.setId(3);
        savedGroup.setName("Third group");
        Group createdGroup = new Group();
        createdGroup.setId(4);
        when(groupDAO.findAll()).thenReturn(new ArrayList<Group>(Arrays.asList(savedGroup, createdGroup)));
        groupService.create(testGroup);
        verify(groupDAO).create(testGroup);
        verify(groupDAO).setGroupFaculty(testGroup.getFaculty().getId(), createdGroup.getId());
    }

    @Test
    void shouldGetAllGroups() {
        List<Faculty> faculties = new ArrayList<>(Arrays.asList(new Faculty(), new Faculty()));
        List<Integer> facultyIndexes = new ArrayList<>(Arrays.asList(1, 2));
        for (int i = 0; i < faculties.size(); i++) {
            faculties.get(i).setId(facultyIndexes.get(i));
        }

        List<Group> groups = new ArrayList<>(Arrays.asList(new Group(), new Group(), new Group()));
        List<Integer> groupsIndexes = new ArrayList<>(Arrays.asList(1, 2, 3));
        for (int i = 0; i < groups.size(); i++) {
            groups.get(i).setId(groupsIndexes.get(i));
        }

        List<Group> expectedGroups = new ArrayList<>(groups);
        expectedGroups.get(0).setFaculty(faculties.get(1));
        expectedGroups.get(1).setFaculty(faculties.get(1));
        expectedGroups.get(2).setFaculty(faculties.get(0));

        when(groupDAO.findAll()).thenReturn(groups);
        when(groupDAO.getGroupFaculty(groups.get(0).getId())).thenReturn(faculties.get(1));
        when(groupDAO.getGroupFaculty(groups.get(1).getId())).thenReturn(faculties.get(1));
        when(groupDAO.getGroupFaculty(groups.get(2).getId())).thenReturn(faculties.get(0));

        List<Group> actualGroups = groupService.getAll();
        assertTrue(expectedGroups.containsAll(actualGroups) && actualGroups.containsAll(expectedGroups));
        verify(groupDAO).findAll();
        for (int i = 0; i < groups.size(); i++) {
            verify(groupDAO).getGroupFaculty(groups.get(i).getId());
        }
    }

    @Test
    void shouldGetGroupById() {
        int testGroupId = 1;
        Group group = new Group();
        group.setId(testGroupId);

        Faculty faculty = new Faculty();
        faculty.setId(1);
        group.setFaculty(faculty);

        Group expectedGroup = new Group();
        expectedGroup.setId(testGroupId);
        expectedGroup.setFaculty(faculty);

        when(groupDAO.findById(testGroupId)).thenReturn(group);
        when(groupDAO.getGroupFaculty(testGroupId)).thenReturn(faculty);

        Group actualGroup = groupService.getById(testGroupId);
        assertEquals(expectedGroup, actualGroup);
        verify(groupDAO).findById(testGroupId);
        verify(groupDAO).getGroupFaculty(testGroupId);
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
        verify(groupDAO).update(groupId, group);
        verify(groupDAO).setGroupFaculty(facultyId, groupId);
    }

    @Test
    void shouldDeleteById() {
        int groupId = 100;
        groupService.deleteById(groupId);
        verify(groupDAO).deleteById(groupId);
    }

    @Test
    void shouldGetGroupsFromFaculty() {
        int facultyId = 4;
        List<Faculty> faculties = new ArrayList<>(Arrays.asList(new Faculty(), new Faculty()));
        faculties.get(0).setId(3);
        faculties.get(1).setId(4);

        List<Group> groups = new ArrayList<>(Arrays.asList(new Group(), new Group(), new Group()));
        groups.get(0).setId(1);
        groups.get(1).setId(2);
        groups.get(2).setId(3);

        List<Group> expectedGroups = new ArrayList<>(groups.subList(1, groups.size()));
        expectedGroups.stream().forEach(group -> group.setFaculty(faculties.get(1)));

        when(groupDAO.findAll()).thenReturn(groups);
        when(groupDAO.getGroupFaculty(groups.get(0).getId())).thenReturn(faculties.get(0));
        when(groupDAO.getGroupFaculty(groups.get(1).getId())).thenReturn(faculties.get(1));
        when(groupDAO.getGroupFaculty(groups.get(2).getId())).thenReturn(faculties.get(1));
        List<Group> actualGroups = groupService.getGroupsFromFaculty(facultyId);

        assertTrue(expectedGroups.containsAll(actualGroups) && actualGroups.containsAll(expectedGroups));
        verify(groupDAO).findAll();
        for (int i = 0; i < groups.size(); i++) {
            verify(groupDAO).getGroupFaculty(groups.get(i).getId());
        }
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
    void shouldThrowServiceExceptionWhenDAOExceptionWhileCreate() {
        Group group = new Group();
        group.setName("Group");
        Faculty faculty = new Faculty();
        faculty.setId(7);
        faculty.setName("Faculty");
        group.setFaculty(faculty);

        doThrow(DAOException.class).when(groupDAO).create(group);
        assertThrows(ServiceException.class, () -> groupService.create(group));
    }

    @Test
    void shouldThrowServiceExceptionWhenDAOExceptionWhileGetAll() {
        when(groupDAO.findAll()).thenThrow(DAOException.class);
        assertThrows(ServiceException.class, () -> groupService.getAll());
    }

    @Test
    void shouldThrowServiceExceptionWhenFacultyIdIsZeroWhileGetById() {
        int testId = 0;
        assertThrows(ServiceException.class, () -> groupService.getById(testId));
    }

    @Test
    void shouldThrowServiceExceptionWhenDAOExceptionWhileGetById() {
        int testId = 2;
        when(groupDAO.findById(testId)).thenThrow(DAOException.class);
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
    void shouldThrowServiceExceptionWhenDAOExceptionWhileUpdate() {
        Group group = new Group();
        group.setId(5);
        group.setName("Group");
        Faculty faculty = new Faculty();
        faculty.setId(8);
        faculty.setName("Faculty");
        doThrow(DAOException.class).when(groupDAO).update(group.getId(), group);
        assertThrows(ServiceException.class, () -> groupService.update(group));
    }

    @Test
    void shouldThrowServiceExceptionWhenGroupIdIsZeroWhileDeleteById() {
        int testId = 0;
        assertThrows(ServiceException.class, () -> groupService.deleteById(testId));
    }

    @Test
    void shouldThrowServiceExceptioinWhenDAOExceptionWhileDeleteById() {
        int testId = 5;
        doThrow(DAOException.class).when(groupDAO).deleteById(testId);
        assertThrows(ServiceException.class, () -> groupService.deleteById(testId));
    }

    @Test
    void shouldThrowServiceExceptionWhenFacultyIsNegativeWhileGetGroupsFromFaculty() {
        int testId = -2;
        assertThrows(ServiceException.class, () -> groupService.getGroupsFromFaculty(testId));
    }

    @Test
    void shouldThrowServiceExceptionWhenDAOExceptionWhileGetGroupsFromFaculty() {
        int testId = 2;
        when(groupDAO.findAll()).thenThrow(DAOException.class);
        assertThrows(ServiceException.class, () -> groupService.getGroupsFromFaculty(testId));
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

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

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

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

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

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

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

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenDAOExceptionWhileCreate() {
        Group group = new Group();
        group.setName("Group");
        Faculty faculty = new Faculty();
        faculty.setId(1);
        faculty.setName("Facutly");
        group.setFaculty(faculty);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to create a new group: " + group + ".",
                "There is some error in dao layer when create an object " + group + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        doThrow(DAOException.class).when(groupDAO).create(group);

        try {
            groupService.create(group);
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

        groupService.getAll();

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

        when(groupDAO.findAll()).thenThrow(DAOException.class);

        try {
            groupService.getAll();
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

            if (i < 2) {
                when(groupDAO.getGroupFaculty(expectedGroups.get(i).getId())).thenReturn(faculty1);
            } else {
                when(groupDAO.getGroupFaculty(expectedGroups.get(i).getId())).thenReturn(faculty2);
            }
        }

        when(groupDAO.findAll()).thenReturn(expectedGroups);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(
                Arrays.asList("Try to get all objects.", "The result is: " + expectedGroups + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        groupService.getAll();

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

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

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenDAOExceptionWhileGetById() {
        int testId = 7;

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to get an object by id: " + testId + ".",
                "There is some error in dao layer when get object by id " + testId + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        when(groupDAO.findById(testId)).thenThrow(DAOException.class);

        try {
            groupService.getById(testId);
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
        int testId = 5;

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to get an object by id: " + testId + ".",
                "The entity is not found when get object by id " + testId + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        DAOException daoException = new DAOException("The result is empty", new EmptyResultDataAccessException(1));

        when(groupDAO.findById(testId)).thenThrow(daoException);

        try {
            groupService.getById(testId);
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

        Group group = new Group();
        group.setId(testId);
        group.setName("Test name");
        Faculty faculty = new Faculty();
        faculty.setId(2);
        faculty.setName("Faculty");
        group.setFaculty(faculty);

        when(groupDAO.findById(testId)).thenReturn(group);
        when(groupDAO.getGroupFaculty(testId)).thenReturn(faculty);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to get an object by id: " + testId + ".",
                "The result object with id " + testId + " is " + group + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        groupService.getById(testId);

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

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

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

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

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

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

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenDAOExceptionWhileUpdate() {
        Group group = new Group();
        group.setId(4);
        group.setName("Group");
        Faculty faculty = new Faculty();
        faculty.setId(8);
        faculty.setName("Faculty");
        group.setFaculty(faculty);

        doThrow(DAOException.class).when(groupDAO).update(group.getId(), group);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to update a group: " + group + ".",
                "There is some error in dao layer when update an object " + group + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            groupService.update(group);
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

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

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

        doThrow(DAOException.class).when(groupDAO).deleteById(testId);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to delete an object by id: " + testId + ".",
                "There is some error in dao layer when delete an object by id " + testId + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            groupService.deleteById(testId);
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

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateеLogsWhenFacultyIdIsNegativeWhileGetGroupsFromFaculty() {
        int negativeId = -1;

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to get groups from faculty by faculty id: " + negativeId + ".",
                "A faculty id " + negativeId + " is not positive when get groups from a faculty by faculty id."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        try {
            groupService.getGroupsFromFaculty(negativeId);
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
    void shouldGenerateLogsWhenResultIsEmptyWhileGetGroupsFromFaculty() {
        int testId = 2;
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.WARN));
        List<String> expectedMessages = new ArrayList<>(
                Arrays.asList("Try to get groups from faculty by faculty id: " + testId + ".",
                        "There are not any groups from faculty with id " + testId + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        groupService.getGroupsFromFaculty(testId);

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

    @Test
    void shouldGenerateLogsWhenDAOExceptionWhileGetGroupsFromFaculty() {
        int testId = 5;

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(
                Arrays.asList("Try to get groups from faculty by faculty id: " + testId + ".",
                        "There is some error in dao layer when getGroupsFromFaculty by faculty id " + testId + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        when(groupDAO.findAll()).thenThrow(DAOException.class);

        try {
            groupService.getGroupsFromFaculty(testId);
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
    void shouldGenerateLogsWhenGetGroupsFromFaculty() {
        int facultyId = 2;
        List<Group> groups = new ArrayList<>(Arrays.asList(new Group(), new Group(), new Group()));

        Faculty faculty1 = new Faculty();
        faculty1.setId(1);
        faculty1.setName("Faculty1");

        Faculty faculty2 = new Faculty();
        faculty2.setId(2);
        faculty2.setName("Facutly2");

        groups.get(0).setFaculty(faculty1);
        groups.get(1).setFaculty(faculty1);
        groups.get(2).setFaculty(faculty2);

        for (int i = 0; i < groups.size(); i++) {
            int index = i + 1;
            groups.get(i).setId(index);
            groups.get(i).setName("Test name: " + index);

            if (i < 2) {
                when(groupDAO.getGroupFaculty(groups.get(i).getId())).thenReturn(faculty1);
            } else {
                when(groupDAO.getGroupFaculty(groups.get(i).getId())).thenReturn(faculty2);
            }
        }

        when(groupDAO.findAll()).thenReturn(groups);

        List<Group> expectedGroups = new ArrayList<>(Arrays.asList(groups.get(2)));

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(
                Arrays.asList("Try to get groups from faculty by faculty id: " + facultyId + ".",
                        "The result is: " + expectedGroups + "."));

        for (int i = 0; i < expectedLogs.size(); i++) {
            expectedLogs.get(i).setLevel(expectedLevels.get(i));
            expectedLogs.get(i).setMessage(expectedMessages.get(i));
        }

        groupService.getGroupsFromFaculty(facultyId);

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int i = 0; i < actualLogs.size(); i++) {
            assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
            assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
        }
    }

}