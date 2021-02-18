package ua.com.foxminded.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ua.com.foxminded.dao.GroupDAO;
import ua.com.foxminded.domain.Faculty;
import ua.com.foxminded.domain.Group;

class GroupServiceTest {
    @InjectMocks
    private GroupService groupService;
    @Mock
    private GroupDAO groupDAO;
    @Mock
    private StudentService studentService;
    @Mock
    private LessonService lessonService;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldCreateGroup() {
        int facultyId = 2;
        Group savedGroup = new Group();
        savedGroup.setId(3);
        Group creatingGroup = new Group();
        creatingGroup.setId(4);
        when(groupDAO.findAll()).thenReturn(new ArrayList<Group>(Arrays.asList(savedGroup, creatingGroup)));
        groupService.createGroup(facultyId, creatingGroup);
        verify(groupDAO).create(creatingGroup);
        verify(groupDAO).setGroupFaculty(facultyId, creatingGroup.getId());
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
}