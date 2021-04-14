package ua.com.foxminded.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ua.com.foxminded.dao.GroupDAO;
import ua.com.foxminded.dao.exceptions.DAOException;
import ua.com.foxminded.domain.Faculty;
import ua.com.foxminded.domain.Group;
import ua.com.foxminded.service.exceptions.NotValidObjectException;
import ua.com.foxminded.service.exceptions.ServiceException;
import ua.com.foxminded.settings.TestAppender;

class GroupServiceTest {
    private TestAppender testAppender = new TestAppender();
    @Spy
    @InjectMocks
    private GroupService groupService;
    @Mock
    private GroupDAO groupDAO;
    @Mock
    private FacultyService facultyService;
    @Mock
    private StudentService studentService;
    @Mock
    private LessonService lessonService;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
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
        groupService.createGroup(testGroup);
        verify(groupDAO).create(testGroup);
        verify(groupDAO).setGroupFaculty(testGroup.getFaculty().getId(), createdGroup.getId());
    }

    @Test
    void shouldGetAllGroups() {
        List<Faculty> faculties = new ArrayList<>(Arrays.asList(
                new Faculty(), new Faculty()));
        List<Integer> facultyIndexes = new ArrayList<>(Arrays.asList(1, 2));
        for (int i = 0; i < faculties.size(); i++) {
            faculties.get(i).setId(facultyIndexes.get(i));
        }

        List<Group> groups = new ArrayList<>(Arrays.asList(
                new Group(), new Group(), new Group()));
        List<Integer> groupsIndexes = new ArrayList<>(Arrays.asList(
                1, 2, 3));
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

        List<Group> actualGroups = groupService.getAllGroups();
        assertTrue(expectedGroups.containsAll(actualGroups) && actualGroups.containsAll(expectedGroups));
        verify(groupDAO).findAll();
        for(int i = 0; i < groups.size(); i++) {
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

        Group actualGroup = groupService.getGroupById(testGroupId);
        assertEquals(expectedGroup, actualGroup);
        verify(groupDAO).findById(testGroupId);
        verify(groupDAO).getGroupFaculty(testGroupId);
    }

    @Test
    void shouldUpdateGroup() {
        int facultyId = 2;
        Faculty faculty = new Faculty();
        faculty.setId(facultyId);

        int groupId = 3;
        Group group = new Group();
        group.setId(groupId);
        group.setFaculty(faculty);

        groupService.updateGroup(groupId, group);
        verify(groupDAO).update(groupId, group);
        verify(groupDAO).setGroupFaculty(facultyId, groupId);
    }

    @Test
    void shouldDeleteById() {
        int groupId = 100;
        groupService.deleteGroupById(groupId);
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
        for(int i = 0; i < groups.size(); i++) {
            verify(groupDAO).getGroupFaculty(groups.get(i).getId());
        }
    }

    @Test
    void shouldThrowNotValidObjectExceptionWhenValidateGroupIsNull() {
        Group group = null;
        assertThrows(NotValidObjectException.class, () -> groupService.validateGroup(group));
    }

    @Test
    void shouldThrowNotValidObjectExceptionWhenGroupNameIsNotValid() {
        Group group = new Group();
        assertThrows(NotValidObjectException.class, () -> groupService.validateGroup(group));
        group.setName(" T  ");
        assertThrows(NotValidObjectException.class, () -> groupService.validateGroup(group));
    }

    @Test
    void shouldThrowNotValidObjectExceptionWhenGroupFacultyHasIncorrectId() {
        Group group = new Group();
        Faculty groupFaculty = new Faculty();
        group.setFaculty(groupFaculty);

        assertThrows(NotValidObjectException.class, () -> groupService.validateGroupFaculty(group));
    }

    @Test
    void shouldGenerateLogsWhenGroupIsNull() {
        Group group = null;
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent()));
        expectedLogs.get(0).setLevel(Level.ERROR);
        expectedLogs.get(0).setMessage("The group " + group + " is null.");

        try {
            groupService.validateGroup(group);
        } catch (NotValidObjectException notValidObjectException) {
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
    void shouldGenerateLogsWhenGroupNameIsNull() {
        Group group = new Group();
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent()));
        expectedLogs.get(0).setLevel(Level.ERROR);
        expectedLogs.get(0).setMessage("The group's name " + group.getName() + " is not valid.");

        try {
            groupService.validateGroup(group);
        } catch (NotValidObjectException notValidObjectException) {
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
    void shouldGenerateLogsWhenGroupHasNotValidName() {
        Group group = new Group();
        group.setName("c ");
        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent()));
        expectedLogs.get(0).setLevel(Level.ERROR);
        expectedLogs.get(0).setMessage("The group's name " + group.getName() + " is not valid.");

        try {
            groupService.validateGroup(group);
        } catch (NotValidObjectException notValidObjectException) {
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
    void shouldGenerateLogsWhenGroupFacultyHasIncorrectId() {
        Group group = new Group();
        Faculty groupFaculty = new Faculty();
        group.setFaculty(groupFaculty);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent()));
        expectedLogs.get(0).setLevel(Level.ERROR);
        expectedLogs.get(0).setMessage("The group " + group + " has a faculty " + groupFaculty + " with incorrect id " + groupFaculty.getId() + ".");

        try {
            groupService.validateGroupFaculty(group);
        } catch (NotValidObjectException notValidObjectException) {
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
    void shouldGenerateLogsWhenCreateGroup () {
        Faculty faculty = new Faculty();
        Group testGroup = new Group();
        testGroup.setFaculty(faculty);
        Group createdGroup = new Group();
        createdGroup.setId(1);
        testGroup.setFaculty(faculty);
        doNothing().when(groupService).validateGroup(testGroup);
        doNothing().when(groupService).validateGroupFaculty(testGroup);
        when(groupDAO.findAll()).thenReturn(Arrays.asList(createdGroup));

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.DEBUG));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to create new group: " + testGroup + ".",
                "The group " + testGroup + " was created."));

        for (int a = 0; a < expectedLogs.size(); a++) {
            expectedLogs.get(a).setLevel(expectedLevels.get(a));
            expectedLogs.get(a).setMessage(expectedMessages.get(a));
        }

        groupService.createGroup(testGroup);

        List<ILoggingEvent> actualLogs = testAppender.getEvents();

        assertEquals(expectedLogs.size(), actualLogs.size());
        for (int a = 0; a < actualLogs.size(); a++) {
            assertEquals(expectedLogs.get(a).getLevel(), actualLogs.get(a).getLevel());
            assertEquals(expectedLogs.get(a).getFormattedMessage(), actualLogs.get(a).getFormattedMessage());
        }    
    }

    @Test
    void shouldGenerateLogsWhenCreateGroupIdIsNotZero() {
        Group group = new Group();
        doNothing().when(groupService).validateGroup(group);
        doNothing().when(groupService).validateGroupFaculty(group);
        int[] groupIdNumbers = new int [] {-4, 7, 10, -1000, -1, 1};
        for (int i = 0; i < groupIdNumbers.length; i++) {
            group.setId(groupIdNumbers[i]);
            List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                    new LoggingEvent(), new LoggingEvent()));
            List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                    Level.DEBUG, Level.ERROR));
            List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                    "Try to create new group: " + group + ".",
                    "The group " + group + " has setted id " + group.getId() + " and it is different from zero."));

            for (int a = 0; a < expectedLogs.size(); a++) {
                expectedLogs.get(a).setLevel(expectedLevels.get(a));
                expectedLogs.get(a).setMessage(expectedMessages.get(a));
            }

            try {
                groupService.createGroup(group);
            } catch (ServiceException serviceException) {
                //do nothing
            }

            List<ILoggingEvent> actualLogs = testAppender.getEvents();

            assertEquals(expectedLogs.size(), actualLogs.size());
            for (int a = 0; a < actualLogs.size(); a++) {
                assertEquals(expectedLogs.get(a).getLevel(), actualLogs.get(a).getLevel());
                assertEquals(expectedLogs.get(a).getFormattedMessage(), actualLogs.get(a).getFormattedMessage());
            }    
            testAppender.cleanEventList();
        }
    }

    @Test
    void shouldGenerateLogsWhenThrowDAOExceptionWhileCreateGroup() {
        Group group = new Group();
        doNothing().when(groupService).validateGroup(group);
        doNothing().when(groupService).validateGroupFaculty(group);
        when(groupDAO.findAll()).thenThrow(DAOException.class);

        List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(
                new LoggingEvent(), new LoggingEvent()));
        List<Level> expectedLevels = new ArrayList<>(Arrays.asList(
                Level.DEBUG, Level.ERROR));
        List<String> expectedMessages = new ArrayList<>(Arrays.asList(
                "Try to create new group: " + group + ".",
                "Can't create group " + group + "."));

        for (int a = 0; a < expectedLogs.size(); a++) {
            expectedLogs.get(a).setLevel(expectedLevels.get(a));
            expectedLogs.get(a).setMessage(expectedMessages.get(a));
        }

        try {
            groupService.createGroup(group);
        } catch (ServiceException serviceException) {
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