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
        Faculty faculty = new Faculty();
        faculty.setId(1);
        faculty.setName("TestFaculty1");
        
        ArrayList<Group> groups = new ArrayList<>(Arrays.asList(
                new Group(), new Group()));
        ArrayList<Integer> groupIndexes = new ArrayList<>(Arrays.asList(1, 3));
        ArrayList<String> groupNames = new ArrayList<>(Arrays.asList(
                "TestGroup1", "TestGroup3"));
        for (int i = 0; i < groups.size(); i++) {
            groups.get(i).setId(groupIndexes.get(i));
            groups.get(i).setName(groupNames.get(i));
            groups.get(i).setFaculty(faculty);
        }
        
        ArrayList<Lecturer> lecturers = new ArrayList<>(Arrays.asList(
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
        for (int i = 0; i < lecturers.size(); i++) {
            lecturers.get(i).setId(indexes.get(i));
            lecturers.get(i).setFirstName(firstNames.get(i));
            lecturers.get(i).setLastName(lastNames.get(i));
            lecturers.get(i).setGender(gendersForLecturers.get(i));
            lecturers.get(i).setPhoneNumber(phoneNumbers.get(i));
            lecturers.get(i).setEmail(emails.get(i));
        }
        
        ArrayList<LessonTime> lessonTimes = new ArrayList<>(Arrays.asList(
                new LessonTime(), new LessonTime()));
        ArrayList<Integer> lessonTimeIndexes = new ArrayList<>(Arrays.asList(1, 3));
        ArrayList<LocalTime> startTimes = new ArrayList<>(Arrays.asList(
                LocalTime.of(9, 0), LocalTime.of(12, 30)));
        ArrayList<LocalTime> endTimes = new ArrayList<>(Arrays.asList(
                LocalTime.of(10, 30), LocalTime.of(14, 0)));
        for (int i = 0; i < lessonTimes.size(); i++) {
            lessonTimes.get(i).setId(lessonTimeIndexes.get(i));
            lessonTimes.get(i).setStartTime(startTimes.get(i));
            lessonTimes.get(i).setEndTime(endTimes.get(i));
        }
        
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
            expectedLessons.get(i).setLecturer(lecturers.get(i));
            expectedLessons.get(i).setAudience(audiences.get(i));
            expectedLessons.get(i).setDay(weekDays.get(i));
        }
        expectedLessons.get(0).setGroup(groups.get(0));
        expectedLessons.get(1).setGroup(groups.get(1));
        expectedLessons.get(2).setGroup(groups.get(0));
        expectedLessons.get(0).setLessonTime(lessonTimes.get(0));
        expectedLessons.get(1).setLessonTime(lessonTimes.get(0));
        expectedLessons.get(2).setLessonTime(lessonTimes.get(1));
    }

    @AfterEach
    void tearDown() throws Exception {
        ScriptUtils.executeSqlScript(connection, testDatabaseCleaner);
    }

    @Test
    void shouldCreateLesson() {
        Faculty faculty = new Faculty();
        faculty.setId(1);
        faculty.setName("TestFaculty");
        facultyDAO.create(faculty);
        
        Group group = new Group();
        group.setId(1);
        group.setName("TestGroup");
        group.setFaculty(faculty);
        groupDAO.create(group);
        
        Lecturer lecturer = new Lecturer();
        lecturer.setId(1);
        lecturer.setFirstName("Olena");
        lecturer.setLastName("Bilous");
        lecturer.setGender(Gender.FEMALE);
        lecturer.setPhoneNumber("+380441474852");
        lectureDAO.create(lecturer);
        
        LessonTime lessonTime = new LessonTime();
        lessonTime.setId(1);
        lessonTime.setStartTime(LocalTime.of(9, 0));
        lessonTime.setEndTime(LocalTime.of(10, 30));
        lessonTimeDAO.create(lessonTime);
        
        Lesson testLesson = new Lesson();
        testLesson.setName("Ukranian");
        testLesson.setLecturer(lecturer);
        testLesson.setGroup(group);
        testLesson.setAudience("101");
        testLesson.setDay(DayOfWeek.TUESDAY);
        testLesson.setLessonTime(lessonTime);
        
        
        Lesson expectedLesson = new Lesson();
        expectedLesson.setId(1);
        expectedLesson.setName("Ukranian");
        expectedLesson.setLecturer(lecturer);
        expectedLesson.setGroup(group);
        expectedLesson.setAudience("101");
        expectedLesson.setDay(DayOfWeek.TUESDAY);
        expectedLesson.setLessonTime(lessonTime);
        
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
        
        Faculty faculty = new Faculty();
        faculty.setId(1);
        faculty.setName("TestFaculty1");
        
        Group group = new Group();
        group.setId(3);
        group.setName("TestGroup3");
        group.setFaculty(faculty);
        
        Lecturer lecturer = new Lecturer();
        lecturer.setId(2);
        lecturer.setFirstName("Ihor");
        lecturer.setLastName("Zakharchuk");
        lecturer.setGender(Gender.MALE);
        lecturer.setEmail("i.zakharchuk@gmail.com");
        
        LessonTime lessonTime = new LessonTime();
        lessonTime.setId(1);
        lessonTime.setStartTime(LocalTime.of(9, 0));
        lessonTime.setEndTime(LocalTime.of(10, 30));
        
        int checkedId = 2;
        Lesson expectedLesson = new Lesson();
        expectedLesson.setId(checkedId);
        expectedLesson.setName("Music");
        expectedLesson.setLecturer(lecturer);
        expectedLesson.setGroup(group);
        expectedLesson.setAudience("102");
        expectedLesson.setDay(DayOfWeek.WEDNESDAY);
        expectedLesson.setLessonTime(lessonTime);
        
        assertEquals(expectedLesson, lessonDAO.findById(checkedId));
    }

    @Test
    void shouldUpdateLesson() {
        ScriptUtils.executeSqlScript(connection, testData);
        
        Faculty faculty = new Faculty();
        faculty.setId(2);
        faculty.setName("TestFaculty2");
        
        Group group = new Group();
        group.setId(2);
        group.setName("TestGroup2");
        group.setFaculty(faculty);
        
        Lecturer lecturer = new Lecturer();
        lecturer.setId(3);
        lecturer.setFirstName("Vasyl");
        lecturer.setLastName("Dudchenko");
        lecturer.setGender(Gender.MALE);
        
        LessonTime lessonTime = new LessonTime();
        lessonTime.setId(3);
        lessonTime.setStartTime(LocalTime.of(12, 30));
        lessonTime.setEndTime(LocalTime.of(14, 0));
        
        int testId = 2;
        
        Lesson testLesson = new Lesson();
        testLesson.setName("History");
        testLesson.setLecturer(lecturer);
        testLesson.setGroup(group);
        testLesson.setAudience("105");
        testLesson.setDay(DayOfWeek.TUESDAY);
        testLesson.setLessonTime(lessonTime);
        
        Lesson expectedLesson = new Lesson();
        expectedLesson.setId(testId);
        expectedLesson.setName("History");
        expectedLesson.setLecturer(lecturer);
        expectedLesson.setGroup(group);
        expectedLesson.setAudience("105");
        expectedLesson.setDay(DayOfWeek.TUESDAY);
        expectedLesson.setLessonTime(lessonTime);
        
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
        Faculty faculty = new Faculty();
        faculty.setId(1);
        faculty.setName("TestFaculty1");
        
        Group group = new Group();
        group.setId(1);
        group.setName("TestGroup1");
        group.setFaculty(faculty);
        
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
