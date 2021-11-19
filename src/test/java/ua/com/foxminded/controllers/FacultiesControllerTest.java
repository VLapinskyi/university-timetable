package ua.com.foxminded.controllers;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import javax.validation.ConstraintViolationException;

import ua.com.foxminded.controllers.aspects.GeneralControllerAspect;
import ua.com.foxminded.domain.Faculty;
import ua.com.foxminded.repositories.exceptions.RepositoryException;
import ua.com.foxminded.service.FacultyService;
import ua.com.foxminded.service.exceptions.ServiceException;

@WebMvcTest(FacultiesController.class)
@Import({AopAutoConfiguration.class, GeneralControllerAspect.class})
class FacultiesControllerTest {

    @Autowired
    private FacultiesController facultiesController;

    @MockBean
    private FacultyService facultyService;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void init() {
        ReflectionTestUtils.setField(facultiesController, "facultyService", facultyService);
    }

    @Test
    void shouldAddToModelListWhenGetFaculties() throws Exception {
        Faculty firstFaculty = new Faculty();
        firstFaculty.setId(1);
        firstFaculty.setName("First faculty");

        Faculty secondFaculty = new Faculty();
        secondFaculty.setId(2);
        secondFaculty.setName("Second faculty");

        when(facultyService.getAll()).thenReturn(Arrays.asList(firstFaculty, secondFaculty));

        mockMvc.perform(get("/faculties")).andExpect(status().isOk()).andExpect(view().name("faculties/faculties"))
                .andExpect(model().attribute("pageTitle", equalTo("Faculties")))
                .andExpect(model().attribute("faculties", hasSize(2)))
                .andExpect(model().attribute("faculties",
                        hasItem(allOf(hasProperty("id", is(1)), hasProperty("name", is("First faculty"))))))
                .andExpect(model().attribute("faculties",
                        hasItem(allOf(hasProperty("id", is(2)), hasProperty("name", is("Second faculty"))))));

        verify(facultyService).getAll();
    }

    @Test
    void shouldAddToModelFoundedEntityWhenGetFaculty() throws Exception {
        int id = 1;
        Faculty firstFaculty = new Faculty();
        firstFaculty.setId(id);
        firstFaculty.setName("First faculty");

        when(facultyService.getById(id)).thenReturn(firstFaculty);

        mockMvc.perform(get("/faculties/{id}", id)).andExpect(status().isOk())
                .andExpect(view().name("faculties/faculty"))
                .andExpect(model().attribute("pageTitle", equalTo(firstFaculty.getName())))
                .andExpect(model().attribute("faculty", hasProperty("id", is(id))))
                .andExpect(model().attribute("faculty", hasProperty("name", is("First faculty"))));

        verify(facultyService).getById(1);
    }

    @Test
    void shouldGenerateRightPageWhenNewFaculty() throws Exception {
        mockMvc.perform(get("/faculties/new")).andExpect(status().isOk()).andExpect(view().name("faculties/new"))
                .andExpect(model().attribute("pageTitle", "Create a new faculty"));
    }

    @Test
    void shouldCreateFaculty() throws Exception {
        Faculty testFaculty = new Faculty();
        testFaculty.setName("Test faculty");
        mockMvc.perform(post("/faculties").flashAttr("faculty", testFaculty))
                .andExpect(view().name("redirect:/faculties")).andExpect(status().is3xxRedirection());
        verify(facultyService).create(testFaculty);
    }

    @Test
    void shouldGenerateRightPageWhenEditFaculty() throws Exception {
        int testId = 1;
        Faculty testFaculty = new Faculty();
        testFaculty.setId(testId);
        testFaculty.setName("Test faculty");

        when(facultyService.getById(testId)).thenReturn(testFaculty);

        mockMvc.perform(get("/faculties/{id}/edit", testId)).andExpect(view().name("faculties/edit"))
                .andExpect(model().attribute("pageTitle", equalTo("Edit " + testFaculty.getName())))
                .andExpect(model().attribute("faculty", hasProperty("id", is(testId))))
                .andExpect(model().attribute("faculty", hasProperty("name", is(testFaculty.getName()))));

        verify(facultyService).getById(testId);
        
    }

    @Test
    void shouldUpdateFaculty() throws Exception {
        int testId = 10;
        Faculty testFaculty = new Faculty();
        testFaculty.setId(testId);
        testFaculty.setName("Test faculty");

        mockMvc.perform(patch("/faculties/{id}", testId).flashAttr("faculty", testFaculty))
                .andExpect(view().name("redirect:/faculties")).andExpect(status().is3xxRedirection());

        verify(facultyService).update(testFaculty);
    }

    @Test
    void shouldDeleteFaculty() throws Exception {
        int testId = 2;

        mockMvc.perform(delete("/faculties/{id}", testId)).andExpect(view().name("redirect:/faculties"))
                .andExpect(status().is3xxRedirection());

        verify(facultyService).deleteById(testId);
    }

    @Test
    void shouldReturnError500WhenRepositoryExceptionWhileGetFaculties() throws Exception {
        when(facultyService.getAll()).thenThrow(new ServiceException("message", new RepositoryException()));

        mockMvc.perform(get("/faculties")).andExpect(status().isInternalServerError());
        verify(facultyService).getAll();
    }

    @Test
    void shouldReturnError404WhenServiceExceptionWhileGetFaculties() throws Exception {
        when(facultyService.getAll()).thenThrow(ServiceException.class);

        mockMvc.perform(get("/faculties")).andExpect(status().isNotFound());
        verify(facultyService).getAll();
    }

    @Test
    void shouldReturnError500WhenRepositoryExceptionWhileGetFaculty() throws Exception {
        int id = 2;

        when(facultyService.getById(id)).thenThrow(new ServiceException("message", new RepositoryException()));

        mockMvc.perform(get("/faculties/{id}", id)).andExpect(status().isInternalServerError());
        verify(facultyService).getById(id);
    }

    @Test
    void shouldReturnError400WhenIllegalArgumentExceptionWhileGetFaculty() throws Exception {
        int id = 5;

        when(facultyService.getById(id)).thenThrow(new ServiceException("Service exception",
                new IllegalArgumentException()));
        mockMvc.perform(get("/faculties/{id}", id)).andExpect(status().isBadRequest());

        verify(facultyService).getById(id);
    }

    @Test
    void shouldReturnError404WhenEntityIsNotFoundWhileGetFaculty() throws Exception {
        int id = 1;
        when(facultyService.getById(id)).thenThrow(ServiceException.class);
        mockMvc.perform(get("/faculties/{id}", id)).andExpect(status().isNotFound());
        verify(facultyService).getById(id);
    }
    
    @Test
    void shouldReturnError500WhenRepositoryExceptionWhileCreateFaculty() throws Exception {
        Faculty testFaculty = new Faculty();
        testFaculty.setName("Test faculty");
        
        doThrow(new ServiceException("message", new RepositoryException())).when(facultyService).create(testFaculty);

        mockMvc.perform(post("/faculties").flashAttr("faculty", testFaculty))
            .andExpect(status().isInternalServerError());
        
        verify(facultyService).create(testFaculty);
    }
    
    @Test
    void shouldReturnError400WhenConstraintViolationExceptionWhileCreateFaculty() throws Exception {
        Faculty testFaculty = new Faculty();
        testFaculty.setName("Test faculty");
        
        doThrow(new ServiceException("Service exception", new ConstraintViolationException(null)))
            .when(facultyService).create(testFaculty);

        mockMvc.perform(post("/faculties").flashAttr("faculty", testFaculty))
            .andExpect(status().isBadRequest());
        
        verify(facultyService).create(testFaculty);
    }
    
    @Test
    void shouldReturnError400WhenIllegalArgumentExceptionWhileCreateFaculty() throws Exception {
        Faculty testFaculty = new Faculty();
        testFaculty.setName("Test faculty");
        
        doThrow(new ServiceException("Service exception", new IllegalArgumentException()))
            .when(facultyService).create(testFaculty);

        mockMvc.perform(post("/faculties").flashAttr("faculty", testFaculty))
            .andExpect(status().isBadRequest());
        
        verify(facultyService).create(testFaculty);
    }
    
    @Test
    void shouldReturnError500WhenServiceExceptionWhileCreateFaculty() throws Exception {
        Faculty testFaculty = new Faculty();
        testFaculty.setName("Test faculty");
        
        doThrow(ServiceException.class).when(facultyService).create(testFaculty);

        mockMvc.perform(post("/faculties").flashAttr("faculty", testFaculty))
            .andExpect(status().isInternalServerError());
        
        verify(facultyService).create(testFaculty);
    }
    
    @Test
    void shouldReturnError500WhenRepositoryExceptionWhileEditFaculty() throws Exception {
        int testId = 9;
        
        doThrow(new ServiceException("message", new RepositoryException())).when(facultyService).getById(testId);
        
        mockMvc.perform(get("/faculties/{id}/edit", testId))
        .andExpect(status().isInternalServerError());
        
        verify(facultyService).getById(testId);
        
    }
    
    @Test
    void shouldReturnError500WhenServiceExceptionWhileEditFaculty() throws Exception {
        int testId = 21;
        
        doThrow(ServiceException.class).when(facultyService).getById(testId);
        
        mockMvc.perform(get("/faculties/{id}/edit", testId))
        .andExpect(status().isInternalServerError());
        
        verify(facultyService).getById(testId);
    }
    
    @Test
    void shouldReturnError500WhenRepositoryExceptionWhileUpdateFaculty() throws Exception {
        int testId = 4;
        Faculty testFaculty = new Faculty();
        testFaculty.setId(testId);
        testFaculty.setName("Test faculty");
        
        doThrow(new ServiceException("message", new RepositoryException())).when(facultyService).update(testFaculty);

        mockMvc.perform(patch("/faculties/{id}", testId).flashAttr("faculty", testFaculty))
            .andExpect(status().isInternalServerError());
        
        verify(facultyService).update(testFaculty);
    }
    
    @Test
    void shouldReturnError400WhenConstraintViolationExceptionWhileUpdateFaculty() throws Exception {
        int testId = 8;
        Faculty testFaculty = new Faculty();
        testFaculty.setId(testId);
        testFaculty.setName("Test faculty");
        
        doThrow(new ServiceException("Service exception", new ConstraintViolationException(null)))
            .when(facultyService).update(testFaculty);

        mockMvc.perform(patch("/faculties/{id}", testId).flashAttr("faculty", testFaculty))
            .andExpect(status().isBadRequest());
        
        verify(facultyService).update(testFaculty);
    }
    
    @Test
    void shouldReturnError400WhenIllegalArgumentExceptionWhileUpdateFaculty() throws Exception {
        int testId = 10;
        Faculty testFaculty = new Faculty();
        testFaculty.setId(testId);
        testFaculty.setName("Test faculty");
        
        doThrow(new ServiceException("Service exception", new IllegalArgumentException()))
            .when(facultyService).update(testFaculty);

        mockMvc.perform(patch("/faculties/{id}", testId).flashAttr("faculty", testFaculty))
            .andExpect(status().isBadRequest());
        
        verify(facultyService).update(testFaculty);
    }
    
    @Test
    void shouldReturnError500WhenServiceExceptionWhileUpdateFaculty() throws Exception {
        int testId = 10;
        Faculty testFaculty = new Faculty();
        testFaculty.setId(testId);
        testFaculty.setName("Test faculty");
        
        doThrow(ServiceException.class).when(facultyService).update(testFaculty);

        mockMvc.perform(patch("/faculties/{id}", testId).flashAttr("faculty", testFaculty))
            .andExpect(status().isInternalServerError());
        
        verify(facultyService).update(testFaculty);
    }
    
    @Test
    void shouldReturnError500WhenRepositoryExceptionWhileDeleteFaculty() throws Exception {
        int testId = 75;
        
        doThrow(new ServiceException("message", new RepositoryException())).when(facultyService).deleteById(testId);

        mockMvc.perform(delete("/faculties/{id}", testId))
            .andExpect(status().isInternalServerError());
        
        verify(facultyService).deleteById(testId);
    }
    
    @Test
    void shouldReturnError400WhenIllegalArgumentExceptionWhileDeleteFaculty() throws Exception {
        int testId = 14;
        
        doThrow(new ServiceException("Service exception", new IllegalArgumentException()))
            .when(facultyService).deleteById(testId);

        mockMvc.perform(delete("/faculties/{id}", testId))
            .andExpect(status().isBadRequest());
        
        verify(facultyService).deleteById(testId);
    }
    
    @Test
    void shouldReturnError500WhenServiceExceptionWhileDeleteFaculty() throws Exception {
        int testId = 41;
        
        doThrow(ServiceException.class).when(facultyService).deleteById(testId);

        mockMvc.perform(delete("/faculties/{id}", testId))
            .andExpect(status().isInternalServerError());
        
        verify(facultyService).deleteById(testId);
    }
}
