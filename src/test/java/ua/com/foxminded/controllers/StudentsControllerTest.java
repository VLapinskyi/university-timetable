package ua.com.foxminded.controllers;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

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
import ua.com.foxminded.domain.Faculty;
import ua.com.foxminded.domain.Gender;
import ua.com.foxminded.domain.Group;
import ua.com.foxminded.domain.Student;
import ua.com.foxminded.service.StudentService;
import ua.com.foxminded.service.exceptions.ServiceException;
import ua.com.foxminded.settings.SpringConfiguration;

@ContextConfiguration(classes = { SpringConfiguration.class })
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
class StudentsControllerTest {
    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private StudentsController studentsController;

    @Mock
    private StudentService studentService;

    private MockMvc mockMvc;

    private DAOException daoException = new DAOException("DAO exception",
            new QueryTimeoutException("Exception message"));
    private ServiceException serviceWithDAOException = new ServiceException("Service exception", daoException);

    private ServiceException serviceWithIllegalArgumentException = new ServiceException("Service exception",
            new IllegalArgumentException());

    private Group group;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(studentsController, "studentService", studentService);
        group = new Group();
        group.setId(1);
        group.setFaculty(new Faculty());
        group.setName("Group");
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldAddToModelListWhenGetStudents() throws Exception {
        Student firstStudent = new Student();
        firstStudent.setId(1);
        firstStudent.setFirstName("Ivan");
        firstStudent.setLastName("Ivanov");
        firstStudent.setGender(Gender.MALE);
        firstStudent.setEmail("ivanovivan@test.com");
        firstStudent.setPhoneNumber("+380123456789");
        firstStudent.setGroup(group);

        Student secondStudent = new Student();
        secondStudent.setId(2);
        secondStudent.setFirstName("Vasyl");
        secondStudent.setLastName("Vasyliev");
        secondStudent.setGender(Gender.MALE);
        secondStudent.setEmail("vasylievvasyl@test.com");
        secondStudent.setPhoneNumber("+380987654321");
        secondStudent.setGroup(group);

        when(studentService.getAll()).thenReturn(Arrays.asList(firstStudent, secondStudent));

        mockMvc.perform(get("/students")).andExpect(status().isOk()).andExpect(view().name("students/students"))
                .andExpect(model().attribute("pageTitle", equalTo("Students")))
                .andExpect(model().attribute("students", hasSize(2)))
                .andExpect(model().attribute("students", hasItem(allOf(hasProperty("id", is(1)),
                        hasProperty("firstName", is("Ivan")), hasProperty("lastName", is("Ivanov")),
                        hasProperty("gender", is(Gender.MALE)), hasProperty("email", is("ivanovivan@test.com")),
                        hasProperty("phoneNumber", is("+380123456789")), hasProperty("group", equalTo((group)))))))
                .andExpect(model().attribute("students",
                        hasItem(allOf(hasProperty("id", is(2)), hasProperty("firstName", is("Vasyl")),
                                hasProperty("lastName", is("Vasyliev")), hasProperty("gender", equalTo(Gender.MALE)),
                                hasProperty("email", is("vasylievvasyl@test.com")),
                                hasProperty("phoneNumber", is("+380987654321")), hasProperty("group", equalTo(group))

                        ))));

        verify(studentService).getAll();
    }

    @Test
    void shouldAddToModelFoundedEntityWhenGetStudent() throws Exception {
        int id = 2;

        Student student = new Student();
        student.setId(id);
        student.setFirstName("Petro");
        student.setLastName("Petrov");
        student.setGender(Gender.MALE);
        student.setEmail("petrovpetro@test.com");
        student.setPhoneNumber("+380784512369");
        student.setGroup(group);

        when(studentService.getById(id)).thenReturn(student);

        mockMvc.perform(get("/students/{id}", id)).andExpect(status().isOk()).andExpect(view().name("students/student"))
                .andExpect(
                        model().attribute("pageTitle", equalTo(student.getFirstName() + " " + student.getLastName())))
                .andExpect(model().attribute("student", hasProperty("id", is(id))))
                .andExpect(model().attribute("student", hasProperty("firstName", equalTo(student.getFirstName()))))
                .andExpect(model().attribute("student", hasProperty("lastName", equalTo(student.getLastName()))))
                .andExpect(model().attribute("student", hasProperty("gender", equalTo(Gender.MALE))))
                .andExpect(model().attribute("student", hasProperty("email", equalTo("petrovpetro@test.com"))))
                .andExpect(model().attribute("student", hasProperty("phoneNumber", equalTo("+380784512369"))))
                .andExpect(model().attribute("student", hasProperty("group", equalTo(group))));

        verify(studentService).getById(id);
    }

    @Test
    void sholdReturnError500WhenDAOExceptionWhileGetStudents() throws Exception {
        when(studentService.getAll()).thenThrow(serviceWithDAOException);

        mockMvc.perform(get("/students")).andExpect(status().isInternalServerError());
        verify(studentService).getAll();
    }

    @Test
    void shouldReturnError404WhenServiceExceptionWhileGetStudents() throws Exception {
        when(studentService.getAll()).thenThrow(ServiceException.class);

        mockMvc.perform(get("/students")).andExpect(status().isNotFound());
        verify(studentService).getAll();
    }

    @Test
    void shouldReturnError500WhenDAOExceptionWhileGetStudent() throws Exception {
        int id = 7;

        when(studentService.getById(id)).thenThrow(serviceWithDAOException);

        mockMvc.perform(get("/students/{id}", id)).andExpect(status().isInternalServerError());
        verify(studentService).getById(id);
    }

    @Test
    void shouldReturnError400WhenIllegalArgumentExceptionWhileGetStudent() throws Exception {
        int id = 12;
        when(studentService.getById(id)).thenThrow(serviceWithIllegalArgumentException);
        mockMvc.perform(get("/students/{id}", id)).andExpect(status().isBadRequest());
        verify(studentService).getById(id);
    }

    @Test
    void shouldReturnError404WhenEntityIsNotFoundWhileGetStudent() throws Exception {
        int id = 62;

        when(studentService.getById(id)).thenThrow(ServiceException.class);
        mockMvc.perform(get("/students/{id}", id)).andExpect(status().isNotFound());
        verify(studentService).getById(id);
    }
}
