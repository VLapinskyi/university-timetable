package ua.com.foxminded.dao;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import ua.com.foxminded.domain.Gender;
import ua.com.foxminded.domain.Group;
import ua.com.foxminded.domain.Student;
import ua.com.foxminded.settings.SpringTestConfiguration;
@ContextConfiguration(classes = {SpringTestConfiguration.class})
@ExtendWith(SpringExtension.class)
class StudentDAOTest {
    @Autowired
    private StudentDAO studentDAO;
    @Autowired
    JdbcTemplate jdbcTemplate;
    private ArrayList<Student> expectedStudents;
    private Connection connection;
    
    private final ClassPathResource testData = new ClassPathResource("/Test data.sql");
    private final ClassPathResource testTablesCreator = new ClassPathResource("/Creating tables.sql");
    private final ClassPathResource testDatabaseCleaner = new ClassPathResource("/Clearing database.sql");
    
    @BeforeEach
    void setUp() throws Exception {
        connection = jdbcTemplate.getDataSource().getConnection();
        ScriptUtils.executeSqlScript(connection, testTablesCreator);
        
        expectedStudents = new ArrayList<>(Arrays.asList(
                new Student(), new Student(), new Student()));
        ArrayList<Integer> studentIndexes = new ArrayList<>(Arrays.asList(
                4, 5, 6));
        ArrayList<String> firstNames = new ArrayList<>(Arrays.asList(
                "Daria", "Illia", "Mykhailo"));
        ArrayList<String> lastNames = new ArrayList<>(Arrays.asList(
                "Hrynchuk", "Misiats", "Mazur"));
        ArrayList<Gender> gendersForLecturers = new ArrayList<>(Arrays.asList(
                Gender.FEMALE, Gender.MALE, Gender.MALE));
        ArrayList<String> phoneNumbers = new ArrayList<>(Arrays.asList(
                "+380992222222", null, null));
        ArrayList<String> emails = new ArrayList<>(Arrays.asList(
                "d.hrynchuk@gmail.com", "illiamisiats@gmail.com", null));
        for (int i = 0; i < expectedStudents.size(); i++) {
            expectedStudents.get(i).setId(studentIndexes.get(i));
            expectedStudents.get(i).setFirstName(firstNames.get(i));
            expectedStudents.get(i).setLastName(lastNames.get(i));
            expectedStudents.get(i).setGender(gendersForLecturers.get(i));
            expectedStudents.get(i).setPhoneNumber(phoneNumbers.get(i));
            expectedStudents.get(i).setEmail(emails.get(i));
        }        
    }

    @AfterEach
    void tearDown() throws Exception {
        ScriptUtils.executeSqlScript(connection, testDatabaseCleaner);
    }

    @Test
    void shouldCreateStudent() {
        Student expectedStudent = new Student();
        expectedStudent.setId(1);
        expectedStudent.setFirstName("First-name");
        expectedStudent.setLastName("Last-name");
        expectedStudent.setGender(Gender.MALE);
        expectedStudent.setPhoneNumber("1233");
        
        Student testStudent = new Student();
        testStudent.setFirstName("First-name");
        testStudent.setLastName("Last-name");
        testStudent.setGender(Gender.MALE);
        testStudent.setPhoneNumber("1233");    
        studentDAO.create(testStudent);
        
        Student actualStudent = studentDAO.findAll().stream().findFirst().get();
        assertEquals(expectedStudent, actualStudent);
    }

    @Test
    void shouldFindAllStudents() {
        ScriptUtils.executeSqlScript(connection, testData);
        ArrayList<Student> actualStudents = (ArrayList<Student>) studentDAO.findAll();
        assertTrue(expectedStudents.containsAll(actualStudents) && actualStudents.containsAll(expectedStudents));
    }

    @Test
    void shouldFindStudentById() {
        ScriptUtils.executeSqlScript(connection, testData);
        int checkedId = 5;
        Student expectedStudent = new Student();
        expectedStudent.setId(checkedId);
        expectedStudent.setFirstName("Illia");
        expectedStudent.setLastName("Misiats");
        expectedStudent.setGender(Gender.MALE);
        expectedStudent.setEmail("illiamisiats@gmail.com");
        assertEquals(expectedStudent, studentDAO.findById(checkedId));
    }

    @Test
    void shouldUpdateStudent() {
        ScriptUtils.executeSqlScript(connection, testData);
        int testId = 5;
        Student testStudent = new Student();
        testStudent.setFirstName("Tetiana");
        testStudent.setLastName("Lytvynenko");
        testStudent.setGender(Gender.FEMALE);
        
        Student expectedStudent = new Student();
        expectedStudent.setId(testId);
        expectedStudent.setFirstName("Tetiana");
        expectedStudent.setLastName("Lytvynenko");
        expectedStudent.setGender(Gender.FEMALE);  
        studentDAO.update(testId, testStudent);
        assertEquals(expectedStudent, studentDAO.findById(testId));
    }

    @Test
    void shouldDeleteStudentById() {
        ScriptUtils.executeSqlScript(connection, testData);
        int deletedId = 5;
        for (int i = 0; i < expectedStudents.size(); i++) {
            if (expectedStudents.get(i).getId() == deletedId) {
                expectedStudents.remove(i);
                i--;
            }
        }
        studentDAO.deleteById(deletedId);
        ArrayList<Student> actualStudents = (ArrayList<Student>) studentDAO.findAll();
        assertTrue(expectedStudents.containsAll(actualStudents) && actualStudents.containsAll(expectedStudents));
    }
    
    @Test
    void shouldSetStudentGroup() {
	ScriptUtils.executeSqlScript(connection, testData);
	int groupId = 2;
	Group group = new Group();
	group.setId(groupId);
	group.setName("TestGroup2");
	
	int studentId = 5;
	Student expectedStudent = expectedStudents.stream().filter(student -> student.getId() == studentId).findFirst().get();
	expectedStudent.setGroup(group);
	
	studentDAO.setStudentGroup(groupId, studentId);
	Student actualStudent = studentDAO.findById(studentId);
	actualStudent.setGroup(studentDAO.getStudentGroup(studentId));
	assertEquals(expectedStudent, actualStudent);
    }
    
    @Test
    void shouldGetStudentGroup() {
	ScriptUtils.executeSqlScript(connection, testData);
	int groupId = 3;
	Group expectedGroup = new Group();
	expectedGroup.setId(groupId);
	expectedGroup.setName("TestGroup3");
	
	int studentId = 6;
	studentDAO.setStudentGroup(groupId, studentId);
	Group actualGroup = studentDAO.getStudentGroup(studentId);
	assertEquals(expectedGroup, actualGroup);
    }
}
