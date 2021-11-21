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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

import ua.com.foxminded.domain.Faculty;
import ua.com.foxminded.domain.Group;
import ua.com.foxminded.service.FacultyService;
import ua.com.foxminded.service.GroupService;

@Tag(name = "groups", description = "This controller operates with group's information.")
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

    @Operation(summary = "Get all groups.")
    @GetMapping()
    public List<Group> getGroups() {
        return groupService.getAll();
    }

    @Operation(summary = "Get a group by its id.")
    @GetMapping("/{id}")
    public Group getGroup(@Parameter(description = "Id of a group to be getted.") @PathVariable("id") int id) {
        return groupService.getById(id);
    }

    @Operation(summary = "Create a group.")
    @PostMapping()
    public Group createGroup(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Provide for creating a group its name.",
            content = @Content(examples = @ExampleObject(value = "{\"name\": \"string\"}")))
            @RequestBody Group group, 
            @Parameter(description = "Provide for a group faculty-id") @RequestParam("faculty-id") int facultyId) {
        group.setFaculty(facultyService.getById(facultyId));
        groupService.create(group);
        return group;
    }

    @Operation(summary = "Update a group.")
    @PatchMapping("/{id}")
    public Group updateGroup(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Provide for a group with some id new information.",
            content = @Content(examples = @ExampleObject(value = "{\"id\": 0, \"name\": \"string\"}")))
            @RequestBody Group group,
            @Parameter(description = "Provide for a group an id of an actual faculty") @RequestParam("faculty-id") int facultyId) {
        Faculty faculty = facultyService.getById(facultyId);
        group.setFaculty(faculty);
        groupService.update(group);
        return group;
    }

    @Operation(summary = "Delete a group by its id.")
    @DeleteMapping("/{id}")
    public String deleteGroup(@Parameter(description = "Id of a group to be deleted.") @PathVariable("id") int id) {
        groupService.deleteById(id);
        return "Group with id: " + id + " was deleted.";
    }
}
