package ua.com.foxminded.controllers;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
import ua.com.foxminded.domain.Group;
import ua.com.foxminded.service.GroupService;
import ua.com.foxminded.service.exceptions.ServiceException;
import ua.com.foxminded.settings.SpringConfiguration;

@ContextConfiguration(classes = { SpringConfiguration.class })
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
class GroupsControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private GroupsController groupsController;

    @Mock
    private GroupService groupService;

    private MockMvc mockMvc;

    private DAOException daoException = new DAOException("DAO exception",
            new QueryTimeoutException("Exception message"));
    private ServiceException serviceWithDAOException = new ServiceException("Service exception", daoException);

    private ServiceException serviceWithIllegalArgumentException = new ServiceException("Service exception",
            new IllegalArgumentException());

    private Faculty faculty = new Faculty();

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(groupsController, "groupService", groupService);

        faculty.setId(1);
        faculty.setName("Faculty");
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
    void shouldReturnError500WhenDAOExceptionWhileGetGroups() throws Exception {
        when(groupService.getAll()).thenThrow(serviceWithDAOException);

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
    void shouldReturnError500WhenDAOExceptionWhileGetGroup() throws Exception {
        int id = 4;

        when(groupService.getById(id)).thenThrow(serviceWithDAOException);

        mockMvc.perform(get("/groups/{id}", id)).andExpect(status().isInternalServerError());
        verify(groupService).getById(id);
    }

    @Test
    void shouldReturnError400WhenIllegalArgumentExceptionWhileGetGroup() throws Exception {
        int id = 1;
        when(groupService.getById(id)).thenThrow(serviceWithIllegalArgumentException);
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
}