package ua.com.foxminded.api.unit;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

import javax.validation.ConstraintViolationException;

import ua.com.foxminded.api.LecturersRestController;
import ua.com.foxminded.api.aspects.GeneralRestControllerAspect;
import ua.com.foxminded.domain.Gender;
import ua.com.foxminded.domain.Lecturer;
import ua.com.foxminded.repositories.exceptions.RepositoryException;
import ua.com.foxminded.service.LecturerService;
import ua.com.foxminded.service.exceptions.ServiceException;

@WebMvcTest(LecturersRestController.class)
@Import({AopAutoConfiguration.class, GeneralRestControllerAspect.class})
class LecturersRestControllerTest {

    @Autowired
    private LecturersRestController lecturersRestController;

    @MockBean
    private LecturerService lecturerService;
    
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;
    
    @BeforeEach
    void setUp() throws Exception {
        ReflectionTestUtils.setField(lecturersRestController, "lecturerService", lecturerService);
    }

    @Test
    void shouldGetLecturers() throws Exception {
        Lecturer firstLecturer = new Lecturer();
        firstLecturer.setId(1);
        firstLecturer.setFirstName("Ivan");
        firstLecturer.setLastName("Ivanov");
        firstLecturer.setGender(Gender.MALE);
        firstLecturer.setEmail("ivanovivan@test.com");
        firstLecturer.setPhoneNumber("+380123456789");

        Lecturer secondLecturer = new Lecturer();
        secondLecturer.setId(2);
        secondLecturer.setFirstName("Vasyl");
        secondLecturer.setLastName("Vasyliev");
        secondLecturer.setGender(Gender.MALE);
        secondLecturer.setEmail("vasylievvasyl@test.com");
        secondLecturer.setPhoneNumber("+380987654321");
        
        List<Lecturer> lecturers = new ArrayList<>(Arrays.asList(firstLecturer, secondLecturer));

        when(lecturerService.getAll()).thenReturn(lecturers);
        
        String expectedResult = objectMapper.writeValueAsString(lecturers);

        mockMvc.perform(get("/lecturers"))
            .andExpect(status().isOk())
            .andExpect(content()
                    .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedResult));

        verify(lecturerService).getAll();
    }

    @Test
    void shouldWhenGetLecturer() throws Exception {
        int id = 3;

        Lecturer lecturer = new Lecturer();
        lecturer.setId(id);
        lecturer.setFirstName("Petro");
        lecturer.setLastName("Petrov");
        lecturer.setGender(Gender.MALE);
        lecturer.setEmail("petrovpetro@test.com");
        lecturer.setPhoneNumber("+380784512369");

        when(lecturerService.getById(id)).thenReturn(lecturer);
        
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
        
        verify(lecturerService).create(lecturer);
    }
    
    @Test
    void shouldUpdateLecturer() throws Exception {
        int testId = 7;
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
    void shouldDeleteLecturer() throws Exception {
        int testId = 6;
        
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
    void sholdReturnError500WhenRepositoryExceptionWhileGetLecturers() throws Exception {
        when(lecturerService.getAll()).thenThrow(new ServiceException("Service exception", new RepositoryException()));

        mockMvc.perform(get("/lecturers")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError());
        
        verify(lecturerService).getAll();
    }

    @Test
    void shouldReturnError404WhenServiceExceptionWhileGetLessons() throws Exception {
        when(lecturerService.getAll()).thenThrow(ServiceException.class);

        mockMvc.perform(get("/lecturers")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
        
        verify(lecturerService).getAll();
    }

    @Test
    void shouldReturnError500WhenRepositoryExceptionWhileGetLecturer() throws Exception {
        int id = 2;

        when(lecturerService.getById(id)).thenThrow(new ServiceException("Service exception", new RepositoryException()));

        mockMvc.perform(get("/lecturers/{id}", id)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError());
        
        verify(lecturerService).getById(id);
    }

    @Test
    void shouldReturnError400WhenIllegalArgumentExceptionWhileGetLecturer() throws Exception {
        int id = 6;
        when(lecturerService.getById(id)).thenThrow(new ServiceException("Service exception", new IllegalArgumentException()));
        
        mockMvc.perform(get("/lecturers/{id}", id)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
        
        verify(lecturerService).getById(id);
    }

    @Test
    void shouldReturnError404WhenEntityIsNotFoundWhileGetLecturer() throws Exception {
        int id = 5;

        when(lecturerService.getById(id)).thenThrow(ServiceException.class);
        
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
        lecturer.setFirstName("Roman");
        lecturer.setLastName("Kolomiiets");
        lecturer.setGender(Gender.MALE);
        lecturer.setPhoneNumber("+380234598741");
        lecturer.setEmail("RKolomiiets@test.com");
        
        String testJson = objectMapper.writeValueAsString(lecturer);
        
        doThrow(new ServiceException("Service exception", new ConstraintViolationException(null))).when(lecturerService).create(lecturer);
        
        mockMvc.perform(post("/lecturers")
                .content(testJson)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
        
        verify(lecturerService).create(lecturer);
    }
    
    @Test
    void shouldReturnError400IllegalArgumentExceptionWhileCreate() throws Exception {
        Lecturer lecturer = new Lecturer();
        lecturer.setFirstName("Serhii");
        lecturer.setLastName("Mazur");
        lecturer.setGender(Gender.MALE);
        lecturer.setPhoneNumber("+380216598741");
        lecturer.setEmail("SMazur@test.com");
        
        String testJson = objectMapper.writeValueAsString(lecturer);
        
        doThrow(new ServiceException("Service exception", new IllegalArgumentException())).when(lecturerService).create(lecturer);
        
        mockMvc.perform(post("/lecturers")
                .content(testJson)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
        
        verify(lecturerService).create(lecturer);
    }
    
    @Test
    void shouldReturnError500ServiceExceptionWhileCreate() throws Exception {
        Lecturer lecturer = new Lecturer();
        lecturer.setFirstName("Vita");
        lecturer.setLastName("Didenko");
        lecturer.setGender(Gender.FEMALE);
        lecturer.setPhoneNumber("+3805698741");
        lecturer.setEmail("VDidenko@test.com");
        
        String testJson = objectMapper.writeValueAsString(lecturer);
        
        doThrow(ServiceException.class).when(lecturerService).create(lecturer);
        
        mockMvc.perform(post("/lecturers")
                .content(testJson)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError());
        
        verify(lecturerService).create(lecturer);
    }
    
    @Test
    void shouldReturnError500WhenRepositoryExceptionWhileUpdate() throws Exception {
        int testId = 1;
        
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
        lecturer.setEmail("MBurlaka@test.com");
        
        String testJson = objectMapper.writeValueAsString(lecturer);
        
        doThrow(new ServiceException("Service exception", new ConstraintViolationException(null))).when(lecturerService).update(lecturer);
        
        mockMvc.perform(patch("/lecturers/{id}", testId)
                .content(testJson)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
        
        verify(lecturerService).update(lecturer);
    }
    
    @Test
    void shouldReturnError400WhenIllegalArgumentExceptionWhileUpdate() throws Exception {
        int testId = 9;
        
        Lecturer lecturer = new Lecturer();
        lecturer.setId(testId);
        lecturer.setFirstName("Oleksandr");
        lecturer.setLastName("Ostapovets");
        lecturer.setGender(Gender.MALE);
        lecturer.setPhoneNumber("+380974563214");
        lecturer.setEmail("OOstapovets@test.com");
        
        String testJson = objectMapper.writeValueAsString(lecturer);
        
        doThrow(new ServiceException("Service exception", new IllegalArgumentException())).when(lecturerService).update(lecturer);
        
        mockMvc.perform(patch("/lecturers/{id}", testId)
                .content(testJson)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
        
        verify(lecturerService).update(lecturer);
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
        
        doThrow(new ServiceException("Service exception", new RepositoryException())).when(lecturerService).deleteById(testId);
        
        mockMvc.perform(delete("/lecturers/{id}", testId)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError());
        
        verify(lecturerService).deleteById(testId);
    }
    
    @Test
    void shouldReturnError400WhenIllegalArgumentExceptionWhileDelete() throws Exception {
        int testId = 43;
        doThrow(new ServiceException("Service exception", new IllegalArgumentException())).when(lecturerService).deleteById(testId);
        
        mockMvc.perform(delete("/lecturers/{id}", testId)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
        
        verify(lecturerService).deleteById(testId);
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
