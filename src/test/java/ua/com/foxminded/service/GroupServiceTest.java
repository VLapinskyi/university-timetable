package ua.com.foxminded.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ua.com.foxminded.dao.GroupDAO;
import ua.com.foxminded.domain.Faculty;
import ua.com.foxminded.domain.Group;

class GroupServiceTest {
    @InjectMocks
    private GroupService groupService;
    @Mock
    private GroupDAO groupDAO;
    @Mock
    private StudentService studentService;
    
    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }
    
    @Test
    void shouldCreateGroup() {
        String groupName = "GroupName";
        Group group = new Group();
        group.setName(groupName);
        groupService.createGroup(groupName);
        verify(groupDAO).create(group);
    }
    
    @Test
    void shouldGetAllGroups() {
        String facultyName = "TestFaculty";
        int facultyId = 1;
        Faculty faculty = new Faculty();
        faculty.setId(facultyId);
        faculty.setName(facultyName);
    }
}