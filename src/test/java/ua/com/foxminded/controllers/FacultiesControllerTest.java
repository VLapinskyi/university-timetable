package ua.com.foxminded.controllers;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import ua.com.foxminded.domain.Faculty;
import ua.com.foxminded.service.FacultyService;
import ua.com.foxminded.settings.SpringConfiguration;

@ContextConfiguration(classes = {SpringConfiguration.class})
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
class FacultiesControllerTest {
    
    @Autowired
    private WebApplicationContext webApplicationContext;
    
    @Autowired
    private FacultiesController facultiesController;
    
    @Mock
    private FacultyService facultyService;

    private MockMvc mockMvc;

    @BeforeEach
    void init() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        MockitoAnnotations.openMocks(this);
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
        
        mockMvc.perform(get("/faculties"))
            .andExpect(status().isOk())
            .andExpect(view().name("faculties/faculties"))
            .andExpect(model().attribute("pageTitle", equalTo("Faculties")))
            .andExpect(model().attribute("faculties", hasSize(2)))
            .andExpect(model().attribute("faculties", hasItem(
                    allOf(
                            hasProperty("id", is(1)),
                            hasProperty("name", is("First faculty"))
                            )
                    )))
            .andExpect(model().attribute("faculties", hasItem(
                    allOf(
                            hasProperty("id", is(2)),
                            hasProperty("name", is("Second faculty"))
                            )
                    )));
            
        verify(facultyService).getAll();
    }

}
