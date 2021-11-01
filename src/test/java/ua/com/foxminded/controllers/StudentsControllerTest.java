package ua.com.foxminded.controllers;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.QueryTimeoutException;

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
import ua.com.foxminded.domain.Gender;
import ua.com.foxminded.domain.Group;
import ua.com.foxminded.domain.Student;
import ua.com.foxminded.repositories.exceptions.RepositoryException;
import ua.com.foxminded.service.GroupService;
import ua.com.foxminded.service.StudentService;
import ua.com.foxminded.service.exceptions.ServiceException;

@WebMvcTest(StudentsController.class)
@Import({AopAutoConfiguration.class, GeneralControllerAspect.class})
class StudentsControllerTest {

    @Autowired
    private StudentsController studentsController;

    @MockBean
    private StudentService studentService;

    @MockBean
    private GroupService groupService;

    @Autowired
    private MockMvc mockMvc;

    private RepositoryException repositoryException = new RepositoryException("repository exception",
            new QueryTimeoutException("Exception message"));
    private ServiceException serviceWithRepositoryException = new ServiceException("Service exception", repositoryException);

    private ServiceException serviceWithIllegalArgumentException = new ServiceException("Service exception",
            new IllegalArgumentException());
    
    private ServiceException serviceWithConstraintViolationException = new ServiceException("Service exception",
            new ConstraintViolationException(null));

    private Group group;
    private Group anotherGroup;

    @BeforeEach
    void setUp() throws Exception {
        ReflectionTestUtils.setField(studentsController, "studentService", studentService);
        ReflectionTestUtils.setField(studentsController, "groupService", groupService);
        group = new Group();
        group.setId(1);
        group.setFaculty(new Faculty());
        group.setName("Group");

        anotherGroup = new Group();
        anotherGroup.setId(2);
        anotherGroup.setName("Another group");
        anotherGroup.setFaculty(new Faculty());
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
    void shouldGenerateRightPageWhenNewStudent() throws Exception {
        List<Group> groups = new ArrayList<>(Arrays.asList(group));

        when(groupService.getAll()).thenReturn(groups);

        mockMvc.perform(get("/students/new"))
        .andExpect(status().isOk())
        .andExpect(view().name("students/new"))
        .andExpect(model().attribute("genders", equalTo(Gender.values())))
        .andExpect(model().attribute("pageTitle", "Create a new student"))
        .andExpect(model().attribute("groups", equalTo(groups)));

        verify(groupService).getAll();
    }

    @Test
    void shouldCreateStudent() throws Exception {
        int groupId = 1;

        Student testStudent = new Student();
        testStudent.setFirstName("Ivan");
        testStudent.setLastName("Ivanov");
        testStudent.setGender(Gender.MALE);
        testStudent.setPhoneNumber("+380987845123");
        testStudent.setEmail("iivanov@test.com");

        Student expectedStudent = new Student();
        expectedStudent.setFirstName("Ivan");
        expectedStudent.setLastName("Ivanov");
        expectedStudent.setGender(Gender.MALE);
        expectedStudent.setPhoneNumber("+380987845123");
        expectedStudent.setEmail("iivanov@test.com");
        expectedStudent.setGroup(group);

        when(groupService.getById(groupId)).thenReturn(group);

        mockMvc.perform(post("/students").flashAttr("student", testStudent)
                .param("group-value", Integer.toString(groupId))
                .param("gender-value", Gender.MALE.toString()))
        .andExpect(view().name("redirect:/students"))
        .andExpect(status().is3xxRedirection());

        verify(groupService).getById(groupId);
        verify(studentService).create(expectedStudent);
    }

    @Test
    void shouldGenerateRightPageWhenEditStudent() throws Exception {
        int studentId = 5;
        int groupId = group.getId();

        Student testStudent = new Student();
        testStudent.setId(studentId);
        testStudent.setFirstName("Vasyl");
        testStudent.setLastName("Romanov");
        testStudent.setGender(Gender.MALE);
        testStudent.setPhoneNumber("+380639865321");
        testStudent.setEmail("VRomanov@test.com");
        testStudent.setGroup(group);

        List<Group> allGroups = new ArrayList<>(Arrays.asList(group, anotherGroup));
        List<Group> expectedGroups = new ArrayList<>(Arrays.asList(anotherGroup));

        when(studentService.getById(studentId)).thenReturn(testStudent);
        when(groupService.getAll()).thenReturn(allGroups);
        when(groupService.getById(groupId)).thenReturn(group);

        mockMvc.perform(get("/students/{id}/edit", studentId))
        .andExpect(view().name("students/edit"))
        .andExpect(model().attribute("pageTitle", equalTo("Edit a student " + testStudent.getFirstName() + " " + testStudent.getLastName())))
        .andExpect(model().attribute("student", equalTo(testStudent)))
        .andExpect(model().attribute("groups", equalTo(expectedGroups)))
        .andExpect(model().attribute("genders", Arrays.asList(Gender.FEMALE)));

        verify(studentService).getById(studentId);
        verify(groupService).getAll();
    }

    @Test
    void shouldUpdateStudent() throws Exception {
        int studentId = 63;
        Student testStudent = new Student();
        testStudent.setId(studentId);
        testStudent.setFirstName("Roman");
        testStudent.setLastName("Dudchenko");
        testStudent.setGender(Gender.MALE);
        testStudent.setPhoneNumber("+380501236547");
        testStudent.setEmail("rdudchenko@test.com");
        testStudent.setGroup(anotherGroup);

        Student expectedStudent = new Student();
        expectedStudent.setId(studentId);
        expectedStudent.setFirstName("Roman");
        expectedStudent.setLastName("Dudchenko");
        expectedStudent.setGender(Gender.MALE);
        expectedStudent.setPhoneNumber("+380501236547");
        expectedStudent.setEmail("rdudchenko@test.com");
        expectedStudent.setGroup(group);

        when(groupService.getById(group.getId())).thenReturn(group);

        mockMvc.perform(patch("/students/{id}", studentId)
                .flashAttr("student", testStudent)
                .param("gender-value", Gender.MALE.toString())
                .param("group-value", Integer.toString(group.getId())))
        .andExpect(view().name("redirect:/students"))
        .andExpect(status().is3xxRedirection());

        verify(groupService).getById(group.getId());
        verify(studentService).update(expectedStudent);
    }

    @Test
    void shouldDeleteStudent() throws Exception {
        int studentId = 4;
        mockMvc.perform(delete("/students/{id}", studentId))
        .andExpect(view().name("redirect:/students"))
        .andExpect(status().is3xxRedirection());

        verify(studentService).deleteById(studentId);
    }

    @Test
    void sholdReturnError500WhenRepositoryExceptionWhileGetStudents() throws Exception {
        when(studentService.getAll()).thenThrow(serviceWithRepositoryException);

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
    void shouldReturnError500WhenRepositoryExceptionWhileGetStudent() throws Exception {
        int id = 7;

        when(studentService.getById(id)).thenThrow(serviceWithRepositoryException);

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

    @Test
    void shouldReturnError500WhenRepositoryExceptionWhileNewStudent() throws Exception {
        doThrow(serviceWithRepositoryException).when(groupService).getAll();

        mockMvc.perform(get("/students/new"))
        .andExpect(status().isInternalServerError());

        verify(groupService).getAll();
    }

    @Test
    void shouldReturnError500WhenServiceExceptionWhileNewStudent() throws Exception {
        doThrow(ServiceException.class).when(groupService).getAll();

        mockMvc.perform(get("/students/new"))
        .andExpect(status().isInternalServerError());

        verify(groupService).getAll();
    }
    
    @Test
    void shouldReturnError500WhenRepositoryExceptionWhileCreateStudent() throws Exception {
        Student student = new Student();
        student.setFirstName("Maya");
        student.setLastName("Ukrainets");
        student.setGender(Gender.FEMALE);
        student.setPhoneNumber("+380986574123");
        student.setEmail("MUkrainets@test.com");
        
        doThrow(serviceWithRepositoryException).when(groupService).getById(group.getId());
        
        mockMvc.perform(post("/students").flashAttr("student", student)
                .param("group-value", Integer.toString(group.getId()))
                .param("gender-value", Gender.FEMALE.toString()))
        .andExpect(status().isInternalServerError());
        
        verify(groupService).getById(group.getId());
    }
    
    @Test
    void shouldReturnError400WhenConstraintViolationExceptionWhileCreateStudent() throws Exception {
        Student student = new Student();
        student.setFirstName("Kateryna");
        student.setLastName("Lytvynenko");
        student.setGender(Gender.FEMALE);
        student.setPhoneNumber("+380967452412");
        student.setEmail("KLytvynenko@test.com");
        
        doThrow(serviceWithConstraintViolationException).when(groupService).getById(anotherGroup.getId());
        
        mockMvc.perform(post("/students").flashAttr("student", student)
                .param("group-value", Integer.toString(anotherGroup.getId()))
                .param("gender-value", Gender.FEMALE.toString()))
        .andExpect(status().isBadRequest());
        
        verify(groupService).getById(anotherGroup.getId());
    }
    
    @Test
    void shouldReturnError400WhenIlleagalArgumentExceptionWhileCreateStudent() throws Exception {
        int wrongId = 14;
        Student student = new Student();
        student.setId(wrongId);
        student.setFirstName("Nataliia");
        student.setLastName("Biliak");
        student.setGender(Gender.FEMALE);
        student.setPhoneNumber("+380447845123");
        student.setEmail("KBiliak@test.com");
        
        doThrow(serviceWithIllegalArgumentException).when(groupService).getById(group.getId());
        
        mockMvc.perform(post("/students").flashAttr("student", student)
                .param("group-value", Integer.toString(group.getId()))
                .param("gender-value", Gender.FEMALE.toString()))
        .andExpect(status().isBadRequest());
        
        verify(groupService).getById(group.getId());
    }
    
    @Test
    void shouldReturnError500WhenServiceExceptionWhileCreateStudent() throws Exception {
        Student student = new Student();
        student.setFirstName("Olena");
        student.setLastName("Gaidamachenko");
        student.setGender(Gender.FEMALE);
        student.setPhoneNumber("+380687452963");
        student.setEmail("OGaidamachenko@test.com");
        
        doThrow(ServiceException.class).when(groupService).getById(group.getId());
        
        mockMvc.perform(post("/students").flashAttr("student", student)
                .param("group-value", Integer.toString(group.getId()))
                .param("gender-value", Gender.FEMALE.toString()))
        .andExpect(status().isInternalServerError());
        
        verify(groupService).getById(group.getId());
    }
    
    @Test
    void shouldError500WhenRepositoryExceptionWhileEditStudent() throws Exception {
        int studentId = 9;
        Student student = new Student();
        student.setId(studentId);
        student.setFirstName("Nataliia");
        student.setLastName("Fedoriaka");
        student.setGender(Gender.FEMALE);
        student.setPhoneNumber("+380437896541");
        student.setEmail("NFedoriaka@test.com");
        student.setGroup(group);
        
        when(studentService.getById(studentId)).thenReturn(student);
        when(groupService.getAll()).thenThrow(serviceWithRepositoryException);
        
        mockMvc.perform(get("/students/{id}/edit", studentId))
        .andExpect(status().isInternalServerError());
        
        verify(studentService).getById(studentId);
        verify(groupService).getAll();       
    }
    
    @Test
    void shouldError500WhenServiceExceptionWhileEditStudent() throws Exception {
        int studentId = 12;
        Student student = new Student();
        student.setId(studentId);
        student.setFirstName("Iuliia");
        student.setLastName("Ogreba");
        student.setGender(Gender.FEMALE);
        student.setPhoneNumber("+380443265987");
        student.setEmail("IOgreba@test.com");
        student.setGroup(group);
        
        when(studentService.getById(studentId)).thenReturn(student);
        when(groupService.getAll()).thenThrow(ServiceException.class);
        
        mockMvc.perform(get("/students/{id}/edit", studentId))
        .andExpect(status().isInternalServerError());
        
        verify(studentService).getById(studentId);
        verify(groupService).getAll();       
    }
    
    @Test
    void shouldError500WhenRepositoryExceptionWhileUpdateStudent() throws Exception {
        int studentId = 52;
        Student student = new Student();
        student.setId(studentId);
        student.setFirstName("Olena");
        student.setLastName("Shlenchak");
        student.setGender(Gender.FEMALE);
        student.setPhoneNumber("+380441236547");
        student.setEmail("OShlenchak@test.com");
        student.setGroup(group);
        
        doThrow(serviceWithRepositoryException).when(groupService).getById(group.getId());
        
        mockMvc.perform(patch("/students/{id}", studentId)
                .flashAttr("student", student)
                .param("group-value", Integer.toString(group.getId()))
                .param("gender-value", Gender.FEMALE.toString()))
        .andExpect(status().isInternalServerError());

        verify(groupService).getById(group.getId());
    }
    
    @Test
    void shouldError400WhenConstraintViolationExceptionWhileUpdateStudent() throws Exception {
        int studentId = 41;
        Student student = new Student();
        student.setId(studentId);
        student.setFirstName("Inna");
        student.setLastName("Nastenko");
        student.setGender(Gender.FEMALE);
        student.setPhoneNumber("+380557485963");
        student.setEmail("INastenko@test.com");
        student.setGroup(group);
        
        doThrow(serviceWithConstraintViolationException).when(groupService).getById(group.getId());
        
        mockMvc.perform(patch("/students/{id}", studentId)
                .flashAttr("student", student)
                .param("group-value", Integer.toString(group.getId()))
                .param("gender-value", Gender.FEMALE.toString()))
        .andExpect(status().isBadRequest());

        verify(groupService).getById(group.getId());
    }
    
    @Test
    void shouldError400WhenIllegalArgumentExceptionWhileUpdateStudent() throws Exception {
        int studentId = 23;
        Student student = new Student();
        student.setId(studentId);
        student.setFirstName("Alla");
        student.setLastName("Matviichuk");
        student.setGender(Gender.FEMALE);
        student.setPhoneNumber("+380659874521");
        student.setEmail("AMatviichuk@test.com");
        student.setGroup(group);
        
        doThrow(serviceWithIllegalArgumentException).when(groupService).getById(group.getId());
        
        mockMvc.perform(patch("/students/{id}", studentId)
                .flashAttr("student", student)
                .param("group-value", Integer.toString(group.getId()))
                .param("gender-value", Gender.FEMALE.toString()))
        .andExpect(status().isBadRequest());

        verify(groupService).getById(group.getId());
    }
    
    @Test
    void shouldError500WhenServiceExceptionWhileUpdateStudent() throws Exception {
        int studentId = 85;
        Student student = new Student();
        student.setId(studentId);
        student.setFirstName("Tetiana");
        student.setLastName("Shcherbak");
        student.setGender(Gender.FEMALE);
        student.setPhoneNumber("+380449685741");
        student.setEmail("TShcherbak@test.com");
        student.setGroup(group);
        
        doThrow(ServiceException.class).when(groupService).getById(group.getId());
        
        mockMvc.perform(patch("/students/{id}", studentId)
                .flashAttr("student", student)
                .param("group-value", Integer.toString(group.getId()))
                .param("gender-value", Gender.FEMALE.toString()))
        .andExpect(status().isInternalServerError());

        verify(groupService).getById(group.getId());
    }
    
    @Test
    void shouldReturn500ErrorWhenRepositoryXExceptionWhileDeleteStudent() throws Exception {
        int studentId = 36;
        doThrow(serviceWithRepositoryException).when(studentService).deleteById(studentId);
        
        mockMvc.perform(delete("/students/{id}", studentId))
        .andExpect(status().isInternalServerError());
        
        verify(studentService).deleteById(studentId);
    }
    
    @Test
    void shouldReturn400ErrorWhenIllegalArgumentExceptionWhileDeleteStudent() throws Exception {
        int studentId = 212;
        doThrow(serviceWithIllegalArgumentException).when(studentService).deleteById(studentId);
        
        mockMvc.perform(delete("/students/{id}", studentId))
        .andExpect(status().isBadRequest());
        
        verify(studentService).deleteById(studentId);
    }
    
    @Test
    void shouldReturn400ErrorWhenConExceptionWhileDeleteStudent() throws Exception {
        int studentId = 212;
        doThrow(serviceWithIllegalArgumentException).when(studentService).deleteById(studentId);
        
        mockMvc.perform(delete("/students/{id}", studentId))
        .andExpect(status().isBadRequest());
        
        verify(studentService).deleteById(studentId);
    }
    
    @Test
    void shouldReturn500ErrorWhenServiceExceptionWhileDeleteStudent() throws Exception {
        int studentId = 325;
        doThrow(ServiceException.class).when(studentService).deleteById(studentId);
        
        mockMvc.perform(delete("/students/{id}", studentId))
        .andExpect(status().isInternalServerError());
        
        verify(studentService).deleteById(studentId);
    }
}
