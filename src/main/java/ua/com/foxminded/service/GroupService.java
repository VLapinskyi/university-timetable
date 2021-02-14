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
    private StudentService studentService;
    
    @Autowired
    public GroupService (GroupDAO groupDAO, StudentService studentService) {
        this.groupDAO = groupDAO;
        this.studentService = studentService;
    }
    
    public void createGroup(String groupName) {
        Group group = new Group();
        group.setName(groupName);
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
}
