package ua.com.foxminded.service;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ua.com.foxminded.dao.LecturerDAO;
import ua.com.foxminded.domain.Lecturer;

class LecturerServiceTest {
    @InjectMocks
    private LecturerService lecturerService;

    @Mock
    private LecturerDAO lecturerDAO;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldCreateLecturer() {
        Lecturer lecturer = new Lecturer();
        lecturer.setFirstName("Valentyn");
        lecturer.setLastName("Lapinskyi");
        lecturerService.createLecturer(lecturer);
        verify(lecturerDAO).create(lecturer);
    }

    @Test
    void shouldGetAllLecturers() {
        lecturerService.getAllLecturers();
        verify(lecturerDAO).findAll();
    }

    @Test
    void shouldGetLecturerById() {
        int lecturerId = 2;

        lecturerService.getLecturerById(lecturerId);
        verify(lecturerDAO).findById(lecturerId);
    }

    @Test
    void shouldUpdateLecturer() {
        int lecturerId = 3;
        Lecturer lecturer = new Lecturer();
        lecturer.setFirstName("Valentyn");
        lecturer.setLastName("Lapinskyi");
        lecturerService.updateLecturer(lecturerId, lecturer);
        verify(lecturerDAO).update(lecturerId, lecturer);
    }

    @Test
    void shouldDeleteLecturerById() {
        int testLecturerId = 5;
        lecturerService.deleteLecturerById(testLecturerId);
        verify(lecturerDAO).deleteById(testLecturerId);
    }
}
