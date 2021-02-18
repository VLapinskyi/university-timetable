package ua.com.foxminded.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ua.com.foxminded.dao.StudentDAO;
import ua.com.foxminded.domain.Group;
import ua.com.foxminded.domain.Student;

class StudentServiceTest {
    @InjectMocks
    private StudentService studentService;

    @Mock
    private StudentDAO studentDAO;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldCreateStudent() {
        int groupId = 1;
        Student savedStudent = new Student();
        savedStudent.setId(1);
        Student creatingStudent = new Student();
        creatingStudent.setId(2);
        when(studentDAO.findAll()).thenReturn(new ArrayList<Student>(Arrays.asList(savedStudent, creatingStudent)));
        studentService.createStudent(groupId, creatingStudent);
        verify(studentDAO).create(creatingStudent);
        verify(studentDAO).setStudentGroup(groupId, creatingStudent.getId());
    }

    @Test
    void shouldGetAllStudents() {
        List<Student> students = new ArrayList<>(Arrays.asList(
                new Student(), new Student(), new Student()));
        List<Integer> studentIndexes = new ArrayList<>(Arrays.asList(
                1, 2, 3));
        for (int i = 0; i < students.size(); i++) {
            students.get(i).setId(studentIndexes.get(i));
        }

        when(studentDAO.findAll()).thenReturn(students);

        studentService.getAllStudents();
        verify(studentDAO).findAll();
        for(int i = 0; i < students.size(); i++) {
            verify(studentDAO).getStudentGroup(students.get(i).getId());
        }
    }

    @Test
    void shouldGetStudentById() {
        int studentId = 1;
        Student student = new Student();
        student.setId(studentId);

        when(studentDAO.findById(studentId)).thenReturn(student);

        studentService.getStudentById(studentId);
        verify(studentDAO).findById(studentId);
        verify(studentDAO).getStudentGroup(studentId);
    }

    @Test
    void shouldUpdateStudent() {
        int groupId = 1;
        Group group = new Group();
        group.setId(groupId);
        int studentId = 2;
        Student student = new Student();
        student.setGroup(group);
        studentService.updateStudent(studentId, student);
        verify(studentDAO).update(studentId, student);
        verify(studentDAO).setStudentGroup(student.getGroup().getId(), studentId);
    }

    @Test
    void shouldDeleteStudentById() {
        int testId = 1;
        studentService.deleteStudentById(testId);
        verify(studentDAO).deleteById(testId);
    }

    @Test
    void shouldGetStudentsFromGroup() {
        int groupId = 3;
        List<Group> groups = new ArrayList<>(Arrays.asList(new Group(), new Group(), new Group()));
        groups.get(0).setId(1);
        groups.get(1).setId(2);
        groups.get(2).setId(3);

        List<Student> students = new ArrayList<>(Arrays.asList(new Student(), new Student(), new Student()));
        students.get(0).setId(1);
        students.get(1).setId(2);
        students.get(2).setId(3);

        List<Student> expectedStudents = new ArrayList<>(students.subList(0, 2));
        expectedStudents.stream().forEach(student -> student.setGroup(groups.get(2)));

        when(studentDAO.findAll()).thenReturn(students);
        when(studentDAO.getStudentGroup(students.get(0).getId())).thenReturn(groups.get(2));
        when(studentDAO.getStudentGroup(students.get(1).getId())).thenReturn(groups.get(2));
        when(studentDAO.getStudentGroup(students.get(2).getId())).thenReturn(groups.get(1));
        List<Student> actualStudents = studentService.getStudentsFromGroup(groupId);

        assertTrue(expectedStudents.containsAll(actualStudents) && actualStudents.containsAll(expectedStudents));
        verify(studentDAO).findAll();
        for(int i = 0; i < students.size(); i++) {
            verify(studentDAO).getStudentGroup(students.get(i).getId());
        }

    }
}
