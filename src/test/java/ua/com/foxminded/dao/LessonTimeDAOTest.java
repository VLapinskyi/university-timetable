package ua.com.foxminded.dao;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
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

import ua.com.foxminded.domain.LessonTime;
import ua.com.foxminded.settings.SpringTestConfiguration;

@ContextConfiguration(classes = {SpringTestConfiguration.class})
@ExtendWith(SpringExtension.class)
class LessonTimeDAOTest {
    @Autowired
    private LessonTimeDAO lessonTimeDAO;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private List<LessonTime> expectedLessonTimes;
    private Connection connection;
    
    private final ClassPathResource testData = new ClassPathResource("/Test data.sql");
    private final ClassPathResource testTablesCreator = new ClassPathResource("/Creating tables.sql");
    private final ClassPathResource testDatabaseCleaner = new ClassPathResource("/Clearing database.sql");

    @BeforeEach
    void setUp() throws Exception {
        connection = jdbcTemplate.getDataSource().getConnection();
        ScriptUtils.executeSqlScript(connection, testTablesCreator);
        expectedLessonTimes = new ArrayList<>(Arrays.asList(
                new LessonTime(), new LessonTime(), new LessonTime()));
        List<Integer> indexes = new ArrayList<>(Arrays.asList(1, 2, 3));
        List<LocalTime> startTimes = new ArrayList<>(Arrays.asList(
                LocalTime.of(9, 0), LocalTime.of(10, 45), LocalTime.of(12, 30)));
        List<LocalTime> endTimes = new ArrayList<>(Arrays.asList(
                LocalTime.of(10, 30), LocalTime.of(12, 15), LocalTime.of(14, 0)));
        for (int i = 0; i < expectedLessonTimes.size(); i++) {
            expectedLessonTimes.get(i).setId(indexes.get(i));
            expectedLessonTimes.get(i).setStartTime(startTimes.get(i));
            expectedLessonTimes.get(i).setEndTime(endTimes.get(i));
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        ScriptUtils.executeSqlScript(connection, testDatabaseCleaner);
    }

    @Test
    void shouldCreateLessonTime() {
        LessonTime testLessonTime = new LessonTime();
        testLessonTime.setStartTime(LocalTime.of(9, 0));
        testLessonTime.setEndTime(LocalTime.of(10, 0));
        
        LessonTime expectedLessonTime = new LessonTime();
        expectedLessonTime.setId(1);
        expectedLessonTime.setStartTime(LocalTime.of(9, 0));
        expectedLessonTime.setEndTime(LocalTime.of(10, 0));
        
        lessonTimeDAO.create(testLessonTime);
        assertEquals(expectedLessonTime, lessonTimeDAO.findAll().stream().findFirst().get());
    }

    @Test
    void shouldFindAllLessonTimes() {
        ScriptUtils.executeSqlScript(connection, testData);
        List<LessonTime> actualLessonTimes = lessonTimeDAO.findAll();
        assertTrue(expectedLessonTimes.containsAll(actualLessonTimes) && actualLessonTimes.containsAll(expectedLessonTimes));
    }

    @Test
    void shouldFindLessonTimeById() {
        ScriptUtils.executeSqlScript(connection, testData);
        int checkedId = 2;
        LessonTime expectedLessonTime = new LessonTime();
        expectedLessonTime.setId(checkedId);
        expectedLessonTime.setStartTime(LocalTime.of(10, 45));
        expectedLessonTime.setEndTime(LocalTime.of(12, 15));
        assertEquals(expectedLessonTime, lessonTimeDAO.findById(checkedId));
    }

    @Test
    void shouldUpdateLessonTime() {
        ScriptUtils.executeSqlScript(connection, testData);
        int testId = 2;
        LessonTime testLessonTime = new LessonTime();
        testLessonTime.setStartTime(LocalTime.of(15, 0));
        testLessonTime.setEndTime(LocalTime.of(16, 0));
        
        LessonTime expectedLessonTime = new LessonTime();
        expectedLessonTime.setId(testId);
        expectedLessonTime.setStartTime(LocalTime.of(15, 0));
        expectedLessonTime.setEndTime(LocalTime.of(16, 0));
        
        lessonTimeDAO.update(testId, testLessonTime);
        assertEquals(expectedLessonTime, lessonTimeDAO.findById(testId));
    }

    @Test
    void shouldDeleteLessonTimeById() {
        ScriptUtils.executeSqlScript(connection, testData);
        int deletedId = 2;
        for (int i = 0; i < expectedLessonTimes.size(); i++) {
            if (expectedLessonTimes.get(i).getId() == deletedId) {
                expectedLessonTimes.remove(i);
                i--;
            }
        }
        lessonTimeDAO.deleteById(deletedId);
        List<LessonTime> actualLessonTimes = lessonTimeDAO.findAll();
        assertTrue(expectedLessonTimes.containsAll(actualLessonTimes) && actualLessonTimes.containsAll(expectedLessonTimes));
    }
}
