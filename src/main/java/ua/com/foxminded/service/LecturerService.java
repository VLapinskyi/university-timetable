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

    public void createLecturer(Lecturer lecturer) {
        lecturerDAO.create(lecturer);
    }

    public List<Lecturer> getAllLecturers() {
        return lecturerDAO.findAll();
    }

    public Lecturer getLecturerById (int lecturerId) {
        return lecturerDAO.findById(lecturerId);
    }

    public void updateLecturer(int lecturerId, Lecturer lecturer) {
        lecturerDAO.update(lecturerId, lecturer);
    }

    public void deleteLecturerById(int lecturerId) {
        lecturerDAO.deleteById(lecturerId);
    }
}
