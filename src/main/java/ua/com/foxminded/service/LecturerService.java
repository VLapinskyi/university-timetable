package ua.com.foxminded.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ua.com.foxminded.dao.LecturerDAO;
import ua.com.foxminded.domain.Lecturer;

@Service
public class LecturerService {
	private LecturerDAO lecturerDAO;

	@Autowired
	public LecturerService(LecturerDAO lecturerDAO) {
		this.lecturerDAO = lecturerDAO;
	}

	public void create(Lecturer lecturer) {
		lecturerDAO.create(lecturer);
	}

	public List<Lecturer> getAll() {
		return lecturerDAO.findAll();
	}

	public Lecturer getById(int lecturerId) {
		return lecturerDAO.findById(lecturerId);
	}

	public void update(Lecturer lecturer) {
		lecturerDAO.update(lecturer.getId(), lecturer);
	}

	public void deleteById(int lecturerId) {
		lecturerDAO.deleteById(lecturerId);
	}
}
