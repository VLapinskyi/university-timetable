package ua.com.foxminded.api.system;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.junit5.api.DBRider;

import ua.com.foxminded.domain.Faculty;
import ua.com.foxminded.domain.Group;
import ua.com.foxminded.repositories.exceptions.RepositoryException;
import ua.com.foxminded.service.GroupService;
import ua.com.foxminded.service.exceptions.ServiceException;

@SpringBootTest
@DBRider
@DBUnit(cacheConnection = false, leakHunter = true)
@TestPropertySource("/application-test.properties")
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
class GroupsRestControllerSystemTest {

    private final String testData = "/datasets/test-data.xml";
    private final String rootFolderDataSets = "/datasets/groups/";
    
    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;
    
    @SpyBean
    @Autowired
    private GroupService groupService;
    
    @Autowired
    private ObjectMapper objectMapper;

    private Faculty faculty = new Faculty();
    private Faculty anotherFaculty = new Faculty();

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        faculty.setId(1);
        faculty.setName("TestFaculty1");

        anotherFaculty.setId(2);
        anotherFaculty.setName("TestFaculty2");
    }

    @Test
    @DataSet(value = testData, cleanBefore = true)
    @ExpectedDataSet(rootFolderDataSets + "all-groups.xml")
    void shouldGetGroups() throws Exception {
        mockMvc.perform(get("/groups"))
            .andExpect(status().isOk())
            .andExpect(content()
                    .contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        verify(groupService).getAll();
    }

    @Test
    @DataSet(value = testData, cleanBefore = true)
    void shouldGetGroup() throws Exception {
        int id = 2;
        Group group = new Group();
        group.setId(id);
        group.setName("TestGroup2");
        group.setFaculty(anotherFaculty);
        
        String expectedResult = objectMapper.writeValueAsString(group);

        mockMvc.perform(get("/groups/{id}", id)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content()
                    .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedResult));

        verify(groupService).getById(id);
    }

    @Test
    @DataSet(value = testData, cleanBefore = true)
    @ExpectedDataSet(rootFolderDataSets + "after-creating.xml")
    void shouldCreateGroup() throws Exception {
        int facultyId = 1;

        Group testGroup = new Group();
        testGroup.setName("Test Group");

        Group expectedGroup = new Group();
        expectedGroup.setId(4);
        expectedGroup.setName("Test Group");
        expectedGroup.setFaculty(faculty);
        
        String testJson = objectMapper.writeValueAsString(testGroup);
        String expectedJson = objectMapper.writeValueAsString(expectedGroup);

        mockMvc.perform(post("/groups")
                .content(testJson).param("faculty-id", Integer.toString(facultyId))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(content()
                    .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedJson))
            .andExpect(status().isOk());

        verify(groupService).create(expectedGroup);

    }

    @Test
    @DataSet(value = testData, cleanBefore = true)
    @ExpectedDataSet(rootFolderDataSets + "after-updating.xml")
    void shouldUpdateGroup() throws Exception {
        int groupId = 2;
        Group testGroup = new Group();
        testGroup.setId(groupId);
        testGroup.setName("Test Group");
        testGroup.setFaculty(faculty);
        
        String testJson = objectMapper.writeValueAsString(testGroup);

        mockMvc.perform(patch("/groups/{id}", groupId)
                .content(testJson).param("faculty-id", Integer.toString(faculty.getId()))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().json(testJson))
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
                
        verify(groupService).update(testGroup);
    }

    @Test
    @DataSet(value = testData, cleanBefore = true, disableConstraints = true)
    @ExpectedDataSet(rootFolderDataSets + "after-deleting.xml")
    void shouldDeleteGroup() throws Exception {
        int groupId = 1;
        
        String expectedResult = "Group with id: " + groupId + " was deleted.";

        mockMvc.perform(delete("/groups/{id}", Integer.toString(groupId))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().string(expectedResult));
     

        verify(groupService).deleteById(groupId);
    }   

    @Test
    @DataSet(value = testData, cleanBefore = true)
    void shouldReturnError500WhenRepositoryExceptionWhileGetGroup() throws Exception {
        int id = 2;

        when(groupService.getById(id)).thenThrow(new ServiceException("message", new RepositoryException()));
        
        mockMvc.perform(get("/groups/{id}", id)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError());
        
        verify(groupService).getById(id);
    }

    @Test
    void shouldReturnError400WhenIllegalArgumentExceptionWhileGetGroup() throws Exception {
        int id = -1;
        
        mockMvc.perform(get("/groups/{id}", id)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
        
    }

    @Test
    void shouldReturnError404WhenEntityIsNotFoundWhileGetGroup() throws Exception {
        int id = 2;
        
        mockMvc.perform(get("/groups/{id}", id)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    @DataSet(value = testData, cleanBefore = true)
    void shouldReturnError500WhenRepositoryExceptionWhileCreateGroup() throws Exception {
        Group testGroup = new Group();
        testGroup.setName("Test group");
        testGroup.setFaculty(faculty);
        
        doThrow(new ServiceException("Service exception", new RepositoryException())).when(groupService).create(testGroup);

        String testJson = objectMapper.writeValueAsString(testGroup);
        
        mockMvc.perform(post("/groups")
                .content(testJson).param("faculty-id", Integer.toString(faculty.getId()))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError());
    }

    @Test
    @DataSet(value = testData, cleanBefore = true)
    void shouldReturnError400WhenConstrantViolationExceptionWhileCreateGroup() throws Exception {
        Group testGroup = new Group();
        testGroup.setName(" Wrong name");
        testGroup.setFaculty(anotherFaculty);

        String testJson = objectMapper.writeValueAsString(testGroup);
        
        mockMvc.perform(post("/groups")
                .content(testJson).param("faculty-id", Integer.toString(anotherFaculty.getId()))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
    }

    @Test
    @DataSet(value = testData, cleanBefore = true)
    void shouldReturnError400WhenIllegalArgumentExceptionWhileCreateGroup() throws Exception {
        Group testGroup = null;

        String testJson = objectMapper.writeValueAsString(testGroup);
        
        mockMvc.perform(post("/groups")
                .content(testJson).param("faculty-id", Integer.toString(faculty.getId()))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
    }

    @Test
    @DataSet(value = testData, cleanBefore = true)
    void shouldReturnError500WhenServiceExceptionWhileCreateGroup() throws Exception {
        Group testGroup = new Group();
        testGroup.setName("Test group");
        testGroup.setFaculty(anotherFaculty);
        
        doThrow(ServiceException.class).when(groupService).create(testGroup);

        String testJson = objectMapper.writeValueAsString(testGroup);
        
        mockMvc.perform(post("/groups")
                .content(testJson).param("faculty-id", Integer.toString(anotherFaculty.getId()))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError());
    }

    @Test
    @DataSet(value = testData, cleanBefore = true)
    void shouldReturnError500WhenRepositoryExceptionWhileUpdateGroup() throws Exception {
        int groupId = 2;
        int wrongFacultyId = 5;
        Group testGroup = new Group();
        testGroup.setId(groupId);
        testGroup.setName("Test group");
        testGroup.setFaculty(anotherFaculty);

        String testJson = objectMapper.writeValueAsString(testGroup);
        
        mockMvc.perform(patch("/groups/{id}", groupId)
                .content(testJson).param("faculty-id", Integer.toString(wrongFacultyId))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError());
    }

    @Test
    @DataSet(value = testData, cleanBefore = true)
    void shouldReturnError400WhenConstraintViolationExceptionWhileUpdate() throws Exception {
        int groupId = 2;
        int facultyId = faculty.getId();
        Group testGroup = new Group();
        testGroup.setId(groupId);
        testGroup.setName("     Test group");
        testGroup.setFaculty(anotherFaculty);

        String testJson = objectMapper.writeValueAsString(testGroup);
        
        mockMvc.perform(patch("/groups/{id}", groupId)
                .content(testJson).param("faculty-id", Integer.toString(facultyId))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
    }
    
    @Test
    @DataSet(value = testData, cleanBefore = true)
    void shouldReturnError400WhenIllegalArgumentExceptionWhileUpdate() throws Exception {
        int groupId = 2;
        int facultyId = anotherFaculty.getId();
        Group testGroup = null;

        String testJson = objectMapper.writeValueAsString(testGroup);
        
        mockMvc.perform(patch("/groups/{id}", groupId)
                .content(testJson).param("faculty-id", Integer.toString(facultyId))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

    }
    
    @Test
    @DataSet(value = testData, cleanBefore = true)
    void shouldReturnError500WhenServiceExceptionWhileUpdate() throws Exception {
        int groupId = 1;
        int facultyId = anotherFaculty.getId();
        Group testGroup = new Group();
        testGroup.setId(groupId);
        testGroup.setName("Test group");
        testGroup.setFaculty(anotherFaculty);

        doThrow(ServiceException.class).when(groupService).update(testGroup);

        String testJson = objectMapper.writeValueAsString(testGroup);
        
        mockMvc.perform(patch("/groups/{id}", groupId)
                .content(testJson).param("faculty-id", Integer.toString(facultyId))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError());

        verify(groupService).update(testGroup);
    }
    
    @Test
    void shouldReturnError500WhenRepositoryExceptionWhileDeleteGroup() throws Exception {
        int testId = 2;
        
        mockMvc.perform(delete("/groups/{id}", testId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError());
        
        verify(groupService).deleteById(testId);
    }
    
    @Test
    void shouldReturnError400WhenIllegalArgumentExceptionWhileDeleteGroup() throws Exception {
        int testId = -7;
        
        mockMvc.perform(delete("/groups/{id}", testId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }
    
    @Test
    void shouldReturnError500WhenServiceExceptionWhileDeleteGroup() throws Exception {
        int testId = 6;
        
        doThrow(ServiceException.class).when(groupService).deleteById(testId);
        
        mockMvc.perform(delete("/groups/{id}", testId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError());
        
        verify(groupService).deleteById(testId);
    }
}