package ua.com.foxminded.api.system;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

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
import ua.com.foxminded.repositories.exceptions.RepositoryException;
import ua.com.foxminded.service.FacultyService;
import ua.com.foxminded.service.exceptions.ServiceException;

@SpringBootTest
@DBRider
@DBUnit(cacheConnection = false, leakHunter = true)
@TestPropertySource("/application-test.properties")
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
class FacultiesRestControllerSystemTest {
    
    private final String testData = "/datasets/test-data.xml";
    private final String rootFolderDataSets = "/datasets/faculties/";

    @Autowired
    private WebApplicationContext context;
    
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    @SpyBean
    private FacultyService facultyService;
    
    @BeforeEach
    void init() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    @DataSet(value = testData, cleanBefore = true)
    @ExpectedDataSet(rootFolderDataSets + "all-faculties.xml")
    void shouldGetFaculties() throws Exception {
        mockMvc.perform(get("/faculties").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content()
                    .contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DataSet(value = testData, cleanBefore = true)
    void shouldGetFaculty() throws Exception {
        int id = 1;
        Faculty firstFaculty = new Faculty();
        firstFaculty.setId(id);
        firstFaculty.setName("TestFaculty1");
        
        String expectedResult = objectMapper.writeValueAsString(firstFaculty);

        mockMvc.perform(get("/faculties/{id}", id)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content()
                    .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedResult));
    }

    @Test
    @DataSet(cleanBefore = true)
    @ExpectedDataSet(rootFolderDataSets + "after-creating.xml")
    void shouldCreateFaculty() throws Exception {
        String facultyName = "Test Faculty";
        
        Faculty testFaculty = new Faculty();
        testFaculty.setName(facultyName);
        
        Faculty expectedFaculty = new Faculty();
        expectedFaculty.setId(1);
        expectedFaculty.setName(facultyName);

        String testJson = objectMapper.writeValueAsString(testFaculty);
        String expectedJson = objectMapper.writeValueAsString(expectedFaculty);
        
        mockMvc.perform(post("/faculties")
                .content(testJson).contentType(MediaType.APPLICATION_JSON))
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));
    }

    @Test
    @DataSet(value = testData, cleanBefore = true)
    @ExpectedDataSet(rootFolderDataSets + "after-updating.xml")
    void shouldUpdateFaculty() throws Exception {
        int testId = 2;
        Faculty testFaculty = new Faculty();
        testFaculty.setId(testId);
        testFaculty.setName("Test Faculty");
        
        String testJson = objectMapper.writeValueAsString(testFaculty);
        
        mockMvc.perform(patch("/faculties/{id}", testId)
                .content(testJson).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(testJson));
    }

    @Test
    @DataSet(value = testData, cleanBefore = true, disableConstraints = true)
    @ExpectedDataSet(rootFolderDataSets + "after-deleting.xml")
    void shouldDeleteFaculty() throws Exception {
        int testId = 2;
        
        String expectedResult = "Faculty with id: " + testId + " was deleted.";
        
        assertNotNull(facultyService.getById(testId));

        mockMvc.perform(delete("/faculties/{id}", testId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(content()
                .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(expectedResult));
    }

    @Test
    @DataSet(value = testData, cleanBefore = true)
    void shouldReturnError500WhenRepositoryExceptionWhileGetFaculty() throws Exception {
        int id = 2;

        when(facultyService.getById(id)).thenThrow(new ServiceException("message", new RepositoryException()));
        
        mockMvc.perform(get("/faculties/{id}", id)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError());
        
        verify(facultyService).getById(id);
    }

    @Test
    @DataSet(value = testData, cleanBefore = true)
    void shouldReturnError400WhenIllegalArgumentExceptionWhileGetFaculty() throws Exception {
        int id = -10;
        
        mockMvc.perform(get("/faculties/{id}", id)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnError404WhenEntityIsNotFoundWhileGetFaculty() throws Exception {
        int id = 1;
        
        mockMvc.perform(get("/faculties/{id}", id)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
        
        verify(facultyService).getById(id);
    }

    @Test
    void shouldReturnError500WhenRepositoryExceptionWhileCreateFaculty() throws Exception {
        Faculty testFaculty = new Faculty();
        testFaculty.setName("Test faculty");
        
        String testJson = objectMapper.writeValueAsString(testFaculty);

        doThrow(new ServiceException("Service exception", new RepositoryException())).when(facultyService).create(testFaculty);

        mockMvc.perform(post("/faculties")
                .content(testJson).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError());

        verify(facultyService).create(testFaculty);
    }

    @Test
    void shouldReturnError400WhenConstraintViolationExceptionWhileCreateFaculty() throws Exception {
        Faculty testFaculty = new Faculty();
        testFaculty.setName(" Test faculty");
        
        String testJson = objectMapper.writeValueAsString(testFaculty);

        mockMvc.perform(post("/faculties")
                .content(testJson).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnError400WhenIllegalArgumentExceptionWhileCreateFaculty() throws Exception {
        Faculty testFaculty = null;
        
        String testJson = objectMapper.writeValueAsString(testFaculty);

        mockMvc.perform(post("/faculties")
                .content(testJson).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnError500WhenServiceExceptionWhileCreateFaculty() throws Exception {
        Faculty testFaculty = new Faculty();
        testFaculty.setName("Test faculty");

        String testJson = objectMapper.writeValueAsString(testFaculty);
        
        doThrow(ServiceException.class).when(facultyService).create(testFaculty);

        mockMvc.perform(post("/faculties")
                .content(testJson).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError());

        verify(facultyService).create(testFaculty);
    }

    @Test
    void shouldReturnError500WhenRepositoryExceptionWhileUpdateFaculty() throws Exception {
        int testId = 4;
        Faculty testFaculty = new Faculty();
        testFaculty.setId(testId);
        testFaculty.setName("Test faculty");
        
        String testJson = objectMapper.writeValueAsString(testFaculty);

        doThrow(new ServiceException("Service exception", new RepositoryException())).when(facultyService).update(testFaculty);

        mockMvc.perform(patch("/faculties/{id}", testId)
                .content(testJson).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError());

        verify(facultyService).update(testFaculty);
    }

    @Test
    void shouldReturnError400WhenConstraintViolationExceptionWhileUpdateFaculty() throws Exception {
        int testId = 8;
        Faculty testFaculty = new Faculty();
        testFaculty.setId(testId);
        testFaculty.setName("       Test faculty");
        
        String testJson = objectMapper.writeValueAsString(testFaculty);

        mockMvc.perform(patch("/faculties/{id}", testId)
                .content(testJson).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnError400WhenIllegalArgumentExceptionWhileUpdateFaculty() throws Exception {
        int testId = 0;
        Faculty testFaculty = new Faculty();
        testFaculty.setId(testId);
        testFaculty.setName("Test faculty");
        
        String testJson = objectMapper.writeValueAsString(testFaculty);

        mockMvc.perform(patch("/faculties/{id}", testId)
                .content(testJson).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnError500WhenServiceExceptionWhileUpdateFaculty() throws Exception {
        int testId = 10;
        Faculty testFaculty = new Faculty();
        testFaculty.setId(testId);
        testFaculty.setName("Test faculty");
        
        String testJson = objectMapper.writeValueAsString(testFaculty);

        doThrow(ServiceException.class).when(facultyService).update(testFaculty);

        mockMvc.perform(patch("/faculties/{id}", testId)
                .content(testJson).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError());

        verify(facultyService).update(testFaculty);
    }

    @Test
    void shouldReturnError500WhenRepositoryExceptionWhileDeleteFaculty() throws Exception {
        int testId = 75;

        mockMvc.perform(delete("/faculties/{id}", testId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError());

        verify(facultyService).deleteById(testId);
    }

    @Test
    void shouldReturnError400WhenIllegalArgumentExceptionWhileDeleteFaculty() throws Exception {
        int testId = -14;

        mockMvc.perform(delete("/faculties/{id}", testId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnError500WhenServiceExceptionWhileDeleteFaculty() throws Exception {
        int testId = 41;

        doThrow(ServiceException.class).when(facultyService).deleteById(testId);

        mockMvc.perform(delete("/faculties/{id}", testId)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError());

        verify(facultyService).deleteById(testId);
    }

}
