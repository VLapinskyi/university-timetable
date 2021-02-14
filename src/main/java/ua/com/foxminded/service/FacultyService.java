package ua.com.foxminded.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ua.com.foxminded.dao.FacultyDAO;
import ua.com.foxminded.dao.GroupDAO;
import ua.com.foxminded.dao.LecturerDAO;
import ua.com.foxminded.dao.LessonDAO;
import ua.com.foxminded.dao.LessonTimeDAO;
import ua.com.foxminded.dao.StudentDAO;
import ua.com.foxminded.domain.Faculty;

@Service
public class FacultyService {
    private FacultyDAO facultyDAO;
    private GroupDAO groupDAO;


    
    @Autowired
    public FacultyService (FacultyDAO facultyDAO, GroupDAO groupDAO) {
	this.facultyDAO = facultyDAO;
	this.groupDAO = groupDAO;
    }
    
    public void createFaculty(String name) {
	Faculty faculty = new Faculty();
	faculty.setName(name);
	facultyDAO.create(faculty);
    }
    
    public List<Faculty> findAllFaculties() {
	return facultyDAO.findAll();
    }
    
    public Faculty findFacultyById(int facultyId) {
	return facultyDAO.findById(facultyId);
    }
    
    public void updateFaculty (int facultyId, String newFacultyName) {
	Faculty faculty = new Faculty();
	faculty.setName(newFacultyName);
	facultyDAO.update(facultyId, faculty);
    }
    
    public void deleteFacultyById (int facultyId) {
        facultyDAO.deleteById(facultyId);
    }
}
