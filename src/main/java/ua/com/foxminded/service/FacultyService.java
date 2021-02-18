package ua.com.foxminded.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ua.com.foxminded.dao.FacultyDAO;
import ua.com.foxminded.domain.Faculty;
import ua.com.foxminded.domain.Group;

@Service
public class FacultyService {
    private FacultyDAO facultyDAO;
    private GroupService groupService;

    @Autowired
    public FacultyService (FacultyDAO facultyDAO, GroupService groupService) {
        this.facultyDAO = facultyDAO;
        this.groupService = groupService;
    }

    public void createFaculty(Faculty faculty) {
        facultyDAO.create(faculty);
    }

    public List<Faculty> getAllFaculties() {
        List<Faculty> faculties = facultyDAO.findAll();
        List<Group> groups = groupService.getAllGroups();
        faculties.stream().forEach(faculty -> {
            List<Group> facultyGroups = new ArrayList<>();
            for(Group group : groups) {
                if(group.getFaculty().getId() == faculty.getId()) {
                    facultyGroups.add(group);
                }
            }
            faculty.setGroups(facultyGroups);
        });
        return faculties;
    }

    public Faculty getFacultyById(int facultyId) {
        Faculty faculty = facultyDAO.findById(facultyId);
        List<Group> groups = groupService.getAllGroups();
        faculty.setGroups(groups.stream().filter(group -> group.getFaculty().getId() == facultyId).collect(Collectors.toList()));
        return faculty;
    }

    public void updateFaculty (int facultyId, Faculty updatedFaculty) {
        facultyDAO.update(facultyId, updatedFaculty);
    }

    public void deleteFacultyById (int facultyId) {
        facultyDAO.deleteById(facultyId);
    }
}
