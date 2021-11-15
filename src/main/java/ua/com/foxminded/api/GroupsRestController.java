package ua.com.foxminded.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import ua.com.foxminded.domain.Faculty;
import ua.com.foxminded.domain.Group;
import ua.com.foxminded.service.FacultyService;
import ua.com.foxminded.service.GroupService;

@RestController
@RequestMapping(value = "/groups", produces = "application/json")
public class GroupsRestController {

    private GroupService groupService;
    private FacultyService facultyService;

    @Autowired
    public GroupsRestController(GroupService groupService, FacultyService facultyService) {
        this.groupService = groupService;
        this.facultyService = facultyService;
    }

    @GetMapping()
    public List<Group> getGroups() {
        return groupService.getAll();
    }

    @GetMapping("/{id}")
    public Group getGroup(@PathVariable("id") int id) {
        return groupService.getById(id);
    }

    @PostMapping()
    public Group createGroup(@RequestBody Group group, @RequestParam("faculty-id") int facultyId) {
        group.setFaculty(facultyService.getById(facultyId));
        groupService.create(group);
        return group;
    }

    @PatchMapping("/{id}")
    public Group updateGroup(@RequestBody Group group, @RequestParam("faculty-id") int facultyId) {
        Faculty faculty = facultyService.getById(facultyId);
        group.setFaculty(faculty);
        groupService.update(group);
        return group;
    }

    @DeleteMapping("/{id}")
    public String deleteGroup(@PathVariable("id") int id) {
        groupService.deleteById(id);
        return "Group with id: " + id + " was deleted.";
    }
}
