package ua.com.foxminded.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ua.com.foxminded.dao.GroupDAO;
import ua.com.foxminded.domain.Group;
import ua.com.foxminded.domain.Lesson;
import ua.com.foxminded.domain.Student;

@Service
public class GroupService {
    private GroupDAO groupDAO;
    private StudentService studentService;
    private LessonService lessonService;
    
    @Autowired
    public GroupService (GroupDAO groupDAO, StudentService studentService,
            LessonService lessonService) {
        this.groupDAO = groupDAO;
        this.studentService = studentService;
        this.lessonService = lessonService;
    }
    
    public void createGroup(int facultyId, Group group) {
        groupDAO.create(group);
        Optional<Group> createdGroup = groupDAO.findAll().stream().max(Comparator.comparing(Group :: getId));
        int groupId = 0;
        if(createdGroup.isPresent()) {
            groupId = createdGroup.get().getId();
        }
        groupDAO.setGroupFaculty(facultyId, groupId);
    }
    
    public List<Group> getAllGroups() {
        List<Group> groups = groupDAO.findAll();
        groups.stream().forEach(group -> group.setFaculty(groupDAO.getGroupFaculty(group.getId())));
        List<Student> students = studentService.getAllStudents();
        groups.stream().forEach(group -> {
            List<Student> groupStudents = new ArrayList<>();
            for(Student student : students) {
                if (student.getGroup().getId() == group.getId()) {
                    groupStudents.add(student);
                }
            }
            group.setStudents(groupStudents);
        });
        List<Lesson> lessons = lessonService.getAllLessons();
        groups.stream().forEach(group -> {
            List<Lesson> groupLessons = new ArrayList<>();
            for (Lesson lesson : lessons) {
                if (lesson.getGroup().getId() == group.getId()) {
                    groupLessons.add(lesson);
                }
            }
            group.setLessons(groupLessons);
        });
        return groups;
    }
    
    public Group getGroupById(int groupId) {
        Group group = groupDAO.findById(groupId);
        group.setFaculty(groupDAO.getGroupFaculty(groupId));
        group.setStudents(studentService.getAllStudents().stream().filter(student -> student.getGroup().getId() == group.getId())
                .collect(Collectors.toList()));
        group.setLessons(lessonService.getAllLessons().stream().filter(lesson -> lesson.getGroup().getId() == group.getId())
                .collect(Collectors.toList()));
        return group;
    }
    
    public void updateGroup (int groupId, Group updatedGroup) {
        groupDAO.update(groupId, updatedGroup);
        groupDAO.setGroupFaculty(updatedGroup.getFaculty().getId(), groupId);
    }
    
    public void deleteGroupById (int groupId) {
        groupDAO.deleteById(groupId);
    }
}
