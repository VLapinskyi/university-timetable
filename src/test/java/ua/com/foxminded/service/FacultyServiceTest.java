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
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ua.com.foxminded.dao.FacultyDAO;
import ua.com.foxminded.dao.GroupDAO;
import ua.com.foxminded.domain.Faculty;

class FacultyServiceTest {
    @InjectMocks
    private FacultyService universityService;

    @Mock
    private FacultyDAO facultyDAO;
    @Mock
    private GroupDAO groupDAO;
    
    @Captor
    ArgumentCaptor<Faculty> facultyCaptor;
    @Captor
    ArgumentCaptor<Integer> numberCaptor;

    @BeforeEach
    void init() {
	MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldCreateFaculty() {
	String facultyName = "TestName";
	universityService.createFaculty(facultyName);
	verify(facultyDAO).create(facultyCaptor.capture());
	Faculty actualFaculty = facultyCaptor.getValue();
	assertEquals(facultyName, actualFaculty.getName());
    }

    @Test
    void shouldFindAllFaculties() {
	Faculty faculty1 = new Faculty();
	Faculty faculty2 = new Faculty();
	faculty1.setId(1);
	faculty1.setName("Faculty1");
	faculty2.setId(2);
	faculty2.setName("Faculty2");
	List<Faculty> facultyList = new ArrayList<>(Arrays.asList(faculty1, faculty2));
	when(facultyDAO.findAll()).thenReturn(facultyList);
	List<Faculty> actualList = universityService.findAllFaculties();
	assertTrue(facultyList.containsAll(actualList) && actualList.containsAll(facultyList));
    }
    
    @Test
    void shouldFindFacultyById() {
	int testId = 2;
	Faculty faculty = new Faculty();
	faculty.setId(testId);
	faculty.setName("TestFaculty");
	when(facultyDAO.findById(testId)).thenReturn(faculty);
	assertEquals(faculty, universityService.findFacultyById(testId));
    }
    
    @Test
    void shouldUpdateFaculty() {
	int testId = 3;
	String testName = "Faculty Name";
	Faculty testFaculty = new Faculty();
	testFaculty.setName(testName);
	universityService.updateFaculty(testId, testName);
	verify(facultyDAO).update(numberCaptor.capture(), facultyCaptor.capture());
	assertTrue(testId == numberCaptor.getValue() && testFaculty.equals(facultyCaptor.getValue()));
    }
    
    @Test
    void shouldDeleteFacultyById() {
        int testId = 1;
        universityService.deleteFacultyById(testId);
        verify(facultyDAO).deleteById(numberCaptor.capture());
        assertTrue(testId == numberCaptor.getValue());
    }
}
