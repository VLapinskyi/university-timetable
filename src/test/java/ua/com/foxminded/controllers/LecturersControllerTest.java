package ua.com.foxminded.controllers;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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

import javax.persistence.QueryTimeoutException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import javax.validation.ConstraintViolationException;

import ua.com.foxminded.controllers.aspects.GeneralControllerAspect;
import ua.com.foxminded.domain.Gender;
import ua.com.foxminded.domain.Lecturer;
import ua.com.foxminded.repositories.exceptions.RepositoryException;
import ua.com.foxminded.service.LecturerService;
import ua.com.foxminded.service.exceptions.ServiceException;

@ExtendWith(SpringExtension.class)
@WebMvcTest(LecturersController.class)
@Import({AopAutoConfiguration.class, GeneralControllerAspect.class})
class LecturersControllerTest {

    @Autowired
    private LecturersController lecturersController;

    @MockBean
    private LecturerService lecturerService;

    @Autowired
    private MockMvc mockMvc;

    private RepositoryException repositoryException = new RepositoryException("repository exception",
            new QueryTimeoutException("Exception message"));
    private ServiceException serviceWithRepositoryException = new ServiceException("Service exception", repositoryException);

    private ServiceException serviceWithIllegalArgumentException = new ServiceException("Service exception",
            new IllegalArgumentException());

    private ServiceException serviceWithConstraintViolationException = new ServiceException("Service exception",
            new ConstraintViolationException(null));
    
    @BeforeEach
    void setUp() throws Exception {
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

        mockMvc.perform(get("/lecturers")).andExpect(status().isOk()).andExpect(view().name("lecturers/lecturers"))
                .andExpect(model().attribute("pageTitle", equalTo("Lecturers")))
                .andExpect(model().attribute("lecturers", hasSize(2)))
                .andExpect(model().attribute("lecturers",
                        hasItem(allOf(hasProperty("id", is(1)), hasProperty("firstName", is("Ivan")),
                                hasProperty("lastName", is("Ivanov")), hasProperty("gender", equalTo(Gender.MALE)),
                                hasProperty("email", is("ivanovivan@test.com")),
                                hasProperty("phoneNumber", is("+380123456789"))

                        ))))
                .andExpect(model().attribute("lecturers",
                        hasItem(allOf(hasProperty("id", is(2)), hasProperty("firstName", is("Vasyl")),
                                hasProperty("lastName", is("Vasyliev")), hasProperty("gender", equalTo(Gender.MALE)),
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

        mockMvc.perform(get("/lecturers/{id}", id)).andExpect(status().isOk())
                .andExpect(view().name("lecturers/lecturer"))
                .andExpect(
                        model().attribute("pageTitle", equalTo(lecturer.getFirstName() + " " + lecturer.getLastName())))
                .andExpect(model().attribute("lecturer", hasProperty("id", is(id))))
                .andExpect(model().attribute("lecturer", hasProperty("firstName", equalTo(lecturer.getFirstName()))))
                .andExpect(model().attribute("lecturer", hasProperty("lastName", equalTo(lecturer.getLastName()))))
                .andExpect(model().attribute("lecturer", hasProperty("gender", equalTo(Gender.MALE))))
                .andExpect(model().attribute("lecturer", hasProperty("email", equalTo("petrovpetro@test.com"))))
                .andExpect(model().attribute("lecturer", hasProperty("phoneNumber", equalTo("+380784512369"))));

        verify(lecturerService).getById(id);
    }
    
    @Test
    void shouldGenerateRightPageWhenNewLecturer() throws Exception {
        mockMvc.perform(get("/lecturers/new"))
        .andExpect(status().isOk())
        .andExpect(view().name("lecturers/new"))
        .andExpect(model().attribute("pageTitle", "Create a new lecturer"))
        .andExpect(model().attribute("genders", equalTo(Gender.values())));
    }
    
    @Test
    void shouldCreateLecturer() throws Exception {
        Lecturer lecturer = new Lecturer();
        lecturer.setFirstName("Petro");
        lecturer.setLastName("Petrov");
        lecturer.setGender(Gender.MALE);
        lecturer.setPhoneNumber("+380967845123");
        lecturer.setEmail("ppetrov@test.com");
        
        mockMvc.perform(post("/lecturers").flashAttr("lecturer", lecturer)
                .param("gender-value", Gender.MALE.toString()))
        .andExpect(view().name("redirect:/lecturers")).andExpect(status().is3xxRedirection());
        
        verify(lecturerService).create(lecturer);
    }
    
    @Test
    void shouldGenerateRightPageWhenEditLecturer() throws Exception {
        int testId = 5;
        Lecturer lecturer = new Lecturer();
        lecturer.setId(testId);
        lecturer.setFirstName("Vasyl");
        lecturer.setLastName("Vasylov");
        lecturer.setGender(Gender.MALE);
        lecturer.setPhoneNumber("+380786542321");
        lecturer.setEmail("VVasylov@test.com");
        
        when(lecturerService.getById(testId)).thenReturn(lecturer);
        
        mockMvc.perform(get("/lecturers/{id}/edit", testId))
        .andExpect(view().name("lecturers/edit"))
        .andExpect(model().attribute("pageTitle", equalTo("Edit " + lecturer.getFirstName() + " " + lecturer.getLastName())))
        .andExpect(model().attribute("lecturer", equalTo(lecturer)))
        .andExpect(model().attribute("genders", Arrays.asList(Gender.FEMALE)))
        .andExpect(status().isOk());
        
        verify(lecturerService).getById(testId);
        
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
        
        mockMvc.perform(patch("/lecturers/{id}", testId).flashAttr("lecturer", lecturer)
                .param("gender-value", Gender.MALE.toString()))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/lecturers"));
        
        verify(lecturerService).update(lecturer);
    }
    
    @Test
    void shouldDeleteLecturer() throws Exception {
        int testId = 6;
        
        mockMvc.perform(delete("/lecturers/{id}", testId))
        .andExpect(view().name("redirect:/lecturers"))
        .andExpect(status().is3xxRedirection());
        
        verify(lecturerService).deleteById(testId);
    }

    @Test
    void sholdReturnError500WhenRepositoryExceptionWhileGetLecturers() throws Exception {
        when(lecturerService.getAll()).thenThrow(serviceWithRepositoryException);

        mockMvc.perform(get("/lecturers")).andExpect(status().isInternalServerError());
        verify(lecturerService).getAll();
    }

    @Test
    void shouldReturnError404WhenServiceExceptionWhileGetLessons() throws Exception {
        when(lecturerService.getAll()).thenThrow(ServiceException.class);

        mockMvc.perform(get("/lecturers")).andExpect(status().isNotFound());
        verify(lecturerService).getAll();
    }

    @Test
    void shouldReturnError500WhenRepositoryExceptionWhileGetLecturer() throws Exception {
        int id = 2;

        when(lecturerService.getById(id)).thenThrow(serviceWithRepositoryException);

        mockMvc.perform(get("/lecturers/{id}", id)).andExpect(status().isInternalServerError());
        verify(lecturerService).getById(id);
    }

    @Test
    void shouldReturnError400WhenIllegalArgumentExceptionWhileGetLecturer() throws Exception {
        int id = 6;
        when(lecturerService.getById(id)).thenThrow(serviceWithIllegalArgumentException);
        mockMvc.perform(get("/lecturers/{id}", id)).andExpect(status().isBadRequest());
        verify(lecturerService).getById(id);
    }

    @Test
    void shouldReturnError404WhenEntityIsNotFoundWhileGetLecturer() throws Exception {
        int id = 5;

        when(lecturerService.getById(id)).thenThrow(ServiceException.class);
        mockMvc.perform(get("/lecturers/{id}", id)).andExpect(status().isNotFound());
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
        
        doThrow(serviceWithRepositoryException).when(lecturerService).create(lecturer);
        
        mockMvc.perform(post("/lecturers").flashAttr("lecturer", lecturer)
                .param("gender-value", Gender.FEMALE.toString()))
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
        
        doThrow(serviceWithConstraintViolationException).when(lecturerService).create(lecturer);
        
        mockMvc.perform(post("/lecturers").flashAttr("lecturer", lecturer)
                .param("gender-value", Gender.MALE.toString()))
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
        
        doThrow(serviceWithIllegalArgumentException).when(lecturerService).create(lecturer);
        
        mockMvc.perform(post("/lecturers").flashAttr("lecturer", lecturer)
                .param("gender-value", Gender.MALE.toString()))
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
        
        doThrow(ServiceException.class).when(lecturerService).create(lecturer);
        
        mockMvc.perform(post("/lecturers").flashAttr("lecturer", lecturer)
                .param("gender-value", Gender.FEMALE.toString()))
        .andExpect(status().isInternalServerError());
        
        verify(lecturerService).create(lecturer);
    }
    
    @Test
    void shouldReturnError500WhenRepositoryExceptionWhileEdit() throws Exception {
        int testId = 13;
        
        doThrow(serviceWithRepositoryException).when(lecturerService).getById(testId);
        
        mockMvc.perform(get("/lecturers/{id}/edit", testId))
        .andExpect(status().isInternalServerError());
        
        verify(lecturerService).getById(testId);
    }
    
    @Test
    void shouldReturnError500WhenServiceExceptionWhileEdit() throws Exception {
        int testId = 4;
        
        doThrow(ServiceException.class).when(lecturerService).getById(testId);
        
        mockMvc.perform(get("/lecturers/{id}/edit", testId))
        .andExpect(status().isInternalServerError());
        
        verify(lecturerService).getById(testId);
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
        
        doThrow(serviceWithRepositoryException).when(lecturerService).update(lecturer);
        
        mockMvc.perform(patch("/lecturers/{id}", testId).flashAttr("lecturer", lecturer)
                .param("gender-value", Gender.MALE.toString()))
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
        
        doThrow(serviceWithConstraintViolationException).when(lecturerService).update(lecturer);
        
        mockMvc.perform(patch("/lecturers/{id}", testId).flashAttr("lecturer", lecturer)
                .param("gender-value", Gender.MALE.toString()))
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
        
        doThrow(serviceWithIllegalArgumentException).when(lecturerService).update(lecturer);
        
        mockMvc.perform(patch("/lecturers/{id}", testId).flashAttr("lecturer", lecturer)
                .param("gender-value", Gender.MALE.toString()))
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
        
        doThrow(ServiceException.class).when(lecturerService).update(lecturer);
        
        mockMvc.perform(patch("/lecturers/{id}", testId).flashAttr("lecturer", lecturer)
                .param("gender-value", Gender.MALE.toString()))
        .andExpect(status().isInternalServerError());
        
        verify(lecturerService).update(lecturer);
    }
    
    @Test
    void shouldReturnError500WhenRepositoryExceptionWhileDelete() throws Exception {
        int testId = 12;
        
        doThrow(serviceWithRepositoryException).when(lecturerService).deleteById(testId);
        
        mockMvc.perform(delete("/lecturers/{id}", testId))
        .andExpect(status().isInternalServerError());
        
        verify(lecturerService).deleteById(testId);
    }
    
    @Test
    void shouldReturnError400WhenIllegalArgumentExceptionWhileDelete() throws Exception {
        int testId = 43;
        doThrow(serviceWithIllegalArgumentException).when(lecturerService).deleteById(testId);
        
        mockMvc.perform(delete("/lecturers/{id}", testId))
        .andExpect(status().isBadRequest());
        
        verify(lecturerService).deleteById(testId);
    }
    
    @Test
    void shouldReturnError500WhenServiceExceptionWhileDelete() throws Exception {
        int testId = 134;
        
        doThrow(ServiceException.class).when(lecturerService).deleteById(testId);
        
        mockMvc.perform(delete("/lecturers/{id}", testId))
        .andExpect(status().isInternalServerError());
        
        verify(lecturerService).deleteById(testId);
    }
}
