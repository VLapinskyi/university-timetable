package ua.com.foxminded.controllers;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import ua.com.foxminded.dao.exceptions.DAOException;
import ua.com.foxminded.domain.Gender;
import ua.com.foxminded.domain.Lecturer;
import ua.com.foxminded.service.LecturerService;
import ua.com.foxminded.service.exceptions.ServiceException;
import ua.com.foxminded.settings.SpringConfiguration;

@ContextConfiguration(classes = {SpringConfiguration.class})
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
class LecturersControllerTest {
    
    @Autowired
    private WebApplicationContext webApplicationContext;
    
    @Autowired
    private LecturersController lecturersController;
    
    @Mock
    private LecturerService lecturerService;
    
    private MockMvc mockMvc;
    
    private DAOException daoException = new DAOException("DAO exception", new QueryTimeoutException("Exception message"));
    private ServiceException serviceWithDAOException = new ServiceException("Service exception", daoException);
    
    private ServiceException serviceWithIllegalArgumentException = new ServiceException("Service exception", new IllegalArgumentException());
    
    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(lecturersController, "lecturerService", lecturerService);
    }

    @Test
    void shouldAddToModelListWhenGetLecturers() throws Exception {
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
        
        when(lecturerService.getAll()).thenReturn(Arrays.asList(firstLecturer, secondLecturer));
        
        mockMvc.perform(get("/lecturers"))
            .andExpect(status().isOk())
            .andExpect(view().name("lecturers/lecturers"))
            .andExpect(model().attribute("pageTitle", equalTo("Lecturers")))
            .andExpect(model().attribute("lecturers", hasSize(2)))
            .andExpect(model().attribute("lecturers", hasItem(
                    allOf(
                            hasProperty("id", is(1)),
                            hasProperty("firstName", is("Ivan")),
                            hasProperty("lastName", is("Ivanov")),
                            hasProperty("gender", equalTo(Gender.MALE)),
                            hasProperty("email", is("ivanovivan@test.com")),
                            hasProperty("phoneNumber", is("+380123456789"))
                            
                    ))))
            .andExpect(model().attribute("lecturers", hasItem(
                    allOf(
                            hasProperty("id", is(2)),
                            hasProperty("firstName", is("Vasyl")),
                            hasProperty("lastName", is("Vasyliev")),
                            hasProperty("gender", equalTo(Gender.MALE)),
                            hasProperty("email", is("vasylievvasyl@test.com")),
                            hasProperty("phoneNumber", is("+380987654321"))
                            
                    ))));
        
        verify(lecturerService).getAll();
    }
    
    @Test
    void shouldAddToModelFoundedEntityWhenGetLecturer() throws Exception {
        int id = 3;
        
        Lecturer lecturer = new Lecturer();
        lecturer.setId(id);
        lecturer.setFirstName("Petro");
        lecturer.setLastName("Petrov");
        lecturer.setGender(Gender.MALE);
        lecturer.setEmail("petrovpetro@test.com");
        lecturer.setPhoneNumber("+380784512369");
        
        when(lecturerService.getById(id)).thenReturn(lecturer);
        
        mockMvc.perform(get("/lecturers/{id}", id))
            .andExpect(status().isOk())
            .andExpect(view().name("lecturers/lecturer"))
            .andExpect(model().attribute("pageTitle", equalTo(lecturer.getFirstName() + " " + lecturer.getLastName())))
            .andExpect(model().attribute("lecturer", hasProperty("id", is(id))))
            .andExpect(model().attribute("lecturer", hasProperty("firstName", equalTo(lecturer.getFirstName()))))
            .andExpect(model().attribute("lecturer", hasProperty("lastName", equalTo(lecturer.getLastName()))))
            .andExpect(model().attribute("lecturer", hasProperty("gender", equalTo(Gender.MALE))))
            .andExpect(model().attribute("lecturer", hasProperty("email", equalTo("petrovpetro@test.com"))))
            .andExpect(model().attribute("lecturer", hasProperty("phoneNumber", equalTo("+380784512369"))));
        
        verify(lecturerService).getById(id);
    }
    
    @Test
    void sholdReturnError500WhenDAOExceptionWhileGetLecturers() throws Exception {
        when(lecturerService.getAll()).thenThrow(serviceWithDAOException);
        
        mockMvc.perform(get("/lecturers"))
            .andExpect(status().isInternalServerError());
        verify(lecturerService).getAll();
    }
    
    @Test
    void shouldReturnError404WhenServiceExceptionWhileGetLessons() throws Exception {
        when(lecturerService.getAll()).thenThrow(ServiceException.class);
        
        mockMvc.perform(get("/lecturers"))
            .andExpect(status().isNotFound());
        verify(lecturerService).getAll();
    }
    
    @Test
    void shouldReturnError500WhenDAOExceptionWhileGetLecturer() throws Exception {
        int id = 2;
        
        when(lecturerService.getById(id)).thenThrow(serviceWithDAOException);
        
        mockMvc.perform(get("/lecturers/{id}", id))
            .andExpect(status().isInternalServerError());
        verify(lecturerService).getById(id);
    }
    
    @Test
    void shouldReturnError400WhenIllegalArgumentExceptionWhileGetLecturer() throws Exception {
        int id = 6;
        when(lecturerService.getById(id)).thenThrow(serviceWithIllegalArgumentException);
        mockMvc.perform(get("/lecturers/{id}", id))
            .andExpect(status().isBadRequest());
        verify(lecturerService).getById(id);
    }
    
    @Test
    void shouldReturnError404WhenEntityIsNotFoundWhileGetLecturer() throws Exception {
        int id = 5;
        
        when(lecturerService.getById(id)).thenThrow(ServiceException.class);
        mockMvc.perform(get("/lecturers/{id}", id))
            .andExpect(status().isNotFound());
        verify(lecturerService).getById(id);
    }
}
