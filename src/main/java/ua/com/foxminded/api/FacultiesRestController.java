package ua.com.foxminded.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import ua.com.foxminded.domain.Faculty;
import ua.com.foxminded.service.FacultyService;

@Tag(name = "faculties", description = "This controller operates with faculty's information.")
@RestController
@RequestMapping(value = "/faculties", produces = "application/json")
public class FacultiesRestController {

    private FacultyService facultyService;

    @Autowired
    public FacultiesRestController(FacultyService facultyService) {
        this.facultyService = facultyService;
    }

    @Operation(summary = "Get all faculties.")
    @GetMapping()
    public List<Faculty> getFaculties() {
        return facultyService.getAll();
    }

    @Operation(summary = "Get a faculty by its id.")
    @GetMapping(value = "/{id}")
    public Faculty getFaculty(@Parameter(description = "Id of a faculty to be getted.") @PathVariable("id") int id) {
        return facultyService.getById(id);
    }

    @Operation(summary = "Create a faculty.")
    @PostMapping()
    public Faculty createFaculty(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Provide for creating a faculty a name.",
            content = @Content(examples = @ExampleObject(value = "{\"name\": \"string\"}")))
            @RequestBody Faculty faculty) {
        facultyService.create(faculty);
        return faculty;
    }

    @Operation(summary = "Update a faculty.")
    @PatchMapping("/{id}")
    public Faculty updateFaculty(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Provide for a faculty with some id a new name.")
            @RequestBody Faculty faculty) {
        facultyService.update(faculty);
        return faculty;
    }

    @Operation(summary = "Delete a faculty by its id.")
    @DeleteMapping("/{id}")
    public String deleteFaculty(@Parameter(description = "Id of a faculty to be deleted.") @PathVariable("id") int id) {
        facultyService.deleteById(id);
        return "Faculty with id: " + id + " was deleted.";
    }
}
