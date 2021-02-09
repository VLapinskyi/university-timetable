package ua.com.foxminded.dao;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.time.DayOfWeek;
import java.time.LocalTime;
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
import ua.com.foxminded.domain.Lecturer;
import ua.com.foxminded.domain.Lesson;
import ua.com.foxminded.domain.LessonTime;
import ua.com.foxminded.settings.SpringTestConfiguration;

@ContextConfiguration(classes = {SpringTestConfiguration.class})
@ExtendWith(SpringExtension.class)
class LessonDAOTest {
    @Autowired
    private LessonDAO lessonDAO;
    @Autowired
    private LecturerDAO lectureDAO;
    @Autowired
    private FacultyDAO facultyDAO;
    @Autowired
    private GroupDAO groupDAO;
    @Autowired
    private LessonTimeDAO lessonTimeDAO;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private ArrayList<Lesson> expectedLessons;
    private Connection connection;
    
    private final ClassPathResource testData = new ClassPathResource("/Test data.sql");
    private final ClassPathResource testTablesCreator = new ClassPathResource("/Creating tables.sql");
    private final ClassPathResource testDatabaseCleaner = new ClassPathResource("/Clearing database.sql");
    
    @BeforeEach
    void setUp() throws Exception {
        connection = jdbcTemplate.getDataSource().getConnection();
        ScriptUtils.executeSqlScript(connection, testTablesCreator);        
        expectedLessons = new ArrayList<>(Arrays.asList(
                new Lesson(), new Lesson(), new Lesson()));
        ArrayList<Integer> lessonIndexes = new ArrayList<>(Arrays.asList(1, 2, 3));
        ArrayList<String> lessonNames = new ArrayList<>(Arrays.asList(
                "Ukranian", "Music", "Physical Exercises"));
        ArrayList<String> audiences = new ArrayList<>(Arrays.asList("101", "102", "103"));
        ArrayList<DayOfWeek> weekDays = new ArrayList<>(Arrays.asList(
                DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY));
        for (int i = 0; i < expectedLessons.size(); i++) {
            expectedLessons.get(i).setId(lessonIndexes.get(i));
            expectedLessons.get(i).setName(lessonNames.get(i));
            expectedLessons.get(i).setAudience(audiences.get(i));
            expectedLessons.get(i).setDay(weekDays.get(i));
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        ScriptUtils.executeSqlScript(connection, testDatabaseCleaner);
    }

    @Test
    void shouldCreateLesson() {  
        Lesson testLesson = new Lesson();
        testLesson.setName("Ukranian");
        testLesson.setAudience("101");
        testLesson.setDay(DayOfWeek.TUESDAY);
        
        Lesson expectedLesson = new Lesson();
        expectedLesson.setId(1);
        expectedLesson.setName("Ukranian");
        expectedLesson.setAudience("101");
        expectedLesson.setDay(DayOfWeek.TUESDAY);
        
        lessonDAO.create(testLesson);
        assertEquals(expectedLesson, lessonDAO.findAll().stream().findFirst().get());
    }

    @Test
    void shouldFindAllLessons() {
        ScriptUtils.executeSqlScript(connection, testData);
        ArrayList<Lesson> actualLessons = (ArrayList<Lesson>) lessonDAO.findAll();
        assertTrue(expectedLessons.containsAll(actualLessons) && actualLessons.containsAll(expectedLessons));
    }

    @Test
    void shouldFindLessonById() {
        ScriptUtils.executeSqlScript(connection, testData);
        int checkedId = 2;
        Lesson expectedLesson = new Lesson();
        expectedLesson.setId(checkedId);
        expectedLesson.setName("Music");
        expectedLesson.setAudience("102");
        expectedLesson.setDay(DayOfWeek.WEDNESDAY);
        
        assertEquals(expectedLesson, lessonDAO.findById(checkedId));
    }

    @Test
    void shouldUpdateLesson() {
        ScriptUtils.executeSqlScript(connection, testData);  
        int testId = 2;
        Lesson testLesson = new Lesson();
        testLesson.setName("History");
        testLesson.setAudience("105");
        testLesson.setDay(DayOfWeek.TUESDAY);
        
        Lesson expectedLesson = new Lesson();
        expectedLesson.setId(testId);
        expectedLesson.setName("History");
        expectedLesson.setAudience("105");
        expectedLesson.setDay(DayOfWeek.TUESDAY);
        
        lessonDAO.update(testId, testLesson);
        assertEquals(expectedLesson, lessonDAO.findById(testId));

    }

    @Test
    void shouldDeleteLessonById() {
        ScriptUtils.executeSqlScript(connection, testData);
        int deletedId = 2;
        for (int i = 0; i < expectedLessons.size(); i++) {
            if (expectedLessons.get(i).getId() == deletedId) {
                expectedLessons.remove(i);
                i--;
            }
        }
        lessonDAO.deleteById(deletedId);
        ArrayList<Lesson> actualLessons = (ArrayList<Lesson>) lessonDAO.findAll();
        assertTrue(expectedLessons.containsAll(actualLessons) && actualLessons.containsAll(expectedLessons));
    }

    @Test
    void shouldGetDayLessonsForGroup() {
        ScriptUtils.executeSqlScript(connection, testData);
        
        DayOfWeek testDay = DayOfWeek.MONDAY;
        for (int i = 0; i < expectedLessons.size(); i++) {
            if (expectedLessons.get(i).getDay().getValue() != testDay.getValue() ||
                    !(expectedLessons.get(i).getGroup().equals(group))) {
                expectedLessons.remove(i);
                i--;
            }
        }
        
        ArrayList<Lesson> actualLessons = (ArrayList<Lesson>) lessonDAO.getDayLessonsForGroup(group, testDay);
        assertTrue(expectedLessons.containsAll(actualLessons) && actualLessons.containsAll(expectedLessons));
    }

    @Test
    void shouldGetDayLessonsForLecturer() {
        ScriptUtils.executeSqlScript(connection, testData);
        Lecturer lecturer = new Lecturer();
        lecturer.setId(3);
        lecturer.setFirstName("Vasyl");
        lecturer.setLastName("Dudchenko");
        lecturer.setGender(Gender.MALE);
        
        DayOfWeek testDay = DayOfWeek.THURSDAY;
        for (int i = 0; i < expectedLessons.size(); i++) {
            if (expectedLessons.get(i).getDay().getValue() != testDay.getValue() ||
                    !(expectedLessons.get(i).getLecturer().equals(lecturer))) {
                expectedLessons.remove(i);
                i--;
            }
        }
        
        ArrayList<Lesson> actualLessons = (ArrayList<Lesson>)
                lessonDAO.getDayLessonsForLecturer(lecturer, testDay);
        assertTrue(expectedLessons.containsAll(actualLessons) && actualLessons.containsAll(expectedLessons));
    }

}
