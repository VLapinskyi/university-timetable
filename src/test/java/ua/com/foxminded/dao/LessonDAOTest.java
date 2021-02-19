package ua.com.foxminded.dao;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    private JdbcTemplate jdbcTemplate;
    private List<Lesson> expectedLessons;
    private Connection connection;

    private final ClassPathResource testData = new ClassPathResource("/Test data.sql");
    private final ClassPathResource testTablesCreator = new ClassPathResource("/Creating tables.sql");
    private final ClassPathResource testDatabaseCleaner = new ClassPathResource("/Clearing database.sql");

    @BeforeEach
    void setUp() throws Exception {
        connection = jdbcTemplate.getDataSource().getConnection();
        ScriptUtils.executeSqlScript(connection, testTablesCreator);        
        expectedLessons = new ArrayList<>(Arrays.asList(
                new Lesson(), new Lesson(), new Lesson(), new Lesson()));
        List<Integer> lessonIndexes = new ArrayList<>(Arrays.asList(1, 2, 3, 4));
        List<String> lessonNames = new ArrayList<>(Arrays.asList(
                "Ukranian", "Music", "Physical Exercises", "Physical Exercises"));
        List<String> audiences = new ArrayList<>(Arrays.asList("101", "102", "103", "103"));
        List<DayOfWeek> weekDays = new ArrayList<>(Arrays.asList(
                DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.THURSDAY));
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
        List<Lesson> actualLessons = lessonDAO.findAll();
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
        List<Lesson> actualLessons = lessonDAO.findAll();
        assertTrue(expectedLessons.containsAll(actualLessons) && actualLessons.containsAll(expectedLessons));
    }

    @Test
    void shouldSetLessonLecturer() {
        ScriptUtils.executeSqlScript(connection, testData);
        int lecturerId = 2;
        Lecturer lecturer = new Lecturer();
        lecturer.setId(lecturerId);
        lecturer.setFirstName("Ihor");
        lecturer.setLastName("Zakharchuk");
        lecturer.setGender(Gender.MALE);
        lecturer.setEmail("i.zakharchuk@gmail.com");

        int lessonId = 2;
        Lesson expectedLesson = expectedLessons.stream().filter(lesson -> lesson.getId() == lessonId).findFirst().get();
        expectedLesson.setLecturer(lecturer);

        lessonDAO.setLessonLecturer(lecturerId, lessonId);
        Lesson actualLesson = lessonDAO.findById(lessonId);
        actualLesson.setLecturer(lessonDAO.getLessonLecturer(lessonId));
        assertEquals(expectedLesson, actualLesson);
    }

    @Test
    void shouldGetLessonLecturer() {
        ScriptUtils.executeSqlScript(connection, testData);
        int lecturerId = 3;
        Lecturer expectedLecturer = new Lecturer();
        expectedLecturer.setId(lecturerId);
        expectedLecturer.setFirstName("Vasyl");
        expectedLecturer.setLastName("Dudchenko");
        expectedLecturer.setGender(Gender.MALE);

        int lessonId = 1;
        lessonDAO.setLessonLecturer(lecturerId, lessonId);
        Lecturer actualLecturer = lessonDAO.getLessonLecturer(lessonId);
        assertEquals(expectedLecturer, actualLecturer);
    }

    @Test
    void shouldSetLessonGroup() {
        ScriptUtils.executeSqlScript(connection, testData);
        int groupId = 2;
        Group group = new Group();
        group.setId(groupId);
        group.setName("TestGroup2");

        int lessonId = 2;
        Lesson expectedLesson = expectedLessons.stream().filter(lesson -> lesson.getId() == lessonId).findFirst().get();
        expectedLesson.setGroup(group);

        lessonDAO.setLessonGroup(groupId, lessonId);
        Lesson actualLesson = lessonDAO.findById(lessonId);
        actualLesson.setGroup(lessonDAO.getLessonGroup(lessonId));
        assertEquals(expectedLesson, actualLesson);
    }

    @Test
    void shouldGetLessonGroup() {
        ScriptUtils.executeSqlScript(connection, testData);
        int groupId = 3;
        Group expectedGroup = new Group();
        expectedGroup.setId(groupId);
        expectedGroup.setName("TestGroup3");

        int lessonId = 1;
        lessonDAO.setLessonGroup(groupId, lessonId);
        Group actualGroup = lessonDAO.getLessonGroup(lessonId);
        assertEquals(expectedGroup, actualGroup);
    }

    @Test
    void shouldSetLessonTime() {
        ScriptUtils.executeSqlScript(connection, testData);
        int lessonTimeId = 1;
        LessonTime lessonTime = new LessonTime();
        lessonTime.setId(lessonTimeId);
        lessonTime.setStartTime(LocalTime.of(9, 0));
        lessonTime.setEndTime(LocalTime.of(10, 30));

        int lessonId = 2;
        Lesson expectedLesson = expectedLessons.stream().filter(lesson -> lesson.getId() == lessonId).findFirst().get();
        expectedLesson.setLessonTime(lessonTime);

        lessonDAO.setLessonTime(lessonTimeId, lessonId);
        Lesson actualLesson = lessonDAO.findById(lessonId);
        actualLesson.setLessonTime(lessonDAO.getLessonTime(lessonId));
        assertEquals(expectedLesson, actualLesson);
    }

    @Test
    void shouldGetLessonTime() {
        ScriptUtils.executeSqlScript(connection, testData);
        int lessonTimeId = 2;
        LessonTime expectedLessonTime = new LessonTime();
        expectedLessonTime.setId(lessonTimeId);
        expectedLessonTime.setStartTime(LocalTime.of(10, 45));
        expectedLessonTime.setEndTime(LocalTime.of(12, 15));

        int lessonId = 1;
        lessonDAO.setLessonTime(lessonTimeId, lessonId);
        LessonTime actualLessonTime = lessonDAO.getLessonTime(lessonId);
        assertEquals(expectedLessonTime, actualLessonTime);	
    }

    @Test
    void shouldGetDayLessonsForGroup() {
        ScriptUtils.executeSqlScript(connection, testData);
        int groupId = 1;
        DayOfWeek testDay = DayOfWeek.MONDAY;
        Lesson expectedLesson = expectedLessons.get(0);
        List<Lesson> actualLessons = lessonDAO.getGroupDayLessons(groupId, testDay);
        assertTrue(actualLessons.contains(expectedLesson) && actualLessons.size() == 1);
    }

    @Test
    void shouldGetDayLessonsForLecturer() {
        ScriptUtils.executeSqlScript(connection, testData);
        int lecturerId = 3;
        DayOfWeek testDay = DayOfWeek.THURSDAY;
        expectedLessons = new ArrayList<> (Arrays.asList(
                this.expectedLessons.get(2), this.expectedLessons.get(3)));

        List<Lesson> actualLessons = lessonDAO.getLecturerDayLessons(lecturerId, testDay);

        assertTrue(expectedLessons.containsAll(actualLessons) && actualLessons.containsAll(expectedLessons));
    }
}
