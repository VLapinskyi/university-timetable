package ua.com.foxminded.controllers;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import javax.validation.ConstraintViolationException;

import ua.com.foxminded.controllers.aspects.GeneralControllerAspect;
import ua.com.foxminded.domain.Faculty;
import ua.com.foxminded.domain.Group;
import ua.com.foxminded.repositories.exceptions.RepositoryException;
import ua.com.foxminded.service.FacultyService;
import ua.com.foxminded.service.GroupService;
import ua.com.foxminded.service.exceptions.ServiceException;

@WebMvcTest(GroupsController.class)
@Import({AopAutoConfiguration.class, GeneralControllerAspect.class})
class GroupsControllerTest {

    @Autowired
    private GroupsController groupsController;

    @MockBean
    private GroupService groupService;

    @MockBean
    private FacultyService facultyService;

    @Autowired
    private MockMvc mockMvc;

    private Faculty faculty = new Faculty();
    private Faculty anotherFaculty = new Faculty();

    @BeforeEach
    void setUp() throws Exception {
        ReflectionTestUtils.setField(groupsController, "groupService", groupService);
        ReflectionTestUtils.setField(groupsController, "facultyService", facultyService);

        faculty.setId(1);
        faculty.setName("Faculty");

        anotherFaculty.setId(2);
        anotherFaculty.setName("Another Faculty");
    }

    @Test
    void shouldAddToModelListWhenGetGroups() throws Exception {
        Group firstGroup = new Group();
        firstGroup.setId(1);
        firstGroup.setName("First group");
        firstGroup.setFaculty(faculty);

        Group secondGroup = new Group();
        secondGroup.setId(2);
        secondGroup.setName("Second group");
        secondGroup.setFaculty(faculty);

        when(groupService.getAll()).thenReturn(Arrays.asList(firstGroup, secondGroup));

        mockMvc.perform(get("/groups")).andExpect(status().isOk()).andExpect(view().name("groups/groups"))
        .andExpect(model().attribute("pageTitle", equalTo("Groups")))
        .andExpect(model().attribute("groups", hasSize(2)))
        .andExpect(model().attribute("groups",
                hasItem(allOf(hasProperty("id", is(1)), hasProperty("name", is("First group")),
                        hasProperty("faculty", equalTo(faculty))))))
        .andExpect(model().attribute("groups", hasItem(allOf(hasProperty("id", is(2)),
                hasProperty("name", is("Second group")), hasProperty("faculty", equalTo(faculty))))));

        verify(groupService).getAll();
    }

    @Test
    void shouldAddToModelFoundedEntityWhenGetGroup() throws Exception {
        int id = 2;
        Group group = new Group();
        group.setId(id);
        group.setName("Group");
        group.setFaculty(faculty);

        when(groupService.getById(id)).thenReturn(group);

        mockMvc.perform(get("/groups/{id}", id)).andExpect(status().isOk()).andExpect(view().name("groups/group"))
        .andExpect(model().attribute("pageTitle", equalTo(group.getName())))
        .andExpect(model().attribute("group", hasProperty("id", is(id))))
        .andExpect(model().attribute("group", hasProperty("name", is("Group"))))
        .andExpect(model().attribute("group", hasProperty("faculty", equalTo(faculty))));

        verify(groupService).getById(id);
    }

    @Test
    void shouldGenerateRightPageWhenNewGroup() throws Exception {
        List<Faculty> faculties = new ArrayList<>(Arrays.asList(faculty));

        when(facultyService.getAll()).thenReturn(faculties);

        mockMvc.perform(get("/groups/new"))
        .andExpect(status().isOk())
        .andExpect(view().name("groups/new"))
        .andExpect(model().attribute("pageTitle", "Create a new group"))
        .andExpect(model().attribute("faculties", equalTo(faculties)));

        verify(facultyService).getAll();
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

        mockMvc.perform(post("/groups").flashAttr("group", testGroup).param("faculty-value", Integer.toString(facultyId)))
        .andExpect(view().name("redirect:/groups")).andExpect(status().is3xxRedirection());

        verify(facultyService).getById(facultyId);
        verify(groupService).create(expectedGroup);

    }

    @Test
    void shouldGenerateRightPageWhenEditGroup()  throws Exception {
        int groupId = 1;

        Group testGroup = new Group();
        testGroup.setId(groupId);
        testGroup.setName("Test group");
        testGroup.setFaculty(faculty);

        List<Faculty> allFaculties = new ArrayList<>(Arrays.asList(faculty, anotherFaculty));

        List<Faculty> expectedListFaculties = new ArrayList<>(Arrays.asList(anotherFaculty));

        when(groupService.getById(groupId)).thenReturn(testGroup);
        when(facultyService.getAll()).thenReturn(allFaculties);

        mockMvc.perform(get("/groups/{id}/edit", groupId))
        .andExpect(view().name("groups/edit"))
        .andExpect(model().attribute("pageTitle", equalTo("Edit " + testGroup.getName())))
        .andExpect(model().attribute("group", equalTo(testGroup)))
        .andExpect(model().attribute("faculties", expectedListFaculties));

        verify(groupService).getById(groupId);
        verify(facultyService).getAll();
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

        mockMvc.perform(patch("/groups/{id}", groupId).flashAttr("group", testGroup).param("faculty-value", Integer.toString(anotherFaculty.getId())))
        .andExpect(view().name("redirect:/groups"))
        .andExpect(status().is3xxRedirection());

        verify(facultyService).getById(anotherFaculty.getId());
        verify(groupService).update(expectedGroup);
    }

    @Test
    void shouldDeleteGroup() throws Exception {
        int groupId = 4;

        mockMvc.perform(delete("/groups/{id}", groupId))
        .andExpect(view().name("redirect:/groups"))
        .andExpect(status().is3xxRedirection());

        verify(groupService).deleteById(groupId);
    }

    @Test
    void shouldReturnError500WhenRepositoryExceptionWhileGetGroups() throws Exception {
        when(groupService.getAll()).thenThrow(new ServiceException("Service exception", new RepositoryException()));

        mockMvc.perform(get("/groups")).andExpect(status().isInternalServerError());
        verify(groupService).getAll();
    }

    @Test
    void shouldReturnError404WhenServiceExceptionWhileGetGroups() throws Exception {
        when(groupService.getAll()).thenThrow(ServiceException.class);

        mockMvc.perform(get("/groups")).andExpect(status().isNotFound());
        verify(groupService).getAll();
    }    

    @Test
    void shouldReturnError500WhenRepositoryExceptionWhileGetGroup() throws Exception {
        int id = 4;

        when(groupService.getById(id)).thenThrow(new ServiceException("Service exception", new RepositoryException()));

        mockMvc.perform(get("/groups/{id}", id)).andExpect(status().isInternalServerError());
        verify(groupService).getById(id);
    }

    @Test
    void shouldReturnError400WhenIllegalArgumentExceptionWhileGetGroup() throws Exception {
        int id = 1;
        when(groupService.getById(id)).thenThrow(new ServiceException("Service exception", new IllegalArgumentException()));
        mockMvc.perform(get("/groups/{id}", id)).andExpect(status().isBadRequest());
        verify(groupService).getById(id);
    }

    @Test
    void shouldReturnError404WhenEntityIsNotFoundWhileGetGroup() throws Exception {
        int id = 2;

        when(groupService.getById(id)).thenThrow(ServiceException.class);
        mockMvc.perform(get("/groups/{id}", id)).andExpect(status().isNotFound());
        verify(groupService).getById(id);
    }

    @Test
    void shouldReturnError500WhenRepositoryExceptionWhileNewGroup() throws Exception {
        doThrow(new ServiceException("Service exception", new RepositoryException()))
            .when(facultyService).getAll();

        mockMvc.perform(get("/groups/new"))
        .andExpect(status().isInternalServerError());

        verify(facultyService).getAll();
    }

    @Test
    void shouldReturnError500WhenServiceExceptionWhileNewGroup() throws Exception {
        doThrow(ServiceException.class).when(facultyService).getAll();

        mockMvc.perform(get("/groups/new"))
        .andExpect(status().isInternalServerError());

        verify(facultyService).getAll();
    }

    @Test
    void shouldReturnError500WhenRepositoryExceptionWhileCreateGroup() throws Exception {
        Group testGroup = new Group();
        testGroup.setName("Test group");
        testGroup.setFaculty(faculty);

        doThrow(new ServiceException("Service exception", new RepositoryException()))
            .when(facultyService).getById(faculty.getId());

        mockMvc.perform(post("/groups").flashAttr("group", testGroup)
                .param("faculty-value", Integer.toString(faculty.getId())))
        .andExpect(status().isInternalServerError());
        verify(facultyService).getById(faculty.getId());
    }

    @Test
    void shouldReturnError400WhenConstrantViolationExceptionWhileCreateGroup() throws Exception {
        Group testGroup = new Group();
        testGroup.setName(" Wrong name");
        testGroup.setFaculty(anotherFaculty);

        doThrow(new ServiceException("Service exception", new ConstraintViolationException(null))).when(groupService).create(testGroup);

        mockMvc.perform(post("/groups").flashAttr("group", testGroup)
                .param("faculty-value", Integer.toString(anotherFaculty.getId())))
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

        doThrow(new ServiceException("Service exception", new IllegalArgumentException())).when(groupService).create(testGroup);

        mockMvc.perform(post("/groups").flashAttr("group", testGroup)
                .param("faculty-value", Integer.toString(faculty.getId())))
        .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnError500WhenServiceExceptionWhileCreateGroup() throws Exception {
        Group testGroup = new Group();
        testGroup.setName("Test group");
        testGroup.setFaculty(anotherFaculty);

        doThrow(ServiceException.class).when(groupService).create(testGroup);

        mockMvc.perform(post("/groups").flashAttr("group", testGroup)
                .param("faculty-value", Integer.toString(anotherFaculty.getId())))
        .andExpect(status().isInternalServerError());
    }

    @Test
    void shouldReturnError500WhenRepositoryExceptionWhileEditGroup() throws Exception {
        int groupId = 1;
        Group testGroup = new Group();
        testGroup.setId(groupId);
        testGroup.setName("Test group");
        testGroup.setFaculty(faculty);

        when(groupService.getById(groupId)).thenReturn(testGroup);
        doThrow(new ServiceException("Service exception", new RepositoryException()))
            .when(facultyService).getAll();

        mockMvc.perform(get("/groups/{id}/edit", groupId))
        .andExpect(status().isInternalServerError());

        verify(groupService).getById(groupId);
        verify(facultyService).getAll();
    }

    @Test
    void shouldReturnError500WhenServiceExceptionWhileEditGroup() throws Exception {
        int groupId = 2;
        Group testGroup = new Group();
        testGroup.setId(groupId);
        testGroup.setName("Test Group");
        testGroup.setFaculty(faculty);

        when(groupService.getById(groupId)).thenReturn(testGroup);
        doThrow(ServiceException.class).when(facultyService).getAll();

        mockMvc.perform(get("/groups/{id}/edit", groupId))
        .andExpect(status().isInternalServerError());

        verify(groupService).getById(groupId);
        verify(facultyService).getAll();
    }

    @Test
    void shouldReturnError500WhenRepositoryExceptionWhileUpdateGroup() throws Exception {
        int groupId = 5;
        int facultyId = anotherFaculty.getId();
        Group testGroup = new Group();
        testGroup.setId(groupId);
        testGroup.setName("Test group");
        testGroup.setFaculty(faculty);

        doThrow(new ServiceException("Service exception", new RepositoryException()))
            .when(facultyService).getById(facultyId);

        mockMvc.perform(patch("/groups/{id}", groupId)
                .flashAttr("group", testGroup)
                .param("faculty-value", Integer.toString(facultyId)))
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

        mockMvc.perform(patch("/groups/{id}", groupId)
                .flashAttr("group", testGroup)
                .param("faculty-value", Integer.toString(facultyId)))
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

        mockMvc.perform(patch("/groups/{id}", groupId)
                .flashAttr("group", testGroup)
                .param("faculty-value", Integer.toString(facultyId)))
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

        mockMvc.perform(patch("/groups/{id}", groupId)
                .flashAttr("group", testGroup)
                .param("faculty-value", Integer.toString(facultyId)))
        .andExpect(status().isInternalServerError());

        verify(facultyService).getById(facultyId);
    }
    
    @Test
    void shouldReturnError500WhenRepositoryExceptionWhileDeleteGroup() throws Exception {
        int testId = 2;
        
        doThrow(new ServiceException("Service exception", new RepositoryException()))
            .when(groupService).deleteById(testId);
        
        mockMvc.perform(delete("/groups/{id}", testId))
        .andExpect(status().isInternalServerError());
        
        verify(groupService).deleteById(testId);
    }
    
    @Test
    void shouldReturnError400WhenIllegalArgumentExceptionWhileDeleteGroup() throws Exception {
        int testId = 7;
        
        doThrow(new ServiceException("Service exception", new IllegalArgumentException())).when(groupService).deleteById(testId);
        
        mockMvc.perform(delete("/groups/{id}", testId))
        .andExpect(status().isBadRequest());
        
        verify(groupService).deleteById(testId);
    }
    
    @Test
    void shouldReturnError500WhenServiceExceptionWhileDeleteGroup() throws Exception {
        int testId = 6;
        
        doThrow(ServiceException.class).when(groupService).deleteById(testId);
        
        mockMvc.perform(delete("/groups/{id}", testId))
        .andExpect(status().isInternalServerError());
        
        verify(groupService).deleteById(testId);
    }
}