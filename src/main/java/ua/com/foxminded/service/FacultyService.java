package ua.com.foxminded.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ua.com.foxminded.dao.FacultyDAO;
import ua.com.foxminded.domain.Faculty;

@Service
public class FacultyService {
    private FacultyDAO facultyDAO;

    @Autowired
    public FacultyService (FacultyDAO facultyDAO) {
        this.facultyDAO = facultyDAO;
    }

    public void createFaculty(Faculty faculty) {
        facultyDAO.create(faculty);
    }

    public List<Faculty> getAllFaculties() {
        return facultyDAO.findAll();
    }

    public Faculty getFacultyById(int facultyId) {
        return facultyDAO.findById(facultyId);
    }

    public void updateFaculty (int facultyId, Faculty updatedFaculty) {
        facultyDAO.update(facultyId, updatedFaculty);
    }

    public void deleteFacultyById (int facultyId) {
        facultyDAO.deleteById(facultyId);
    }
}
