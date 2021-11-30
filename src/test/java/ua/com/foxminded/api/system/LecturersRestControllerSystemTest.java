package ua.com.foxminded.api.system;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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


import ua.com.foxminded.domain.Gender;
import ua.com.foxminded.domain.Lecturer;
import ua.com.foxminded.repositories.exceptions.RepositoryException;
import ua.com.foxminded.service.LecturerService;
import ua.com.foxminded.service.exceptions.ServiceException;

@SpringBootTest
@DBRider
@DBUnit(cacheConnection = false, leakHunter = true)
@TestPropertySource("/application-test.properties")
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
class LecturersRestControllerSystemTest {

    private final String testData = "/datasets/test-data.xml";
    private final String rootFolderDataSets = "/datasets/lecturers/";

    @Autowired
    private WebApplicationContext context;
    
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    @SpyBean
    private LecturerService lecturerService;
    
    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    @DataSet(value = testData, cleanBefore = true)
    @ExpectedDataSet(rootFolderDataSets + "all-lecturers.xml")
    void shouldGetLecturers() throws Exception {

        mockMvc.perform(get("/lecturers"))
            .andExpect(status().isOk())
            .andExpect(content()
                    .contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        verify(lecturerService).getAll();
    }

    @Test
    @DataSet(value = testData, cleanBefore = true)
    void shouldWhenGetLecturer() throws Exception {
        int id = 6;

        Lecturer lecturer = new Lecturer();
        lecturer.setId(id);
        lecturer.setFirstName("Vasyl");
        lecturer.setLastName("Dudchenko");
        lecturer.setGender(Gender.MALE);
        lecturer.setEmail("vdudchenko@test.com");
        lecturer.setPhoneNumber("+380457895263");
        
        String expectedResult = objectMapper.writeValueAsString(lecturer);

        mockMvc.perform(get("/lecturers/{id}", id)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content()
                    .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedResult));            

        verify(lecturerService).getById(id);
    }
    
    @Test
    @DataSet(cleanBefore = true)
    @ExpectedDataSet(rootFolderDataSets + "after-creating.xml")
    void shouldCreateLecturer() throws Exception {
        Lecturer lecturer = new Lecturer();
        lecturer.setFirstName("Petro");
        lecturer.setLastName("Petrov");
        lecturer.setGender(Gender.MALE);
        lecturer.setPhoneNumber("+380967845123");
        lecturer.setEmail("ppetrov@test.com");
        
        String testJson = objectMapper.writeValueAsString(lecturer);
        
        mockMvc.perform(post("/lecturers")
                .content(testJson)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
    }
    
    @Test
    @DataSet(value = testData, cleanBefore = true)
    @ExpectedDataSet(rootFolderDataSets + "after-updating.xml")
    void shouldUpdateLecturer() throws Exception {
        int testId = 5;
        Lecturer lecturer = new Lecturer();
        lecturer.setFirstName("Ivan");
        lecturer.setLastName("Ivanov");
        lecturer.setGender(Gender.MALE);
        lecturer.setId(testId);
        lecturer.setPhoneNumber("+380459865321");
        lecturer.setEmail("IIvanov@test.com");
        
        String testJson = objectMapper.writeValueAsString(lecturer);
        
        mockMvc.perform(patch("/lecturers/{id}", testId)
                .content(testJson).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(testJson));
        
        verify(lecturerService).update(lecturer);
    }
    
    @Test
    @DataSet(value = testData, cleanBefore = true, disableConstraints = true)
    @ExpectedDataSet(rootFolderDataSets + "after-deleting.xml")
    void shouldDeleteLecturer() throws Exception {
        int testId = 4;
        
        String expectedResult = "Lecturer with id: " + testId + " was deleted.";
        
        mockMvc.perform(delete("/lecturers/{id}", testId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(content()
                    .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(expectedResult));
        
        verify(lecturerService).deleteById(testId);
    }

    @Test
    @DataSet(value = testData, cleanBefore = true)
    void shouldReturnError500WhenRepositoryExceptionWhileGetLecturer() throws Exception {
        int id = 5;

        when(lecturerService.getById(id)).thenThrow(new ServiceException("Service exception", new RepositoryException()));

        mockMvc.perform(get("/lecturers/{id}", id)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError());
        
        verify(lecturerService).getById(id);
    }

    @Test
    void shouldReturnError400WhenIllegalArgumentExceptionWhileGetLecturer() throws Exception {
        int id = -5;
        
        mockMvc.perform(get("/lecturers/{id}", id)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnError404WhenEntityIsNotFoundWhileGetLecturer() throws Exception {
        int id = 5;
        
        mockMvc.perform(get("/lecturers/{id}", id)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
        
        verify(lecturerService).getById(id);
    }
    
    @Test
    void shouldReturnError500WhenRepositoryExceptionWhileCreate() throws Exception {
        Lecturer lecturer = new Lecturer();
        lecturer.setFirstName("Mariia");
        lecturer.setLastName("Romanova");
        lecturer.setGender(Gender.FEMALE);
        lecturer.setPhoneNumber("+380459865321");
        lecturer.setEmail("MRomanova@test.com");
        
        String testJson = objectMapper.writeValueAsString(lecturer);
        
        doThrow(new ServiceException("Service exception", new RepositoryException())).when(lecturerService).create(lecturer);
        
        mockMvc.perform(post("/lecturers")
                .content(testJson)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError());
        
        verify(lecturerService).create(lecturer);
    }
    
    @Test
    void shouldReturnError400ConstraintViolationExceptionWhileCreate() throws Exception {
        Lecturer lecturer = new Lecturer();
        lecturer.setFirstName("  Roman");
        lecturer.setLastName("Kolomiiets");
        lecturer.setGender(Gender.MALE);
        lecturer.setPhoneNumber("+380234598741");
        lecturer.setEmail("RKolomiiets@test.com");
        
        String testJson = objectMapper.writeValueAsString(lecturer);
        
        mockMvc.perform(post("/lecturers")
                .content(testJson)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
    }
    
    @Test
    void shouldReturnError400IllegalArgumentExceptionWhileCreate() throws Exception {
        Lecturer lecturer = null;
        
        String testJson = objectMapper.writeValueAsString(lecturer);
        
        mockMvc.perform(post("/lecturers")
                .content(testJson)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }
    
    @Test
    void shouldReturnError500ServiceExceptionWhileCreate() throws Exception {
        Lecturer lecturer = new Lecturer();
        lecturer.setFirstName("Vita");
        lecturer.setLastName("Didenko");
        lecturer.setGender(Gender.FEMALE);
        lecturer.setPhoneNumber("+380569874112");
        lecturer.setEmail("VDidenko@test.com");
        
        String testJson = objectMapper.writeValueAsString(lecturer);
        
        doThrow(ServiceException.class).when(lecturerService).create(lecturer);
        
        mockMvc.perform(post("/lecturers")
                .content(testJson)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError());
    }
    
    @Test
    @DataSet(value = testData, cleanBefore = true)
    void shouldReturnError500WhenRepositoryExceptionWhileUpdate() throws Exception {
        int testId = 4;
        
        Lecturer lecturer = new Lecturer();
        lecturer.setId(testId);
        lecturer.setFirstName("Vasyl");
        lecturer.setLastName("Kolisnichenko");
        lecturer.setGender(Gender.MALE);
        lecturer.setPhoneNumber("+380984563214");
        lecturer.setEmail("VKolisnichenko@test.com");
        
        String testJson = objectMapper.writeValueAsString(lecturer);
        
        doThrow(new ServiceException("Service exception", new RepositoryException())).when(lecturerService).update(lecturer);
        
        mockMvc.perform(patch("/lecturers/{id}", testId)
                .content(testJson)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError());
        
        verify(lecturerService).update(lecturer);
    }
    
    @Test
    void shouldReturnError400WhenConstraintViolationExceptionWhileUpdate() throws Exception {
        int testId = 8;
        
        Lecturer lecturer = new Lecturer();
        lecturer.setId(testId);
        lecturer.setFirstName("Mykhailo");
        lecturer.setLastName("Burlaka");
        lecturer.setGender(Gender.MALE);
        lecturer.setPhoneNumber("+380956352741");
        lecturer.setEmail("MBurlakatest.com");
        
        String testJson = objectMapper.writeValueAsString(lecturer);
        
        mockMvc.perform(patch("/lecturers/{id}", testId)
                .content(testJson)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
    }
    
    @Test
    void shouldReturnError400WhenIllegalArgumentExceptionWhileUpdate() throws Exception {
        int testId = -9;
        
        Lecturer lecturer = new Lecturer();
        lecturer.setId(testId);
        lecturer.setFirstName("Oleksandr");
        lecturer.setLastName("Ostapovets");
        lecturer.setGender(Gender.MALE);
        lecturer.setPhoneNumber("+380974563214");
        lecturer.setEmail("OOstapovets@test.com");
        
        String testJson = objectMapper.writeValueAsString(lecturer);
        
        mockMvc.perform(patch("/lecturers/{id}", testId)
                .content(testJson)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
    }
    
    @Test
    void shouldReturnError500WhenServiceExceptionWhileUpdate() throws Exception {
        int testId = 23;
        
        Lecturer lecturer = new Lecturer();
        lecturer.setId(testId);
        lecturer.setFirstName("Nataliia");
        lecturer.setLastName("Shvets");
        lecturer.setGender(Gender.FEMALE);
        lecturer.setPhoneNumber("+380459685741");
        lecturer.setEmail("NSvets@test.com");
        
        String testJson = objectMapper.writeValueAsString(lecturer);
        
        doThrow(ServiceException.class).when(lecturerService).update(lecturer);
        
        mockMvc.perform(patch("/lecturers/{id}", testId)
                .content(testJson)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError());
        
        verify(lecturerService).update(lecturer);
    }
    
    @Test
    void shouldReturnError500WhenRepositoryExceptionWhileDelete() throws Exception {
        int testId = 12;
        
        mockMvc.perform(delete("/lecturers/{id}", testId)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError());
        
        verify(lecturerService).deleteById(testId);
    }
    
    @Test
    void shouldReturnError400WhenIllegalArgumentExceptionWhileDelete() throws Exception {
        int testId = -43;
        
        mockMvc.perform(delete("/lecturers/{id}", testId)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
    }
    
    @Test
    void shouldReturnError500WhenServiceExceptionWhileDelete() throws Exception {
        int testId = 134;
        
        doThrow(ServiceException.class).when(lecturerService).deleteById(testId);
        
        mockMvc.perform(delete("/lecturers/{id}", testId)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError());
        
        verify(lecturerService).deleteById(testId);
    }
}
