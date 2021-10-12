package ua.com.foxminded.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ua.com.foxminded.domain.Student;
import ua.com.foxminded.repositories.StudentRepository;

@Service
public class StudentService {
    private StudentRepository studentRepository;

    @Autowired
    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public void create(Student student) {
        studentRepository.create(student);
    }

    public List<Student> getAll() {
        return studentRepository.findAll();
    }

    public Student getById(int studentId) {
        return studentRepository.findById(studentId);
    }

    public void update(Student updatedStudent) {
        studentRepository.update(updatedStudent);
    }

    public void deleteById(int studentId) {
        Student student = studentRepository.findById(studentId);
        studentRepository.delete(student);
    }
}
