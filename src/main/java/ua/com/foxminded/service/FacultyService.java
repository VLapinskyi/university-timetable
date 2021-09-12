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
	public FacultyService(FacultyDAO facultyDAO) {
		this.facultyDAO = facultyDAO;
	}

	public void create(Faculty faculty) {
		facultyDAO.create(faculty);
	}

	public List<Faculty> getAll() {
		return facultyDAO.findAll();
	}

	public Faculty getById(int facultyId) {
		return facultyDAO.findById(facultyId);
	}

	public void update(Faculty updatedFaculty) {
		facultyDAO.update(updatedFaculty.getId(), updatedFaculty);
	}

	public void deleteById(int facultyId) {
		facultyDAO.deleteById(facultyId);
	}
}