package ua.com.foxminded.dao;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.SQLException;
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
    private JdbcTemplate jdbcTemplate;
    private List<Group> expectedGroups;
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
        List<String> groupNames = new ArrayList<>(Arrays.asList(
                "TestGroup1", "TestGroup2", "TestGroup3"));
        List<Integer> groupIndexes = new ArrayList<>(Arrays.asList(
                1, 2, 3));
        for (int i = 0; i < expectedGroups.size(); i++) {
            expectedGroups.get(i).setId(groupIndexes.get(i));
            expectedGroups.get(i).setName(groupNames.get(i));
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        ScriptUtils.executeSqlScript(connection, testDatabaseCleaner);
    }

    @Test
    void shouldCreateGroup() {
        Group testGroup = new Group();
        testGroup.setName("TestGroup");
        Group expectedGroup = new Group();
        expectedGroup.setId(1);
        expectedGroup.setName("TestGroup");
        groupDAO.create(testGroup);
        Group actualGroup = groupDAO.findAll().stream().findFirst().get();
        assertEquals(expectedGroup, actualGroup);
    }

    @Test
    void shouldFindAllGroups() throws ScriptException, SQLException {
        ScriptUtils.executeSqlScript(connection, testData);
        List<Group> actualGroups = groupDAO.findAll();
        assertTrue(expectedGroups.containsAll(actualGroups) && actualGroups.containsAll(expectedGroups));
    }

    @Test
    void shouldFindGroupById() throws ScriptException, SQLException {
        ScriptUtils.executeSqlScript(connection, testData);
        int checkedGroupId = 2;
        Group expectedGroup = new Group();
        expectedGroup.setId(checkedGroupId);
        expectedGroup.setName("TestGroup2");

        assertEquals(expectedGroup, groupDAO.findById(checkedGroupId));
    }

    @Test
    void shouldUpdateGroup() throws ScriptException, SQLException {
        ScriptUtils.executeSqlScript(connection, testData);
        int testGroupId = 2;        
        Group testGroup = new Group();
        testGroup.setName("TestGroupUpdated");
        groupDAO.update(testGroupId, testGroup);
        Group expectedGroup = new Group();
        expectedGroup.setId(testGroupId);
        expectedGroup.setName("TestGroupUpdated");
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
        List<Group> actualGroups = groupDAO.findAll();
        assertTrue(expectedGroups.containsAll(actualGroups) && actualGroups.containsAll(expectedGroups));
    }

    @Test
    void shouldSetGroupFaculty() {
        ScriptUtils.executeSqlScript(connection, testData);
        int facultyId = 1;
        Faculty faculty = new Faculty();
        faculty.setId(facultyId);
        faculty.setName("TestFaculty1");

        int groupId = 2;
        Group expectedGroup = expectedGroups.stream().filter(group -> group.getId() == groupId).findFirst().get();
        expectedGroup.setFaculty(faculty);

        groupDAO.setGroupFaculty(facultyId, groupId);
        Group actualGroup = groupDAO.findById(groupId);
        actualGroup.setFaculty(groupDAO.getGroupFaculty(groupId));
        assertEquals(expectedGroup, actualGroup);
    }

    @Test
    void shouldGetGroupFaculty() {
        ScriptUtils.executeSqlScript(connection, testData);
        int facultyId = 2;
        Faculty expectedFaculty = new Faculty();
        expectedFaculty.setId(facultyId);
        expectedFaculty.setName("TestFaculty2");

        int groupId = 2;
        groupDAO.setGroupFaculty(facultyId, groupId);
        Faculty actualFaculty = groupDAO.getGroupFaculty(groupId);
        assertEquals(expectedFaculty, actualFaculty);
    }
}
