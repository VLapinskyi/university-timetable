package ua.com.foxminded.dao;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptException;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import ua.com.foxminded.domain.Faculty;
import ua.com.foxminded.settings.SpringTestConfiguration;

@ContextConfiguration(classes = { SpringTestConfiguration.class})
@ExtendWith(SpringExtension.class)
class FacultyDAOTest {
    @Autowired
    private FacultyDAO facultyDAO;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private ArrayList<Faculty> expectedFaculties;
    
    private static final String TABLES_CREATOR_SCRIPT = "/Creating tables.sql";
    private static final String TABLES_CLEANER_SCRIPT = "/Clearing database.sql";
    private static final String FACULTY_TABLE_DATA = "/Faculty test data.sql";
    
    @BeforeEach
    void setUp() throws ScriptException, SQLException {
        ScriptUtils.executeSqlScript(jdbcTemplate.getDataSource().getConnection(), new ClassPathResource(TABLES_CREATOR_SCRIPT));
        expectedFaculties = new ArrayList<> (Arrays.asList(
                new Faculty(), new Faculty(), new Faculty()));
        ArrayList<String> facultyNames = new ArrayList<>(Arrays.asList(
                "TestFaculty1", "TestFaculty2", "TestFaculty3"));
        ArrayList<Integer> facultyIndexes = new ArrayList<>(Arrays.asList(
                1, 2, 3));
        for (int i = 0; i < expectedFaculties.size(); i++) {
            expectedFaculties.get(i).setId(facultyIndexes.get(i));
            expectedFaculties.get(i).setName(facultyNames.get(i));
        }
    }
    
    @AfterEach
    void tearDown() throws ScriptException, SQLException {
        ScriptUtils.executeSqlScript(jdbcTemplate.getDataSource().getConnection(), new ClassPathResource(TABLES_CLEANER_SCRIPT));
    }

    @Test
    void shouldCreateFaculty() {
        int id = 1;
        String name = "TestFaculty";
        Faculty expectedFaculty = new Faculty();
        expectedFaculty.setId(id);
        expectedFaculty.setName(name);
        Faculty testFaculty = new Faculty();
        testFaculty.setName(name);
        facultyDAO.create(testFaculty);
        Faculty actualFaculty = facultyDAO.findAll().stream().findFirst().get();
        assertEquals(expectedFaculty, actualFaculty);
    }

    @Test
    void shouldFindAllFaculties() throws ScriptException, SQLException {
        ScriptUtils.executeSqlScript(jdbcTemplate.getDataSource().getConnection(), new ClassPathResource(FACULTY_TABLE_DATA));
        ArrayList<Faculty> actualFaculties = (ArrayList<Faculty>) facultyDAO.findAll();
        assertTrue(expectedFaculties.containsAll(actualFaculties) && actualFaculties.containsAll(expectedFaculties));
    }

    @Test
    void testFindById() throws ScriptException, SQLException {
        ScriptUtils.executeSqlScript(jdbcTemplate.getDataSource().getConnection(), new ClassPathResource(FACULTY_TABLE_DATA));
        int id = 2;
        String name = "TestFaculty2";
        Faculty expectedFaculty = new Faculty();
        expectedFaculty.setId(id);
        expectedFaculty.setName(name);
        assertEquals(expectedFaculty, facultyDAO.findById(id));
    }

    @Test
    void testUpdate() throws ScriptException, SQLException {
        ScriptUtils.executeSqlScript(jdbcTemplate.getDataSource().getConnection(), new ClassPathResource(FACULTY_TABLE_DATA));
        int id = 2;
        String name = "TestFacultyUpdated";
        Faculty testFaculty = new Faculty();
        testFaculty.setName(name);
        facultyDAO.update(id, testFaculty);
        Faculty expectedFaculty = new Faculty();
        expectedFaculty.setId(id);
        expectedFaculty.setName(name);
        assertEquals(expectedFaculty, facultyDAO.findById(id));
    }

    @Test
    void testDeleteById() throws ScriptException, SQLException {
        ScriptUtils.executeSqlScript(jdbcTemplate.getDataSource().getConnection(), new ClassPathResource(FACULTY_TABLE_DATA));
        int deletedFacultyId = 2;
        for (int i = 0; i < expectedFaculties.size(); i++) {
            if (expectedFaculties.get(i).getId() == deletedFacultyId) {
                expectedFaculties.remove(i);
            }
        }
        facultyDAO.deleteById(deletedFacultyId);
        ArrayList<Faculty> actualFaculties = (ArrayList<Faculty>) facultyDAO.findAll();
        assertTrue(expectedFaculties.containsAll(actualFaculties) && actualFaculties.containsAll(expectedFaculties));
    }

}
