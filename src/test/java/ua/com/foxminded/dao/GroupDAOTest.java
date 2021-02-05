package ua.com.foxminded.dao;

import static org.junit.jupiter.api.Assertions.*;

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

import ua.com.foxminded.domain.Group;
import ua.com.foxminded.settings.SpringTestConfiguration;

@ContextConfiguration(classes = { SpringTestConfiguration.class})
@ExtendWith(SpringExtension.class)
class GroupDAOTest {
    @Autowired
    private GroupDAO groupDAO;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private ArrayList<Group> expectedGroups;
    
    private static final String TABLES_CREATOR_SCRIPT = "/Creating tables.sql";
    private static final String TABLES_CLEANER_SCRIPT = "/Clearing database.sql";
    private static final String GROUP_TABLE_DATA = "/Group test data.sql";

    @BeforeEach
    void setUp() throws Exception {
        ScriptUtils.executeSqlScript(jdbcTemplate.getDataSource().getConnection(), new ClassPathResource(TABLES_CREATOR_SCRIPT));
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
    }

    //@AfterEach
    void tearDown() throws Exception {
        ScriptUtils.executeSqlScript(jdbcTemplate.getDataSource().getConnection(), new ClassPathResource(TABLES_CLEANER_SCRIPT));
    }

    @Test
    void shouldCreateGroup() {
        int id = 1;
        String name = "TestGroup";
        Group testGroup = new Group();
        testGroup.setName(name);
        Group expectedGroup = new Group();
        expectedGroup.setId(id);
        expectedGroup.setName(name);
        groupDAO.create(testGroup);
        Group actualGroup = groupDAO.findAll().stream().findFirst().get();
        assertEquals(expectedGroup, actualGroup);
    }

    @Test
    void testFindAll() {
        fail("Not yet implemented");
    }

    @Test
    void testFindById() {
        fail("Not yet implemented");
    }

    @Test
    void testUpdate() {
        fail("Not yet implemented");
    }

    @Test
    void testDeleteById() {
        fail("Not yet implemented");
    }

}
