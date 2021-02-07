package ua.com.foxminded.dao;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
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
import ua.com.foxminded.domain.Group;
import ua.com.foxminded.settings.SpringTestConfiguration;

@ContextConfiguration(classes = { SpringTestConfiguration.class})
@ExtendWith(SpringExtension.class)
class GroupDAOTest {
    @Autowired
    private GroupDAO groupDAO;
    @Autowired
    private FacultyDAO facultyDAO;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private ArrayList<Group> expectedGroups;
    private Connection connection;
    
    private final ClassPathResource testData = new ClassPathResource("/Test data.sql");
    private final ClassPathResource testTablesCreator = new ClassPathResource("/Creating tables.sql");
    private final ClassPathResource testDatabaseCleaner = new ClassPathResource("/Clearing database.sql");

    @BeforeEach
    void setUp() throws Exception {
        connection = jdbcTemplate.getDataSource().getConnection();
        ScriptUtils.executeSqlScript(connection, testTablesCreator);
        
        expectedGroups = new ArrayList<>(Arrays.asList(
                new Group(), new Group(), new Group()));
        ArrayList<String> groupNames = new ArrayList<>(Arrays.asList(
                "TestGroup1", "TestGroup2", "TestGroup3"));
        ArrayList<Integer> groupIndexes = new ArrayList<>(Arrays.asList(
                1, 2, 3));
        for (int i = 0; i < expectedGroups.size(); i++) {
            expectedGroups.get(i).setId(groupIndexes.get(i));
            expectedGroups.get(i).setName(groupNames.get(i));
        }
        
        ArrayList<Faculty> faculties = new ArrayList<>(Arrays.asList(
                new Faculty(), new Faculty()));
        ArrayList<String> facultyNames = new ArrayList<>(Arrays.asList(
                "TestFaculty1", "TestFaculty2"));
        ArrayList<Integer> facultyIndexes = new ArrayList<>(Arrays.asList(1, 2));
        for (int i = 0; i < faculties.size(); i++) {
            faculties.get(i).setId(facultyIndexes.get(i));
            faculties.get(i).setName(facultyNames.get(i));
        }
        
        expectedGroups.get(0).setFaculty(faculties.get(0));
        expectedGroups.get(1).setFaculty(faculties.get(1));
        expectedGroups.get(2).setFaculty(faculties.get(0));
    }

    @AfterEach
    void tearDown() throws Exception {
        ScriptUtils.executeSqlScript(connection, testDatabaseCleaner);
    }

    @Test
    void shouldCreateGroup() {
        Faculty faculty = new Faculty();
        faculty.setId(1);
        faculty.setName("TestFaculty");
        facultyDAO.create(faculty);
        
        Group testGroup = new Group();
        testGroup.setName("TestGroup");
        testGroup.setFaculty(faculty);
        Group expectedGroup = new Group();
        expectedGroup.setId(1);
        expectedGroup.setName("TestGroup");
        expectedGroup.setFaculty(faculty);
        groupDAO.create(testGroup);
        Group actualGroup = groupDAO.findAll().stream().findFirst().get();
        assertEquals(expectedGroup, actualGroup);
    }

    @Test
    void shouldFindAllGroups() throws ScriptException, SQLException {
        ScriptUtils.executeSqlScript(connection, testData);
        ArrayList<Group> actualGroups = (ArrayList<Group>) groupDAO.findAll();
        assertTrue(expectedGroups.containsAll(actualGroups) && actualGroups.containsAll(expectedGroups));
    }

    @Test
    void shouldFindGroupById() throws ScriptException, SQLException {
        ScriptUtils.executeSqlScript(connection, testData);
        int checkedGroupId = 2;
        Group expectedGroup = new Group();
        expectedGroup.setId(checkedGroupId);
        expectedGroup.setName("TestGroup2");
        
        Faculty faculty = new Faculty();
        faculty.setId(2);
        faculty.setName("TestFaculty2");
        expectedGroup.setFaculty(faculty);
        
        assertEquals(expectedGroup, groupDAO.findById(checkedGroupId));
    }

    @Test
    void shouldUpdateGroup() throws ScriptException, SQLException {
        ScriptUtils.executeSqlScript(connection, testData);
        Faculty faculty = new Faculty();
        faculty.setId(3);
        faculty.setName("TestFaculty3");
        
        int testGroupId = 2;        
        Group testGroup = new Group();
        testGroup.setName("TestGroupUpdated");
        testGroup.setFaculty(faculty);
        groupDAO.update(testGroupId, testGroup);
        Group expectedGroup = new Group();
        expectedGroup.setId(testGroupId);
        expectedGroup.setName("TestGroupUpdated");
        expectedGroup.setFaculty(faculty);
        assertEquals(expectedGroup, groupDAO.findById(testGroupId));
    }

    @Test
    void shouldDeleteGroupById() {
        ScriptUtils.executeSqlScript(connection, testData);
        int deletedGroupId = 2;
        for (int i = 0; i < expectedGroups.size(); i++) {
            if (expectedGroups.get(i).getId() == deletedGroupId) {
                expectedGroups.remove(i);
                i--;
            }
        }
        groupDAO.deleteById(deletedGroupId);
        ArrayList<Group> actualGroups = (ArrayList<Group>) groupDAO.findAll();
        assertTrue(expectedGroups.containsAll(actualGroups) && actualGroups.containsAll(expectedGroups));
    }
}
