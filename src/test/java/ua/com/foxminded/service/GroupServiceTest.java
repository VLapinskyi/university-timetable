package ua.com.foxminded.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ua.com.foxminded.dao.GroupDAO;
import ua.com.foxminded.domain.Faculty;
import ua.com.foxminded.domain.Group;
import ua.com.foxminded.domain.Lesson;
import ua.com.foxminded.domain.Student;

class GroupServiceTest {
    @InjectMocks
    private GroupService groupService;
    @Mock
    private GroupDAO groupDAO;
    @Mock
    private StudentService studentService;
    @Mock
    private LessonService lessonService;
    
    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }
    
    @Test
    void shouldCreateGroup() {
        int facultyId = 2;
        Group savedGroup = new Group();
        savedGroup.setId(3);
        Group creatingGroup = new Group();
        creatingGroup.setId(4);
        when(groupDAO.findAll()).thenReturn(new ArrayList<Group>(Arrays.asList(savedGroup, creatingGroup)));
        groupService.createGroup(facultyId, creatingGroup);
        verify(groupDAO).create(creatingGroup);
        verify(groupDAO).setGroupFaculty(facultyId, creatingGroup.getId());
        
    }
    
    @Test
    void shouldGetAllGroups() {
        List<Faculty> faculties = new ArrayList<>(Arrays.asList(
                new Faculty(), new Faculty()));
        List<Integer> facultyIndexes = new ArrayList<>(Arrays.asList(1, 2));
        for (int i = 0; i < faculties.size(); i++) {
            faculties.get(i).setId(facultyIndexes.get(i));
        }
        
        List<Group> groups = new ArrayList<>(Arrays.asList(
                new Group(), new Group(), new Group()));
        List<Integer> groupsIndexes = new ArrayList<>(Arrays.asList(
                1, 2, 3));
        for (int i = 0; i < groups.size(); i++) {
            groups.get(i).setId(groupsIndexes.get(i));
        }
        
        List<Lesson> lessons = new ArrayList<>(Arrays.asList(
                new Lesson(), new Lesson(), new Lesson()));
        List<Integer> lessonIndexes = new ArrayList<>(Arrays.asList(1, 2, 3));
        for (int i = 0; i < lessons.size(); i++) {
            lessons.get(i).setId(lessonIndexes.get(i));
            lessons.get(i).setGroup(groups.get(i));
        }
        
        List<Student> students = new ArrayList<>(Arrays.asList(
                new Student(), new Student(), new Student(), new Student(),
                new Student(), new Student()));
        List<Integer> studentIndexes = new ArrayList<>(Arrays.asList(
                1, 2, 3, 4, 5, 6));
        for (int i = 0; i < students.size(); i++) {
            students.get(i).setId(studentIndexes.get(i));
            if (i < 3) {
                students.get(i).setGroup(groups.get(0));
            }
            else {
                students.get(i).setGroup(groups.get(1));
            }
        }
        
        List<Group> expectedGroups = new ArrayList<>(groups);
        expectedGroups.get(0).setFaculty(faculties.get(1));
        expectedGroups.get(1).setFaculty(faculties.get(1));
        expectedGroups.get(2).setFaculty(faculties.get(0));
        expectedGroups.get(0).setStudents(students.subList(0, 3));
        expectedGroups.get(1).setStudents(students.subList(3, students.size()));
        expectedGroups.get(0).setLessons(lessons.subList(0, 2));
        expectedGroups.get(1).setLessons(lessons.subList(2, lessons.size()));
        
        when(groupDAO.findAll()).thenReturn(groups);
        when(groupDAO.getGroupFaculty(groups.get(0).getId())).thenReturn(faculties.get(1));
        when(groupDAO.getGroupFaculty(groups.get(1).getId())).thenReturn(faculties.get(1));
        when(groupDAO.getGroupFaculty(groups.get(2).getId())).thenReturn(faculties.get(0));
        when(studentService.getAllStudents()).thenReturn(students);
        when(lessonService.getAllLessons()).thenReturn(lessons);
        
        List<Group> actualGroups = groupService.getAllGroups();
        assertTrue(expectedGroups.containsAll(actualGroups) && actualGroups.containsAll(expectedGroups));
        verify(groupDAO).findAll();
        for(int i = 0; i < groups.size(); i++) {
            verify(groupDAO).getGroupFaculty(groups.get(i).getId());
        }
        verify(studentService).getAllStudents();
        verify(lessonService).getAllLessons();
    }
    
    @Test
    void shouldGetGroupById() {
        int testGroupId = 1;
        Group group = new Group();
        group.setId(testGroupId);
        
        Faculty faculty = new Faculty();
        faculty.setId(1);
        group.setFaculty(faculty);
        
        List<Lesson> lessons = new ArrayList<>(Arrays.asList(
                new Lesson(), new Lesson()));
        lessons.get(0).setId(1);
        lessons.get(1).setId(2);
        lessons.stream().forEach(lesson -> lesson.setGroup(group));
        
        List<Student> students = new ArrayList<>(Arrays.asList(
                new Student(), new Student(), new Student()));
       students.get(0).setId(1);
       students.get(1).setId(2);
       students.get(2).setId(3);
       students.stream().forEach(student -> student.setGroup(group));
       
       Group expectedGroup = new Group();
       expectedGroup.setId(testGroupId);
       expectedGroup.setFaculty(faculty);
       expectedGroup.setLessons(lessons);
       expectedGroup.setStudents(students);
       
       when(groupDAO.findById(testGroupId)).thenReturn(group);
       when(groupDAO.getGroupFaculty(testGroupId)).thenReturn(faculty);
       when(lessonService.getAllLessons()).thenReturn(lessons);
       when(studentService.getAllStudents()).thenReturn(students);
       
       Group actualGroup = groupService.getGroupById(testGroupId);
       assertEquals(expectedGroup, actualGroup);
       verify(groupDAO).findById(testGroupId);
       verify(groupDAO).getGroupFaculty(testGroupId);
       verify(lessonService).getAllLessons();
       verify(studentService).getAllStudents();
    }
    
    @Test
    void shouldUpdateGroup() {
        int facultyId = 2;
        Faculty faculty = new Faculty();
        faculty.setId(facultyId);
        
        int groupId = 3;
        Group group = new Group();
        group.setId(groupId);
        group.setFaculty(faculty);
        
        groupService.updateGroup(groupId, group);
        verify(groupDAO).update(groupId, group);
        verify(groupDAO).setGroupFaculty(facultyId, groupId);
    }
    
    @Test
    void shouldDeleteById() {
        int groupId = 100;
        groupService.deleteGroupById(groupId);
        verify(groupDAO).deleteById(groupId);
    }
}