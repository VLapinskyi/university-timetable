package ua.com.foxminded.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ua.com.foxminded.domain.Faculty;
import ua.com.foxminded.repositories.FacultyRepository;

@Service
public class FacultyService {
    private FacultyRepository facultyRepository;

    @Autowired
    public FacultyService(FacultyRepository facultyRepository) {
        this.facultyRepository = facultyRepository;
    }

    public void create(Faculty faculty) {
        facultyRepository.create(faculty);
    }

    public List<Faculty> getAll() {
        return facultyRepository.findAll();
    }

    public Faculty getById(int facultyId) {
        return facultyRepository.findById(facultyId);
    }

    public void update(Faculty updatedFaculty) {
        facultyRepository.update(updatedFaculty);
    }

    public void deleteById(int facultyId) {
        Faculty faculty = facultyRepository.findById(facultyId);
        facultyRepository.delete(faculty);
    }
}