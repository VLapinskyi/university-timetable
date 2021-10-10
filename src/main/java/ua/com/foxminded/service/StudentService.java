package ua.com.foxminded.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ua.com.foxminded.dao.StudentDAO;
import ua.com.foxminded.domain.Student;

@Service
public class StudentService {
    private StudentDAO studentDAO;

    @Autowired
    public StudentService(StudentDAO studentDAO) {
        this.studentDAO = studentDAO;
    }

    public void create(Student student) {
        studentDAO.create(student);
    }

    public List<Student> getAll() {
        return studentDAO.findAll();
    }

    public Student getById(int studentId) {
        return studentDAO.findById(studentId);
    }

    public void update(Student updatedStudent) {
        studentDAO.update(updatedStudent);
    }

    public void deleteById(int studentId) {
        Student student = studentDAO.findById(studentId);
        studentDAO.delete(student);
    }
}
