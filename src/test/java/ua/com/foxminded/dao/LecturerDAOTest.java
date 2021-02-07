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
import ua.com.foxminded.domain.Lecturer;
import ua.com.foxminded.settings.SpringTestConfiguration;

@ContextConfiguration (classes = {SpringTestConfiguration.class})
@ExtendWith(SpringExtension.class)
class LecturerDAOTest {
    @Autowired
    private LecturerDAO lecturerDAO;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private ArrayList<Lecturer> expectedLecturers;
    private Connection connection;
    
    private final ClassPathResource testData = new ClassPathResource("/Test data.sql");
    private final ClassPathResource testTablesCreator = new ClassPathResource("/Creating tables.sql");
    private final ClassPathResource testDatabaseCleaner = new ClassPathResource("/Clearing database.sql");

    @BeforeEach
    void setUp() throws Exception {
        connection = jdbcTemplate.getDataSource().getConnection();
        ScriptUtils.executeSqlScript(connection, testTablesCreator);
        
        expectedLecturers = new ArrayList<>(Arrays.asList(
                new Lecturer(), new Lecturer(), new Lecturer()));
        ArrayList<Integer> indexes = new ArrayList<>(Arrays.asList(
                1, 2, 3));
        ArrayList<String> firstNames = new ArrayList<>(Arrays.asList(
                "Olena", "Ihor", "Vasyl"));
        ArrayList<String> lastNames = new ArrayList<>(Arrays.asList(
                "Skladenko", "Zakharchuk", "Dudchenko"));
        ArrayList<Gender> gendersForLecturers = new ArrayList<>(Arrays.asList(
                Gender.FEMALE, Gender.MALE, Gender.MALE));
        ArrayList<String> phoneNumbers = new ArrayList<>(Arrays.asList(
                "+380991111111", null, null));
        ArrayList<String> emails = new ArrayList<>(Arrays.asList(
                "oskladenko@gmail.com", "i.zakharchuk@gmail.com", null));
        for (int i = 0; i < expectedLecturers.size(); i++) {
            expectedLecturers.get(i).setId(indexes.get(i));
            expectedLecturers.get(i).setFirstName(firstNames.get(i));
            expectedLecturers.get(i).setLastName(lastNames.get(i));
            expectedLecturers.get(i).setGender(gendersForLecturers.get(i));
            expectedLecturers.get(i).setPhoneNumber(phoneNumbers.get(i));
            expectedLecturers.get(i).setEmail(emails.get(i));
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        ScriptUtils.executeSqlScript(connection, testDatabaseCleaner);
    }

    @Test
    void shouldCreateLecturer() {
        Lecturer expectedLecturer = new Lecturer();
        expectedLecturer.setId(1);
        expectedLecturer.setFirstName("First-name");
        expectedLecturer.setLastName("Last-name");
        expectedLecturer.setGender(Gender.MALE);
        expectedLecturer.setPhoneNumber("1233");
        
        Lecturer testLecturer = new Lecturer();
        testLecturer.setFirstName("First-name");
        testLecturer.setLastName("Last-name");
        testLecturer.setGender(Gender.MALE);
        testLecturer.setPhoneNumber("1233");
        
        lecturerDAO.create(testLecturer);
        Lecturer actualLecturer = lecturerDAO.findAll().stream().findFirst().get();
        assertEquals(expectedLecturer, actualLecturer);
    }

    @Test
    void shouldFindAllLecturers() {
        ScriptUtils.executeSqlScript(connection, testData);
        ArrayList<Lecturer> actualLecturers = (ArrayList<Lecturer>) lecturerDAO.findAll();
        assertTrue(expectedLecturers.containsAll(actualLecturers) && actualLecturers.containsAll(expectedLecturers));
    }

    @Test
    void shouldFindLecturerById() {
        ScriptUtils.executeSqlScript(connection, testData);
        int checkedId = 2;
        Lecturer expectedLecturer = new Lecturer();
        expectedLecturer.setId(checkedId);
        expectedLecturer.setFirstName("Ihor");
        expectedLecturer.setLastName("Zakharchuk");
        expectedLecturer.setGender(Gender.MALE);
        expectedLecturer.setEmail("i.zakharchuk@gmail.com");
        assertEquals(expectedLecturer, lecturerDAO.findById(checkedId));
    }

    @Test
    void shouldUpdateLecturer() {
        ScriptUtils.executeSqlScript(connection, testData);
        int testId = 2;
        
        Lecturer testLecturer = new Lecturer();
        testLecturer.setFirstName("Iryna");
        testLecturer.setLastName("Kohan");
        testLecturer.setGender(Gender.FEMALE);
        testLecturer.setEmail("i.kohan@gmail.com");
        testLecturer.setPhoneNumber("+380501234567");
        
        Lecturer expectedLecturer = new Lecturer();
        expectedLecturer.setId(testId);
        expectedLecturer.setFirstName("Iryna");
        expectedLecturer.setLastName("Kohan");
        expectedLecturer.setGender(Gender.FEMALE);
        expectedLecturer.setEmail("i.kohan@gmail.com");
        expectedLecturer.setPhoneNumber("+380501234567");
        
        lecturerDAO.update(testId, testLecturer);
        assertEquals(expectedLecturer, lecturerDAO.findById(testId));
    }

    @Test
    void shouldDeleteLecturerById() {
        ScriptUtils.executeSqlScript(connection, testData);
        int deletedId = 2;
        for (int i = 0; i < expectedLecturers.size(); i++) {
            if (expectedLecturers.get(i).getId() == deletedId) {
               expectedLecturers.remove(i);
               i--;
            }
        }
        lecturerDAO.deleteById(deletedId);
        ArrayList<Lecturer> actualLecturers = (ArrayList<Lecturer>) lecturerDAO.findAll();
        assertTrue(expectedLecturers.containsAll(actualLecturers) && actualLecturers.containsAll(expectedLecturers));
    }
}
