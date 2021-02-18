package ua.com.foxminded.service;

import static org.mockito.Mockito.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ua.com.foxminded.dao.FacultyDAO;
import ua.com.foxminded.domain.Faculty;
import ua.com.foxminded.domain.Group;

class FacultyServiceTest {
    @InjectMocks
    private FacultyService facultyService;

    @Mock
    private FacultyDAO facultyDAO;
    @Mock
    private GroupService groupService;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldCreateFaculty() {
        String facultyName = "TestName";
        Faculty faculty = new Faculty();
        faculty.setName(facultyName);
        facultyService.createFaculty(faculty);
        verify(facultyDAO).create(faculty);
    }

    @Test
    void shouldGetAllFaculties() {
        List<Faculty> faculties = new ArrayList<>(Arrays.asList(
                new Faculty(), new Faculty()));
        faculties.get(0).setId(1);
        faculties.get(0).setId(2);
        
        List<Group> groups = new ArrayList<>(Arrays.asList(
                new Group(), new Group(), new Group()));
        groups.get(0).setId(1);
        groups.get(1).setId(2);
        groups.get(2).setId(3);
        groups.get(0).setFaculty(faculties.get(0));
        groups.get(1).setFaculty(faculties.get(0));
        groups.get(2).setFaculty(faculties.get(1));
        
        List<Faculty> expectedFaculties = new ArrayList<>(faculties);
        expectedFaculties.get(0).setGroups(groups.subList(0, 2));
        expectedFaculties.get(1).setGroups(groups.subList(2, groups.size()));
        
        when(facultyDAO.findAll()).thenReturn(faculties);
        when(groupService.getAllGroups()).thenReturn(groups);
        List<Faculty> actualList = facultyService.getAllFaculties();
        assertTrue(expectedFaculties.containsAll(actualList) && actualList.containsAll(expectedFaculties));
        verify(facultyDAO).findAll();
        verify(groupService).getAllGroups();
    }

    @Test
    void shouldFindFacultyById() {
        int testId = 2;
        Faculty faculty = new Faculty();
        faculty.setId(testId);
        List<Group> groups = new ArrayList<>(Arrays.asList(
                new Group(), new Group()));
        groups.get(0).setId(1);
        groups.get(0).setId(1);
        groups.stream().forEach(group -> group.setFaculty(faculty));
        
        Faculty expectedFaculty = new Faculty();
        expectedFaculty.setId(testId);
        expectedFaculty.setGroups(groups);
        
        when(facultyDAO.findById(testId)).thenReturn(faculty);
        when(groupService.getAllGroups()).thenReturn(groups);
        Faculty actualFaculty = facultyService.getFacultyById(testId);
        assertEquals(expectedFaculty, actualFaculty);
        verify(facultyDAO).findById(testId);
        verify(groupService).getAllGroups();
    }

    @Test
    void shouldUpdateFaculty() {
        int testId = 3;
        String testName = "Faculty Name";
        Faculty testFaculty = new Faculty();
        testFaculty.setName(testName);
        facultyService.updateFaculty(testId, testFaculty);
        verify(facultyDAO).update(testId, testFaculty);
    }

    @Test
    void shouldDeleteFacultyById() {
        int testId = 1;
        facultyService.deleteFacultyById(testId);
        verify(facultyDAO).deleteById(testId);
    }
}
