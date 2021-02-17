package ua.com.foxminded.service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ua.com.foxminded.dao.StudentDAO;
import ua.com.foxminded.domain.Student;

@Service
public class StudentService {
    private StudentDAO studentDAO;
    private GroupService groupService;

    @Autowired
    public StudentService (StudentDAO studentDAO, GroupService groupService) {
        this.studentDAO = studentDAO;
        this.groupService = groupService;
    }

    public void createStudent(int groupId, Student student) {
        studentDAO.create(student);
        Optional<Student> createdStudent = studentDAO.findAll().stream().max(Comparator.comparing(Student :: getId));
        int studentId = 0;
        if (createdStudent.isPresent()) {
            studentId = createdStudent.get().getId();
        }
        studentDAO.setStudentGroup(groupId, studentId);
    }
    
    public List<Student> getAllStudents() {
        List<Student> students = studentDAO.findAll();
        students.stream().forEach(student -> student.setGroup(studentDAO.getStudentGroup(student.getId())));
        students.stream().forEach(student -> student.setGroup(groupService.getGroupById(student.getGroup().getId())));
        return students;
    }
    
    public Student getStudentById(int studentId) {
        Student student = studentDAO.findById(studentId);
        student.setGroup(studentDAO.getStudentGroup(studentId));
        student.setGroup(groupService.getGroupById(student.getGroup().getId()));
        return student;
    }
    
    public void updateStudent(int studentId, Student updatedStudent) {
        studentDAO.update(studentId, updatedStudent);
        studentDAO.setStudentGroup(updatedStudent.getGroup().getId(), studentId);
    }
    
    public void deleteStudentById(int studentId) {
        studentDAO.deleteById(studentId);
    }
}
