package ua.com.foxminded.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ua.com.foxminded.domain.Lecturer;
import ua.com.foxminded.repositories.interfaces.LecturerRepository;

@Service
public class LecturerService {
    private LecturerRepository lecturerRepository;

    @Autowired
    public LecturerService(LecturerRepository lecturerRepository) {
        this.lecturerRepository = lecturerRepository;
    }

    public void create(Lecturer lecturer) {
        lecturerRepository.save(lecturer);
    }

    public List<Lecturer> getAll() {
        return lecturerRepository.findAll();
    }

    public Lecturer getById(int lecturerId) {
        return lecturerRepository.findById(lecturerId).get();
    }

    public void update(Lecturer lecturer) {
        lecturerRepository.save(lecturer);
    }

    public void deleteById(int lecturerId) {
        lecturerRepository.deleteById(lecturerId);
    }
}
