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

import ua.com.foxminded.domain.Faculty;
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
    private GroupDAO groupDAO;
    @Autowired
    private FacultyDAO facultyDAO;
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
        
        ArrayList<Group> groups = new ArrayList<>(Arrays.asList(
                new Group(), new Group()));
        ArrayList<Integer> groupIndexes = new ArrayList<>(Arrays.asList(1, 2));
        ArrayList<String> groupNames = new ArrayList<>(Arrays.asList(
                "TestGroup1", "TestGroup2"));
        for(int i = 0; i < groups.size(); i++) {
            groups.get(i).setId(groupIndexes.get(i));
            groups.get(i).setName(groupNames.get(i));
        }
        
        ArrayList<Faculty> faculties = new ArrayList<>(Arrays.asList(
                new Faculty(), new Faculty()));
        ArrayList<Integer> facultyIndexes = new ArrayList<>(Arrays.asList(1, 2));
        ArrayList<String> facultyNames = new ArrayList<>(Arrays.asList(
                "TestFaculty1", "TestFaculty2"));
        for(int i = 0; i < faculties.size(); i++) {
            faculties.get(i).setId(facultyIndexes.get(i));
            faculties.get(i).setName(facultyNames.get(i));
        }
        
        groups.get(0).setFaculty(faculties.get(0));
        groups.get(1).setFaculty(faculties.get(1));
        
        expectedStudents.get(0).setGroup(groups.get(0));
        expectedStudents.get(1).setGroup(groups.get(0));
        expectedStudents.get(2).setGroup(groups.get(1));
        
        
    }

    @AfterEach
    void tearDown() throws Exception {
        ScriptUtils.executeSqlScript(connection, testDatabaseCleaner);
    }

    @Test
    void shouldCreateStudent() {
        Faculty faculty = new Faculty();
        faculty.setId(1);
        faculty.setName("TestFaculty");
        facultyDAO.create(faculty);
        
        Group group = new Group();
        group .setId(1);
        group .setName("TestGroup");
        group.setFaculty(faculty);
        groupDAO.create(group);
        
        Student expectedStudent = new Student();
        expectedStudent.setId(1);
        expectedStudent.setFirstName("First-name");
        expectedStudent.setLastName("Last-name");
        expectedStudent.setGender(Gender.MALE);
        expectedStudent.setPhoneNumber("1233");
        expectedStudent.setGroup(group);
        
        Student testStudent = new Student();
        testStudent.setFirstName("First-name");
        testStudent.setLastName("Last-name");
        testStudent.setGender(Gender.MALE);
        testStudent.setPhoneNumber("1233");
        testStudent.setGroup(group);       
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
        Faculty faculty = new Faculty();
        faculty.setId(1);
        faculty.setName("TestFaculty1");;
        
        Group group = new Group();
        group.setId(1);
        group.setName("TestGroup1");
        group.setFaculty(faculty);
        
        int checkedId = 5;
        Student expectedStudent = new Student();
        expectedStudent.setId(checkedId);
        expectedStudent.setFirstName("Illia");
        expectedStudent.setLastName("Misiats");
        expectedStudent.setGender(Gender.MALE);
        expectedStudent.setEmail("illiamisiats@gmail.com");
        expectedStudent.setGroup(group);
        assertEquals(expectedStudent, studentDAO.findById(checkedId));
    }

    @Test
    void shouldUpdateStudent() {
        ScriptUtils.executeSqlScript(connection, testData);
        Faculty expectedFaculty = new Faculty();
        expectedFaculty.setId(1);
        expectedFaculty.setName("TestFaculty1");
        
        Group expectedGroup = new Group();
        expectedGroup.setId(3);       
        expectedGroup.setName("TestGroup3");
        expectedGroup.setFaculty(expectedFaculty);
        
        int testId = 5;
        Student testStudent = new Student();
        testStudent.setFirstName("Tetiana");
        testStudent.setLastName("Lytvynenko");
        testStudent.setGender(Gender.FEMALE);
        testStudent.setGroup(expectedGroup);
        
        Student expectedStudent = new Student();
        expectedStudent.setId(testId);
        expectedStudent.setFirstName("Tetiana");
        expectedStudent.setLastName("Lytvynenko");
        expectedStudent.setGender(Gender.FEMALE);
        expectedStudent.setGroup(expectedGroup);
        
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
}
