package ua.com.foxminded.api.unit;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.List;

import java.util.ArrayList;

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

import ua.com.foxminded.api.GroupsRestController;
import ua.com.foxminded.api.aspects.GeneralRestControllerAspect;
import ua.com.foxminded.domain.Faculty;
import ua.com.foxminded.domain.Group;
import ua.com.foxminded.repositories.exceptions.RepositoryException;
import ua.com.foxminded.service.FacultyService;
import ua.com.foxminded.service.GroupService;
import ua.com.foxminded.service.exceptions.ServiceException;

@WebMvcTest(GroupsRestController.class)
@Import({AopAutoConfiguration.class, GeneralRestControllerAspect.class})
class GroupsRestControllerTest {

    @Autowired
    private GroupsRestController groupsRestController;

    @MockBean
    private GroupService groupService;

    @MockBean
    private FacultyService facultyService;
    
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    private Faculty faculty = new Faculty();
    private Faculty anotherFaculty = new Faculty();

    @BeforeEach
    void setUp() throws Exception {
        ReflectionTestUtils.setField(groupsRestController, "groupService", groupService);
        ReflectionTestUtils.setField(groupsRestController, "facultyService", facultyService);

        faculty.setId(1);
        faculty.setName("Faculty");

        anotherFaculty.setId(2);
        anotherFaculty.setName("Another Faculty");
    }

    @Test
    void shouldGetGroups() throws Exception {
        Group firstGroup = new Group();
        firstGroup.setId(1);
        firstGroup.setName("First group");
        firstGroup.setFaculty(faculty);

        Group secondGroup = new Group();
        secondGroup.setId(2);
        secondGroup.setName("Second group");
        secondGroup.setFaculty(faculty);
        
        List<Group> groups = new ArrayList<>(Arrays.asList(firstGroup, secondGroup));

        when(groupService.getAll()).thenReturn(groups);
        
        String expectedResult = objectMapper.writeValueAsString(groups);

        mockMvc.perform(get("/groups"))
            .andExpect(status().isOk())
            .andExpect(content()
                    .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedResult));

        verify(groupService).getAll();
    }

    @Test
    void shouldGetGroup() throws Exception {
        int id = 2;
        Group group = new Group();
        group.setId(id);
        group.setName("Group");
        group.setFaculty(faculty);

        when(groupService.getById(id)).thenReturn(group);
        
        String expectedResult = objectMapper.writeValueAsString(group);

        mockMvc.perform(get("/groups/{id}", id)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content()
                    .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedResult));

        verify(groupService).getById(id);
    }

    @Test
    void shouldCreateGroup() throws Exception {
        int facultyId = 1;

        Group testGroup = new Group();
        testGroup.setName("Test group");

        Group expectedGroup = new Group();
        expectedGroup.setName("Test group");
        expectedGroup.setFaculty(faculty);

        when(facultyService.getById(facultyId)).thenReturn(faculty);
        
        String testJson = objectMapper.writeValueAsString(testGroup);
        String expectedJson = objectMapper.writeValueAsString(expectedGroup);

        mockMvc.perform(post("/groups")
                .content(testJson).param("faculty-id", Integer.toString(facultyId))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(content()
                    .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedJson))
            .andExpect(status().isOk());

        verify(facultyService).getById(facultyId);
        verify(groupService).create(expectedGroup);

    }

    @Test
    void shouldUpdateGroup() throws Exception {
        int groupId = 12;
        Group testGroup = new Group();
        testGroup.setId(groupId);
        testGroup.setName("Test group");
        testGroup.setFaculty(faculty);

        Group expectedGroup = new Group();
        expectedGroup.setId(groupId);
        expectedGroup.setName("Test group");
        expectedGroup.setFaculty(anotherFaculty);

        when(facultyService.getById(anotherFaculty.getId())).thenReturn(anotherFaculty);
        
        String testJson = objectMapper.writeValueAsString(testGroup);
        String expectedJsong = objectMapper.writeValueAsString(expectedGroup);

        mockMvc.perform(patch("/groups/{id}", groupId)
                .content(testJson).param("faculty-id", Integer.toString(anotherFaculty.getId()))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().json(expectedJsong))
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
                

        verify(facultyService).getById(anotherFaculty.getId());
        verify(groupService).update(expectedGroup);
    }

    @Test
    void shouldDeleteGroup() throws Exception {
        int groupId = 4;
        
        String expectedResult = "Group with id: " + groupId + " was deleted.";

        mockMvc.perform(delete("/groups/{id}", groupId)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().string(expectedResult));
     

        verify(groupService).deleteById(groupId);
    }

    @Test
    void shouldReturnError500WhenRepositoryExceptionWhileGetGroups() throws Exception {
        when(groupService.getAll()).thenThrow(new ServiceException("Service exception", new RepositoryException()));

        mockMvc.perform(get("/groups")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError());
        
        verify(groupService).getAll();
    }

    @Test
    void shouldReturnError404WhenServiceExceptionWhileGetGroups() throws Exception {
        when(groupService.getAll()).thenThrow(ServiceException.class);

        mockMvc.perform(get("/groups")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
        
        verify(groupService).getAll();
    }    

    @Test
    void shouldReturnError500WhenRepositoryExceptionWhileGetGroup() throws Exception {
        int id = 4;

        when(groupService.getById(id)).thenThrow(new ServiceException("Service exception", new RepositoryException()));

        mockMvc.perform(get("/groups/{id}", id)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError());
        
        verify(groupService).getById(id);
    }

    @Test
    void shouldReturnError400WhenIllegalArgumentExceptionWhileGetGroup() throws Exception {
        int id = 1;
        when(groupService.getById(id)).thenThrow(new ServiceException("Service exception", new IllegalArgumentException()));
        
        mockMvc.perform(get("/groups/{id}", id)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
        
        verify(groupService).getById(id);
    }

    @Test
    void shouldReturnError404WhenEntityIsNotFoundWhileGetGroup() throws Exception {
        int id = 2;

        when(groupService.getById(id)).thenThrow(ServiceException.class);
        
        mockMvc.perform(get("/groups/{id}", id)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
        
        verify(groupService).getById(id);
    }

    @Test
    void shouldReturnError500WhenRepositoryExceptionWhileCreateGroup() throws Exception {
        Group testGroup = new Group();
        testGroup.setName("Test group");
        testGroup.setFaculty(faculty);
        
        when(facultyService.getById(faculty.getId())).thenReturn(faculty);
        doThrow(new ServiceException("Service exception", new RepositoryException())).when(facultyService).getById(faculty.getId());

        String testJson = objectMapper.writeValueAsString(testGroup);
        
        mockMvc.perform(post("/groups")
                .content(testJson).param("faculty-id", Integer.toString(faculty.getId()))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError());
        
        verify(facultyService).getById(faculty.getId());
    }

    @Test
    void shouldReturnError400WhenConstrantViolationExceptionWhileCreateGroup() throws Exception {
        Group testGroup = new Group();
        testGroup.setName(" Wrong name");
        testGroup.setFaculty(anotherFaculty);

        when(facultyService.getById(anotherFaculty.getId())).thenReturn(anotherFaculty);
        doThrow(new ServiceException("Service exception", new ConstraintViolationException(null))).when(groupService).create(testGroup);

        String testJson = objectMapper.writeValueAsString(testGroup);
        
        mockMvc.perform(post("/groups")
                .content(testJson).param("faculty-id", Integer.toString(anotherFaculty.getId()))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

        verify(facultyService).getById(anotherFaculty.getId());
        verify(groupService).create(testGroup);
    }

    @Test
    void shouldReturnError400WhenIllegalArgumentExceptionWhileCreateGroup() throws Exception {
        int wrongId = 5;
        Group testGroup = new Group();
        testGroup.setName("Test group");
        testGroup.setFaculty(faculty);
        testGroup.setId(wrongId);

        when(facultyService.getById(faculty.getId())).thenReturn(faculty);
        doThrow(new ServiceException("Service exception", new IllegalArgumentException())).when(groupService).create(testGroup);

        String testJson = objectMapper.writeValueAsString(testGroup);
        
        mockMvc.perform(post("/groups")
                .content(testJson).param("faculty-id", Integer.toString(faculty.getId()))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnError500WhenServiceExceptionWhileCreateGroup() throws Exception {
        Group testGroup = new Group();
        testGroup.setName("Test group");
        testGroup.setFaculty(anotherFaculty);
        
        when(facultyService.getById(anotherFaculty.getId())).thenReturn(anotherFaculty);
        doThrow(ServiceException.class).when(groupService).create(testGroup);

        String testJson = objectMapper.writeValueAsString(testGroup);
        
        mockMvc.perform(post("/groups")
                .content(testJson).param("faculty-id", Integer.toString(anotherFaculty.getId()))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError());
    }

    @Test
    void shouldReturnError500WhenRepositoryExceptionWhileUpdateGroup() throws Exception {
        int groupId = 5;
        int facultyId = anotherFaculty.getId();
        Group testGroup = new Group();
        testGroup.setId(groupId);
        testGroup.setName("Test group");
        testGroup.setFaculty(faculty);

        doThrow(new ServiceException("Service exception", new RepositoryException())).when(facultyService).getById(facultyId);

        String testJson = objectMapper.writeValueAsString(testGroup);
        
        mockMvc.perform(patch("/groups/{id}", groupId)
                .content(testJson).param("faculty-id", Integer.toString(facultyId))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError());

        verify(facultyService).getById(facultyId);
    }

    @Test
    void shouldReturnError400WhenConstraintViolationExceptionWhileUpdate() throws Exception {
        int groupId = 2;
        int facultyId = faculty.getId();
        Group testGroup = new Group();
        testGroup.setId(groupId);
        testGroup.setName("Test group");
        testGroup.setFaculty(anotherFaculty);

        doThrow(new ServiceException("Service exception", new ConstraintViolationException(null))).when(facultyService).getById(facultyId);

        String testJson = objectMapper.writeValueAsString(testGroup);
        
        mockMvc.perform(patch("/groups/{id}", groupId)
                .content(testJson).param("faculty-id", Integer.toString(facultyId))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

        verify(facultyService).getById(facultyId);
    }
    
    @Test
    void shouldReturnError400WhenIllegalArgumentExceptionWhileUpdate() throws Exception {
        int groupId = 30;
        int facultyId = anotherFaculty.getId();
        Group testGroup = new Group();
        testGroup.setId(groupId);
        testGroup.setName("Test group");
        testGroup.setFaculty(faculty);

        doThrow(new ServiceException("Service exception", new IllegalArgumentException())).when(facultyService).getById(facultyId);

        String testJson = objectMapper.writeValueAsString(testGroup);
        
        mockMvc.perform(patch("/groups/{id}", groupId)
                .content(testJson).param("faculty-id", Integer.toString(facultyId))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

        verify(facultyService).getById(facultyId);
    }
    
    @Test
    void shouldReturnError500WhenServiceExceptionWhileUpdate() throws Exception {
        int groupId = 41;
        int facultyId = anotherFaculty.getId();
        Group testGroup = new Group();
        testGroup.setId(groupId);
        testGroup.setName("Test group");
        testGroup.setFaculty(faculty);

        doThrow(ServiceException.class).when(facultyService).getById(facultyId);

        String testJson = objectMapper.writeValueAsString(testGroup);
        
        mockMvc.perform(patch("/groups/{id}", groupId)
                .content(testJson).param("faculty-id", Integer.toString(facultyId))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError());

        verify(facultyService).getById(facultyId);
    }
    
    @Test
    void shouldReturnError500WhenRepositoryExceptionWhileDeleteGroup() throws Exception {
        int testId = 2;
        
        doThrow(new ServiceException("Service exception", new RepositoryException())).when(groupService).deleteById(testId);
        
        mockMvc.perform(delete("/groups/{id}", testId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError());
        
        verify(groupService).deleteById(testId);
    }
    
    @Test
    void shouldReturnError400WhenIllegalArgumentExceptionWhileDeleteGroup() throws Exception {
        int testId = 7;
        
        doThrow(new ServiceException("Service exception", new IllegalArgumentException())).when(groupService).deleteById(testId);
        
        mockMvc.perform(delete("/groups/{id}", testId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
        
        verify(groupService).deleteById(testId);
    }
    
    @Test
    void shouldReturnError500WhenServiceExceptionWhileDeleteGroup() throws Exception {
        int testId = 6;
        
        doThrow(ServiceException.class).when(groupService).deleteById(testId);
        
        mockMvc.perform(delete("/groups/{id}", testId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError());
        
        verify(groupService).deleteById(testId);
    }
}