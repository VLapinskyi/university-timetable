package ua.com.foxminded.service;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ua.com.foxminded.dao.FacultyDAO;
import ua.com.foxminded.domain.Faculty;

class FacultyServiceTest {
    @InjectMocks
    private FacultyService facultyService;

    @Mock
    private FacultyDAO facultyDAO;

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
        facultyService.getAllFaculties();
        verify(facultyDAO).findAll();
    }

    @Test
    void shouldFindFacultyById() {
        int facultyId = 5;
        facultyService.getFacultyById(facultyId);
        verify(facultyDAO).findById(facultyId);
    }

    @Test
    void shouldUpdateFaculty() {
        int facultyId = 3;
        String testName = "Faculty Name";
        Faculty faculty = new Faculty();
        faculty.setName(testName);
        facultyService.updateFaculty(facultyId, faculty);
        verify(facultyDAO).update(facultyId, faculty);
    }

    @Test
    void shouldDeleteFacultyById() {
        int facultyId = 1;
        facultyService.deleteFacultyById(facultyId);
        verify(facultyDAO).deleteById(facultyId);
    }
}
