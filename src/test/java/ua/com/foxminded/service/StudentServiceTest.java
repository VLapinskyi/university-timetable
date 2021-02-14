package ua.com.foxminded.service;

import static org.junit.jupiter.api.Assertions.*;
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

import ua.com.foxminded.dao.GroupDAO;
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
    private GroupDAO groupDAO;

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
        List<String> groupNames = new ArrayList<>(Arrays.asList(
                "Group1", "Group2"));
        for (int i = 0; i < groups.size(); i++) {
            groups.get(i).setId(groupIndexes.get(i));
            groups.get(i).setName(groupNames.get(i));
        }

        List<Student> testStudents = new ArrayList<>(Arrays.asList(
                new Student(), new Student(), new Student()));
        List<Integer> studentIndexes = new ArrayList<>(Arrays.asList(
                1, 2, 3));
        List<String> firstNames = new ArrayList<>(Arrays.asList(
                "Valentyn", "Ivan", "Olha"));
        List<String> lastNames = new ArrayList<>(Arrays.asList(
                "Lapinskyi", "Zakharchuk", "Skladenko"));
        List<Gender> studentGenders = new ArrayList<>(Arrays.asList(
                Gender.MALE, Gender.MALE, Gender.FEMALE));
        String phoneNumber = "+380445237954";
        String email =  "test@gmail.com";
        for (int i = 0; i < testStudents.size(); i++) {
            testStudents.get(i).setId(studentIndexes.get(i));
            testStudents.get(i).setFirstName(firstNames.get(i));
            testStudents.get(i).setLastName(lastNames.get(i));
            testStudents.get(i).setGender(studentGenders.get(i));
        }
        testStudents.get(0).setPhoneNumber(phoneNumber);
        testStudents.get(1).setEmail(email);

        when(studentDAO.findAll()).thenReturn(testStudents);
        when(studentDAO.getStudentGroup(testStudents.get(0).getId())).thenReturn(groups.get(0));
        when(studentDAO.getStudentGroup(testStudents.get(1).getId())).thenReturn(groups.get(0));
        when(studentDAO.getStudentGroup(testStudents.get(2).getId())).thenReturn(groups.get(1));
        
        List<Student> expectedStudents = new ArrayList<>(Arrays.asList(
                new Student(), new Student(), new Student()));
        for(int i = 0; i < expectedStudents.size(); i++) {
            expectedStudents.get(i).setId(studentIndexes.get(i));
            expectedStudents.get(i).setFirstName(firstNames.get(i));
            expectedStudents.get(i).setLastName(lastNames.get(i));
            expectedStudents.get(i).setGender(studentGenders.get(i));
        }
        expectedStudents.get(0).setPhoneNumber(phoneNumber);
        expectedStudents.get(0).setGroup(groups.get(0));
        expectedStudents.get(1).setEmail(email);
        expectedStudents.get(1).setGroup(groups.get(0));
        expectedStudents.get(2).setGroup(groups.get(1));
        
        List<Student> actualStudents = studentService.getAllStudents();
        assertTrue(expectedStudents.containsAll(actualStudents) && actualStudents.containsAll(expectedStudents));
    }
    
    @Test
    void shouldGetStudentById() {
        Group group = new Group();
        group.setId(1);
        group.setName("Group1");
        
        int studentId = 1;
        String firstName = "Valentyn";
        String lastName = "Lapinskyi";
        Gender gender = Gender.MALE;
        String email = "valentinlapinskiy@gmail.com";
        
        Student testStudent = new Student();
        testStudent.setId(studentId);
        testStudent.setFirstName(firstName);
        testStudent.setLastName(lastName);
        testStudent.setGender(gender);
        testStudent.setEmail(email);
        
        Student expectedStudent = new Student();
        expectedStudent.setId(studentId);
        expectedStudent.setFirstName(firstName);
        expectedStudent.setLastName(lastName);
        expectedStudent.setGender(gender);
        expectedStudent.setEmail(email);
        expectedStudent.setGroup(group);
        
        when(studentDAO.findById(studentId)).thenReturn(testStudent);
        when(studentDAO.getStudentGroup(studentId)).thenReturn(group);
        
        Student actualStudent = studentService.getStudentById(studentId);
        assertEquals(expectedStudent, actualStudent);
    }
    
    @Test
    void shouldUpdateStudent() {
        int testId = 2;
        Student student = new Student();
        student.setFirstName("Ivan");
        student.setLastName("Zakharchuk");
        student.setGender(Gender.MALE);
        studentService.changePersonalStudentData(testId, student);
        verify(studentDAO).update(testId, student);
    }
    
    @Test
    void shouldDeleteStudentById() {
        int testId = 1;
        studentService.deleteStudentById(testId);
        verify(studentDAO).deleteById(testId);
    }
    
    @Test
    void shouldSetGroupForStudent() {
        int groupId = 3;
        int studentId = 1;
        studentService.changeStudentGroup(groupId, studentId);
        verify(studentDAO).setStudentGroup(groupId, studentId);
    }
}
