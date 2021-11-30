package ua.com.foxminded.api.system;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

import ua.com.foxminded.domain.Faculty;
import ua.com.foxminded.domain.Gender;
import ua.com.foxminded.domain.Group;
import ua.com.foxminded.domain.Student;
import ua.com.foxminded.repositories.exceptions.RepositoryException;
import ua.com.foxminded.service.StudentService;
import ua.com.foxminded.service.exceptions.ServiceException;

@SpringBootTest
@DBRider
@DBUnit(cacheConnection = false, leakHunter = true)
@TestPropertySource("/application-test.properties")
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
class StudentsRestControllerSystemTest {
    
    private final String testData = "/datasets/test-data.xml";
    private final String rootFolderDataSets = "/datasets/students/";
    
    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @SpyBean
    @Autowired
    private StudentService studentService;
    
    @Autowired
    private ObjectMapper objectMapper;

    private Group firstGroup;
    private Group secondGroup;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        Faculty firstFaculty = new Faculty();
        firstFaculty.setId(1);
        firstFaculty.setName("TestFaculty1");
        
        Faculty secondFaculty = new Faculty();
        secondFaculty.setId(2);
        secondFaculty.setName("TestFaculty2");
        
        firstGroup = new Group();
        firstGroup.setId(1);
        firstGroup.setFaculty(firstFaculty);
        firstGroup.setName("TestGroup1");

        secondGroup = new Group();
        secondGroup.setId(2);
        secondGroup.setName("TestGroup2");
        secondGroup.setFaculty(secondFaculty);
    }

    @Test
    @DataSet(value = testData, cleanBefore = true)
    @ExpectedDataSet(rootFolderDataSets + "all-students.xml")
    void shouldGetStudents() throws Exception {     
        
        mockMvc.perform(get("/students")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content()
                .contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
            
        verify(studentService).getAll();
    }

    @Test
    @DataSet(value = testData, cleanBefore = true)
    void shouldGetStudent() throws Exception {
        int id = 2;

        Student student = new Student();
        student.setId(id);
        student.setFirstName("Illia");
        student.setLastName("Misiats");
        student.setGender(Gender.MALE);
        student.setEmail("illiamisiats@gmail.com");
        student.setPhoneNumber("+380323659872");
        student.setGroup(firstGroup);

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
    @DataSet(value = testData, cleanBefore = true)
    @ExpectedDataSet(rootFolderDataSets + "after-creating.xml")
    void shouldCreateStudent() throws Exception {
        int groupId = 1;

        Student testStudent = new Student();
        testStudent.setFirstName("Ivan");
        testStudent.setLastName("Ivanov");
        testStudent.setGender(Gender.MALE);
        testStudent.setPhoneNumber("+380987845123");
        testStudent.setEmail("iivanov@test.com");
        testStudent.setGroup(firstGroup);

        String testJson = objectMapper.writeValueAsString(testStudent);
        
        mockMvc.perform(post("/students")
                .content(testJson)
                .contentType(MediaType.APPLICATION_JSON)
                .param("group-id", Integer.toString(groupId)))
            .andExpect(status().isOk())
            .andExpect(content()
                    .contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DataSet(value = testData, cleanBefore = true)
    @ExpectedDataSet(rootFolderDataSets + "after-updating.xml")
    void shouldUpdateStudent() throws Exception {
        int studentId = 2;
        Student testStudent = new Student();
        testStudent.setId(studentId);
        testStudent.setFirstName("Roman");
        testStudent.setLastName("Dudchenko");
        testStudent.setGender(Gender.MALE);
        testStudent.setPhoneNumber("+380501236547");
        testStudent.setEmail("rdudchenko@test.com");
        testStudent.setGroup(secondGroup);

        String testJson = objectMapper.writeValueAsString(testStudent);
        
        mockMvc.perform(patch("/students/{id}", studentId)
                .content(testJson)
                .param("group-id", Integer.toString(secondGroup.getId()))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json(testJson));
    }

    @Test
    @DataSet(value = testData, cleanBefore = true, disableConstraints = true)
    void shouldDeleteStudent() throws Exception {
        int studentId = 1;
        
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
    @DataSet(value = testData, cleanBefore = true)
    void shouldReturnError500WhenRepositoryExceptionWhileGetStudent() throws Exception {
        int id = 3;
        
        when(studentService.getById(id)).thenThrow(new ServiceException("message", new RepositoryException()));

        mockMvc.perform(get("/students/{id}", id)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError());
        verify(studentService).getById(id);
    }

    @Test
    void shouldReturnError400WhenIllegalArgumentExceptionWhileGetStudent() throws Exception {
        int id = -12;
        mockMvc.perform(get("/students/{id}", id)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnError404WhenEntityIsNotFoundWhileGetStudent() throws Exception {
        int id = 62;
        
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
        student.setGroup(firstGroup);
        
        String testJson = objectMapper.writeValueAsString(student);
        
        doThrow(new ServiceException("Service exception", new RepositoryException())).when(studentService).create(student);
        
        mockMvc.perform(post("/students")
                .content(testJson)
                .param("group-id", Integer.toString(firstGroup.getId()))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError());
    }
    
    @Test
    @DataSet(value = testData, cleanBefore = true)
    void shouldReturnError400WhenConstraintViolationExceptionWhileCreateStudent() throws Exception {
        Student student = new Student();
        student.setFirstName(" Kateryna");
        student.setLastName("Lytvynenko");
        student.setGender(Gender.FEMALE);
        student.setPhoneNumber("+380967452412");
        student.setEmail("KLytvynenko@test.com");
        student.setGroup(secondGroup);
        
        String testJson = objectMapper.writeValueAsString(student);
        
        mockMvc.perform(post("/students")
                .content(testJson)
                .param("group-id", Integer.toString(secondGroup.getId()))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
    }
    
    @Test
    @DataSet(value = testData, cleanBefore = true)
    void shouldReturnError400WhenIlleagalArgumentExceptionWhileCreateStudent() throws Exception {
        int wrongId = -14;
        Student student = new Student();
        student.setId(wrongId);
        student.setFirstName("Nataliia");
        student.setLastName("Biliak");
        student.setGender(Gender.FEMALE);
        student.setPhoneNumber("+380447845123");
        student.setEmail("KBiliak@test.com");
        student.setGroup(firstGroup);
        
        String testJson = objectMapper.writeValueAsString(student);
        
        mockMvc.perform(post("/students")
                .content(testJson)
                .param("group-id", Integer.toString(firstGroup.getId()))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
    }
    
    @Test
    @DataSet(value = testData, cleanBefore = true)
    void shouldReturnError500WhenServiceExceptionWhileCreateStudent() throws Exception {
        Student student = new Student();
        student.setFirstName("Olena");
        student.setLastName("Gaidamachenko");
        student.setGender(Gender.FEMALE);
        student.setPhoneNumber("+380687452963");
        student.setEmail("OGaidamachenko@test.com");
        student.setGroup(firstGroup);
        
        String testJson = objectMapper.writeValueAsString(student);
        
        doThrow(ServiceException.class).when(studentService).create(student);
        
        mockMvc.perform(post("/students")
                .content(testJson)
                .param("group-id", Integer.toString(firstGroup.getId()))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError());
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
        student.setGroup(firstGroup);
        
        String testJson = objectMapper.writeValueAsString(student);
        
        mockMvc.perform(patch("/students/{id}", studentId)
                .content(testJson)
                .param("group-id", Integer.toString(firstGroup.getId()))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError());
    }
    
    @Test
    void shouldError400WhenConstraintViolationExceptionWhileUpdateStudent() throws Exception {
        int studentId = 41;
        Student student = new Student();
        student.setId(studentId);
        student.setFirstName("Inna");
        student.setLastName("     Nastenko");
        student.setGender(Gender.FEMALE);
        student.setPhoneNumber("+380557485963");
        student.setEmail("INastenko@test.com");
        student.setGroup(secondGroup);
        
        String testJson = objectMapper.writeValueAsString(student);
        
        mockMvc.perform(patch("/students/{id}", studentId)
                .content(testJson)
                .param("group-value", Integer.toString(secondGroup.getId()))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
    }
    
    @Test
    void shouldError400WhenIllegalArgumentExceptionWhileUpdateStudent() throws Exception {
        int studentId = 23;
        Student student = null;
        
        String testJson = objectMapper.writeValueAsString(student);
        
        mockMvc.perform(patch("/students/{id}", studentId)
                .content(testJson)
                .param("group-id", Integer.toString(firstGroup.getId()))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
    }
    
    @Test
    @DataSet(value = testData, cleanBefore = true)
    void shouldError500WhenServiceExceptionWhileUpdateStudent() throws Exception {
        int studentId = 1;
        Student student = new Student();
        student.setId(studentId);
        student.setFirstName("Tetiana");
        student.setLastName("Shcherbak");
        student.setGender(Gender.FEMALE);
        student.setPhoneNumber("+380449685741");
        student.setEmail("TShcherbak@test.com");
        student.setGroup(secondGroup);
        
        String testJson = objectMapper.writeValueAsString(student);
        
        doThrow(ServiceException.class).when(studentService).update(student);
        
        mockMvc.perform(patch("/students/{id}", studentId)
                .content(testJson)
                .param("group-id", Integer.toString(secondGroup.getId()))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError());
    }
    
    @Test
    void shouldReturn500ErrorWhenRepositoryXExceptionWhileDeleteStudent() throws Exception {
        int studentId = 36;
        
        mockMvc.perform(delete("/students/{id}", studentId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError());
        
        verify(studentService).deleteById(studentId);
    }
    
    @Test
    void shouldReturn400ErrorWhenIllegalArgumentExceptionWhileDeleteStudent() throws Exception {
        int studentId = -212;
        
        mockMvc.perform(delete("/students/{id}", studentId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
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
