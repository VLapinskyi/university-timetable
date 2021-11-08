package ua.com.foxminded.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ua.com.foxminded.domain.Student;
import ua.com.foxminded.repositories.interfaces.StudentRepository;

@Service
public class StudentService {
    private StudentRepository studentRepository;

    @Autowired
    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public void create(Student student) {
        studentRepository.save(student);
    }

    public List<Student> getAll() {
        return studentRepository.findAll();
    }

    public Student getById(int studentId) {
        return studentRepository.findById(studentId).get();
    }

    public void update(Student updatedStudent) {
        studentRepository.save(updatedStudent);
    }

    public void deleteById(int studentId) {
        studentRepository.deleteById(studentId);
    }
}
