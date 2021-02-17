package ua.com.foxminded.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ua.com.foxminded.dao.StudentDAO;
import ua.com.foxminded.domain.Gender;
import ua.com.foxminded.domain.Group;
import ua.com.foxminded.domain.Student;

class StudentServiceTest {
    @InjectMocks
    private StudentService studentService;

    @Mock
    private StudentDAO studentDAO;
    @Mock
    private GroupService groupService;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldCreateStudent() {
        Student savedStudent = new Student();
        savedStudent.setId(1);  
        int groupId = 1;
        Student creatingStudent = new Student();
        creatingStudent.setId(2);
        when(studentDAO.findAll()).thenReturn(new ArrayList<Student>(Arrays.asList(savedStudent, creatingStudent)));
        studentService.createStudent(groupId, creatingStudent);
        verify(studentDAO).create(creatingStudent);
        verify(studentDAO).setStudentGroup(groupId, creatingStudent.getId());
    }

    @Test
    void shouldGetAllStudents() {
        List<Group> groups = new ArrayList<>(Arrays.asList(
                new Group(), new Group()));
        List<Integer> groupIndexes = new ArrayList<>(Arrays.asList(
                1, 2));
        for (int i = 0; i < groups.size(); i++) {
            groups.get(i).setId(groupIndexes.get(i));
        }

        List<Student> students = new ArrayList<>(Arrays.asList(
                new Student(), new Student(), new Student()));
        List<Integer> studentIndexes = new ArrayList<>(Arrays.asList(
                1, 2, 3));
        for (int i = 0; i < students.size(); i++) {
            students.get(i).setId(studentIndexes.get(i));
        }

        when(studentDAO.findAll()).thenReturn(students);
        when(studentDAO.getStudentGroup(students.get(0).getId())).thenReturn(groups.get(0));
        when(studentDAO.getStudentGroup(students.get(1).getId())).thenReturn(groups.get(0));
        when(studentDAO.getStudentGroup(students.get(2).getId())).thenReturn(groups.get(1));
        
        studentService.getAllStudents();
        verify(studentDAO).findAll();
        verify(studentDAO).getStudentGroup(students.get(0).getId());
        verify(studentDAO).getStudentGroup(students.get(1).getId());
        verify(studentDAO).getStudentGroup(students.get(2).getId());
        verify(groupService, times(2)).getGroupById(groups.get(0).getId());
        verify(groupService).getGroupById(groups.get(1).getId());
    }
    
    @Test
    void shouldGetStudentById() {
	int groupId = 2;
        Group group = new Group();
        group.setId(groupId);
        
        int studentId = 1;
        Student student = new Student();
        student.setId(studentId);
        
        when(studentDAO.findById(studentId)).thenReturn(student);
        when(studentDAO.getStudentGroup(studentId)).thenReturn(group);
        
        studentService.getStudentById(studentId);
        verify(studentDAO).findById(studentId);
        verify(studentDAO).getStudentGroup(studentId);
        verify(groupService).getGroupById(groupId);
    }
    
    @Test
    void shouldUpdateStudent() {
	int groupId = 1;
	Group group = new Group();
	group.setId(groupId);
        int studentId = 2;
        Student student = new Student();
        student.setFirstName("Ivan");
        student.setLastName("Zakharchuk");
        student.setGender(Gender.MALE);
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
}
