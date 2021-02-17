package ua.com.foxminded.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ua.com.foxminded.dao.GroupDAO;
import ua.com.foxminded.domain.Group;
import ua.com.foxminded.domain.Student;

@Service
public class GroupService {
    private GroupDAO groupDAO;
    private FacultyService facultyService;
    private StudentService studentService;
    
    @Autowired
    public GroupService (GroupDAO groupDAO, FacultyService facultyService, StudentService studentService) {
        this.groupDAO = groupDAO;
        this.facultyService = facultyService;
        this.studentService = studentService;
    }
    
    public void createGroup(int facultyId, Group group) {
        
        groupDAO.create(group);
    }
    
    public List<Group> getAllGroups() {
        List<Group> groups = groupDAO.findAll();
        groups.stream().forEach(group -> group.setFaculty(groupDAO.getGroupFaculty(group.getId())));
        List<Student> students = studentService.getAllStudents();
        groups.stream().forEach(group -> {
            List<Student> groupStudents = new ArrayList<>();
            for(Student student : students) {
                if (student.getGroup().equals(group)) {
                    groupStudents.add(student);
                }
            }
            group.setStudents(groupStudents);
        });
        return groups;
    }
    
    public Group getGroupById(int groupId) {
	return null;
    }
}
