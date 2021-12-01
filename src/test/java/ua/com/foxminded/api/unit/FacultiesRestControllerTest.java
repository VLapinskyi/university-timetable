package ua.com.foxminded.api.unit;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.validation.ConstraintViolationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import ua.com.foxminded.api.FacultiesRestController;
import ua.com.foxminded.api.aspects.GeneralRestControllerAspect;
import ua.com.foxminded.domain.Faculty;
import ua.com.foxminded.repositories.exceptions.RepositoryException;
import ua.com.foxminded.service.FacultyService;
import ua.com.foxminded.service.exceptions.ServiceException;

@WebMvcTest(FacultiesRestController.class)
@Import({AopAutoConfiguration.class, GeneralRestControllerAspect.class})
class FacultiesRestControllerTest {
    
    @Autowired
    private FacultiesRestController facultiesRestController;

    @MockBean
    private FacultyService facultyService;

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void init() {
        ReflectionTestUtils.setField(facultiesRestController, "facultyService", facultyService);
    }

    @Test
    void shouldGetFaculties() throws Exception {
        Faculty firstFaculty = new Faculty();
        firstFaculty.setId(1);
        firstFaculty.setName("First faculty");

        Faculty secondFaculty = new Faculty();
        secondFaculty.setId(2);
        secondFaculty.setName("Second faculty");
        
        List<Faculty> faculties = new ArrayList<>(Arrays.asList(firstFaculty, secondFaculty));
        
        String expectedResult = objectMapper.writeValueAsString(faculties);

        when(facultyService.getAll()).thenReturn(faculties);

        mockMvc.perform(get("/faculties").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content()
                    .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedResult));
        
        verify(facultyService).getAll();
    }

    @Test
    void shouldGetFaculty() throws Exception {
        int id = 1;
        Faculty firstFaculty = new Faculty();
        firstFaculty.setId(id);
        firstFaculty.setName("First faculty");
        
        String expectedResult = objectMapper.writeValueAsString(firstFaculty);

        when(facultyService.getById(id)).thenReturn(firstFaculty);

        mockMvc.perform(get("/faculties/{id}", id)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content()
                    .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedResult));
        
        verify(facultyService).getById(1);
    }

    @Test
    void shouldCreateFaculty() throws Exception {
        Faculty testFaculty = new Faculty();
        testFaculty.setName("Test faculty");

        String testJson = objectMapper.writeValueAsString(testFaculty);
        
        mockMvc.perform(post("/faculties")
                .content(testJson).contentType(MediaType.APPLICATION_JSON))
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
           
        verify(facultyService).create(testFaculty);
    }

    @Test
    void shouldUpdateFaculty() throws Exception {
        int testId = 10;
        Faculty testFaculty = new Faculty();
        testFaculty.setId(testId);
        testFaculty.setName("Test faculty");
        
        String testJson = objectMapper.writeValueAsString(testFaculty);

        mockMvc.perform(patch("/faculties/{id}", testId)
                .content(testJson).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(testJson));        
        
        verify(facultyService).update(testFaculty);
    }

    @Test
    void shouldDeleteFaculty() throws Exception {
        int testId = 2;
        
        String expectedResult = "Faculty with id: " + testId + " was deleted.";

        mockMvc.perform(delete("/faculties/{id}", testId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(content()
                .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(expectedResult));
        
        verify(facultyService).deleteById(testId);
    }

    @Test
    void shouldReturnError500WhenRepositoryExceptionWhileGetFaculties() throws Exception {
        when(facultyService.getAll()).thenThrow(new ServiceException("Service exception", new RepositoryException()));

        mockMvc.perform(get("/faculties")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError());
        
        verify(facultyService).getAll();
    }

    @Test
    void shouldReturnError404WhenServiceExceptionWhileGetFaculties() throws Exception {
        when(facultyService.getAll()).thenThrow(ServiceException.class);

        mockMvc.perform(get("/faculties")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
        
        verify(facultyService).getAll();
    }

    @Test
    void shouldReturnError500WhenRepositoryExceptionWhileGetFaculty() throws Exception {
        int id = 2;

        when(facultyService.getById(id)).thenThrow(new ServiceException("Service exception", new RepositoryException()));

        mockMvc.perform(get("/faculties/{id}", id)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError());
        
        verify(facultyService).getById(id);
    }

    @Test
    void shouldReturnError400WhenIllegalArgumentExceptionWhileGetFaculty() throws Exception {
        int id = 5;

        when(facultyService.getById(id)).thenThrow(new ServiceException("Service exception", new IllegalArgumentException()));
        
        mockMvc.perform(get("/faculties/{id}", id)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());

        verify(facultyService).getById(id);
    }

    @Test
    void shouldReturnError404WhenEntityIsNotFoundWhileGetFaculty() throws Exception {
        int id = 1;
        when(facultyService.getById(id)).thenThrow(ServiceException.class);
        
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
        testFaculty.setName("Test faculty");
        
        String testJson = objectMapper.writeValueAsString(testFaculty);

        doThrow(new ServiceException("Service exception", new ConstraintViolationException(null))).when(facultyService).create(testFaculty);

        mockMvc.perform(post("/faculties")
                .content(testJson).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

        verify(facultyService).create(testFaculty);
    }

    @Test
    void shouldReturnError400WhenIllegalArgumentExceptionWhileCreateFaculty() throws Exception {
        Faculty testFaculty = new Faculty();
        testFaculty.setName("Test faculty");
        
        String testJson = objectMapper.writeValueAsString(testFaculty);

        doThrow(new ServiceException("Service exception", new IllegalArgumentException())).when(facultyService).create(testFaculty);

        mockMvc.perform(post("/faculties")
                .content(testJson).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

        verify(facultyService).create(testFaculty);
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
        testFaculty.setName("Test faculty");
        
        String testJson = objectMapper.writeValueAsString(testFaculty);

        doThrow(new ServiceException("Service exception", new ConstraintViolationException(null))).when(facultyService).update(testFaculty);

        mockMvc.perform(patch("/faculties/{id}", testId)
                .content(testJson).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

        verify(facultyService).update(testFaculty);
    }

    @Test
    void shouldReturnError400WhenIllegalArgumentExceptionWhileUpdateFaculty() throws Exception {
        int testId = 10;
        Faculty testFaculty = new Faculty();
        testFaculty.setId(testId);
        testFaculty.setName("Test faculty");
        
        String testJson = objectMapper.writeValueAsString(testFaculty);

        doThrow(new ServiceException("Service exception", new IllegalArgumentException())).when(facultyService).update(testFaculty);

        mockMvc.perform(patch("/faculties/{id}", testId)
                .content(testJson).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

        verify(facultyService).update(testFaculty);
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

        doThrow(new ServiceException("Service exception", new RepositoryException())).when(facultyService).deleteById(testId);

        mockMvc.perform(delete("/faculties/{id}", testId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError());

        verify(facultyService).deleteById(testId);
    }

    @Test
    void shouldReturnError400WhenIllegalArgumentExceptionWhileDeleteFaculty() throws Exception {
        int testId = 14;

        doThrow(new ServiceException("Service exception", new IllegalArgumentException())).when(facultyService).deleteById(testId);

        mockMvc.perform(delete("/faculties/{id}", testId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());

        verify(facultyService).deleteById(testId);
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
