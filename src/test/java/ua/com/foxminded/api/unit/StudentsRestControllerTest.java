package ua.com.foxminded.api.unit;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

import ua.com.foxminded.api.StudentsRestController;
import ua.com.foxminded.api.aspects.GeneralRestControllerAspect;
import ua.com.foxminded.domain.Faculty;
import ua.com.foxminded.domain.Gender;
import ua.com.foxminded.domain.Group;
import ua.com.foxminded.domain.Student;
import ua.com.foxminded.repositories.exceptions.RepositoryException;
import ua.com.foxminded.service.GroupService;
import ua.com.foxminded.service.StudentService;
import ua.com.foxminded.service.exceptions.ServiceException;

@WebMvcTest(StudentsRestController.class)
@Import({AopAutoConfiguration.class, GeneralRestControllerAspect.class})
class StudentsRestControllerTest {

    @Autowired
    private StudentsRestController studentsRestController;

    @MockBean
    private StudentService studentService;

    @MockBean
    private GroupService groupService;

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;

    private Group group;
    private Group anotherGroup;

    @BeforeEach
    void setUp() throws Exception {
        ReflectionTestUtils.setField(studentsRestController, "studentService", studentService);
        ReflectionTestUtils.setField(studentsRestController, "groupService", groupService);
        group = new Group();
        group.setId(1);
        group.setFaculty(new Faculty());
        group.setName("Group");

        anotherGroup = new Group();
        anotherGroup.setId(2);
        anotherGroup.setName("Another group");
        anotherGroup.setFaculty(new Faculty());
    }

    @Test
    void shouldGetStudents() throws Exception {
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
        
        List<Student> students = new ArrayList<>(Arrays.asList(firstStudent, secondStudent));

        when(studentService.getAll()).thenReturn(students);

        String expectedResult = objectMapper.writeValueAsString(students);       
        
        mockMvc.perform(get("/students")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content()
                .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedResult));
            
        verify(studentService).getAll();
    }

    @Test
    void shouldGetStudent() throws Exception {
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

        String expectedResult = objectMapper.writeValueAsString(student);
        
        mockMvc.perform(get("/students/{id}", id)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content()
                    .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedResult));
        
        verify(studentService).getById(id);
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
        testStudent.setGroup(group);

        when(groupService.getById(groupId)).thenReturn(group);

        String testJson = objectMapper.writeValueAsString(testStudent);
        
        mockMvc.perform(post("/students")
                .content(testJson)
                .contentType(MediaType.APPLICATION_JSON)
                .param("group-id", Integer.toString(groupId)))
            .andExpect(status().isOk())
            .andExpect(content()
                    .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json(testJson));
                
        verify(groupService).getById(groupId);
        verify(studentService).create(testStudent);
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
        testStudent.setGroup(group);

        when(groupService.getById(group.getId())).thenReturn(group);

        String testJson = objectMapper.writeValueAsString(testStudent);
        
        mockMvc.perform(patch("/students/{id}", studentId)
                .content(testJson)
                .param("group-id", Integer.toString(group.getId()))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json(testJson));

        verify(groupService).getById(group.getId());
        verify(studentService).update(testStudent);
    }

    @Test
    void shouldDeleteStudent() throws Exception {
        int studentId = 4;
        
        String expectedResult = "Student with id: " + studentId + " was deleted.";
        
        mockMvc.perform(delete("/students/{id}", studentId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content()
                    .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().string(expectedResult));

        verify(studentService).deleteById(studentId);
    }

    @Test
    void sholdReturnError500WhenRepositoryExceptionWhileGetStudents() throws Exception {
        when(studentService.getAll()).thenThrow(new ServiceException("Service exception", new RepositoryException()));

        mockMvc.perform(get("/students")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError());
        verify(studentService).getAll();
    }

    @Test
    void shouldReturnError404WhenServiceExceptionWhileGetStudents() throws Exception {
        when(studentService.getAll()).thenThrow(ServiceException.class);

        mockMvc.perform(get("/students")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
        verify(studentService).getAll();
    }

    @Test
    void shouldReturnError500WhenRepositoryExceptionWhileGetStudent() throws Exception {
        int id = 7;

        when(studentService.getById(id)).thenThrow(new ServiceException("Service exception", new RepositoryException()));

        mockMvc.perform(get("/students/{id}", id)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError());
        verify(studentService).getById(id);
    }

    @Test
    void shouldReturnError400WhenIllegalArgumentExceptionWhileGetStudent() throws Exception {
        int id = 12;
        when(studentService.getById(id)).thenThrow(new ServiceException("Service exception", new IllegalArgumentException()));
        mockMvc.perform(get("/students/{id}", id)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
        verify(studentService).getById(id);
    }

    @Test
    void shouldReturnError404WhenEntityIsNotFoundWhileGetStudent() throws Exception {
        int id = 62;

        when(studentService.getById(id)).thenThrow(ServiceException.class);
        mockMvc.perform(get("/students/{id}", id)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
        verify(studentService).getById(id);
    }
    
    @Test
    void shouldReturnError500WhenRepositoryExceptionWhileCreateStudent() throws Exception {
        Student student = new Student();
        student.setFirstName("Maya");
        student.setLastName("Ukrainets");
        student.setGender(Gender.FEMALE);
        student.setPhoneNumber("+380986574123");
        student.setEmail("MUkrainets@test.com");
        
        String testJson = objectMapper.writeValueAsString(student);
        
        doThrow(new ServiceException("Service exception", new RepositoryException())).when(groupService).getById(group.getId());
        
        mockMvc.perform(post("/students")
                .content(testJson)
                .param("group-id", Integer.toString(group.getId()))
                .contentType(MediaType.APPLICATION_JSON))
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
        
        String testJson = objectMapper.writeValueAsString(student);
        
        doThrow(new ServiceException("Service exception", new ConstraintViolationException(null))).when(groupService).getById(anotherGroup.getId());
        
        mockMvc.perform(post("/students")
                .content(testJson)
                .param("group-id", Integer.toString(anotherGroup.getId()))
                .contentType(MediaType.APPLICATION_JSON))
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
        
        String testJson = objectMapper.writeValueAsString(student);
        
        doThrow(new ServiceException("Service exception", new IllegalArgumentException())).when(groupService).getById(group.getId());
        
        mockMvc.perform(post("/students")
                .content(testJson)
                .param("group-id", Integer.toString(group.getId()))
                .contentType(MediaType.APPLICATION_JSON))
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
        
        String testJson = objectMapper.writeValueAsString(student);
        
        doThrow(ServiceException.class).when(groupService).getById(group.getId());
        
        mockMvc.perform(post("/students")
                .content(testJson)
                .param("group-id", Integer.toString(group.getId()))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError());
        
        verify(groupService).getById(group.getId());
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
        
        String testJson = objectMapper.writeValueAsString(student);
        
        doThrow(new ServiceException("Service exception", new RepositoryException())).when(groupService).getById(group.getId());
        
        mockMvc.perform(patch("/students/{id}", studentId)
                .content(testJson)
                .param("group-id", Integer.toString(group.getId()))
                .contentType(MediaType.APPLICATION_JSON))
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
        
        String testJson = objectMapper.writeValueAsString(student);
        
        doThrow(new ServiceException("Service exception", new ConstraintViolationException(null))).when(groupService).getById(group.getId());
        
        mockMvc.perform(patch("/students/{id}", studentId)
                .content(testJson)
                .param("group-value", Integer.toString(group.getId()))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
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
        
        String testJson = objectMapper.writeValueAsString(student);
        
        doThrow(new ServiceException("Service exception", new IllegalArgumentException())).when(groupService).getById(group.getId());
        
        mockMvc.perform(patch("/students/{id}", studentId)
                .content(testJson)
                .param("group-id", Integer.toString(group.getId()))
                .contentType(MediaType.APPLICATION_JSON))
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
        
        String testJson = objectMapper.writeValueAsString(student);
        
        doThrow(ServiceException.class).when(groupService).getById(group.getId());
        
        mockMvc.perform(patch("/students/{id}", studentId)
                .content(testJson)
                .param("group-id", Integer.toString(group.getId()))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError());

        verify(groupService).getById(group.getId());
    }
    
    @Test
    void shouldReturn500ErrorWhenRepositoryXExceptionWhileDeleteStudent() throws Exception {
        int studentId = 36;
        doThrow(new ServiceException("Service exception", new RepositoryException())).when(studentService).deleteById(studentId);
        
        mockMvc.perform(delete("/students/{id}", studentId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError());
        
        verify(studentService).deleteById(studentId);
    }
    
    @Test
    void shouldReturn400ErrorWhenIllegalArgumentExceptionWhileDeleteStudent() throws Exception {
        int studentId = 212;
        doThrow(new ServiceException("Service exception", new IllegalArgumentException())).when(studentService).deleteById(studentId);
        
        mockMvc.perform(delete("/students/{id}", studentId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
        
        verify(studentService).deleteById(studentId);
    }
    
    @Test
    void shouldReturn400ErrorWhenConExceptionWhileDeleteStudent() throws Exception {
        int studentId = 212;
        doThrow(new ServiceException("Service exception", new IllegalArgumentException())).when(studentService).deleteById(studentId);
        
        mockMvc.perform(delete("/students/{id}", studentId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
        
        verify(studentService).deleteById(studentId);
    }
    
    @Test
    void shouldReturn500ErrorWhenServiceExceptionWhileDeleteStudent() throws Exception {
        int studentId = 325;
        doThrow(ServiceException.class).when(studentService).deleteById(studentId);
        
        mockMvc.perform(delete("/students/{id}", studentId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError());
        
        verify(studentService).deleteById(studentId);
    }
}
