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
import ua.com.foxminded.domain.Lecturer;
import ua.com.foxminded.service.LecturerService;

@Tag(name = "lecturers", description = "This controller operates with lecturer's information.")
@RestController
@RequestMapping(value = "/lecturers", produces = "application/json")
public class LecturersRestController {

    private LecturerService lecturerService;

    @Autowired
    public LecturersRestController(LecturerService lecturerService) {
        this.lecturerService = lecturerService;
    }

    @Operation(summary = "Get all lecturers.")
    @GetMapping()
    public List<Lecturer> getLecturers() {
        return lecturerService.getAll();
    }

    @Operation(summary = "Get a lecturer by its id.")
    @GetMapping("/{id}")
    public Lecturer getLecturer(@Parameter(description = "Id of a lecturer to be getted.") @PathVariable("id") int id) {
        return lecturerService.getById(id);
    }

    @Operation(summary = "Create a lecturer.")
    @PostMapping()
    public Lecturer createLecturer(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Provide for creating a lecturer information without lecturer-id.",
            content =  @Content(examples = @ExampleObject(value = "{\"firstName\": \"string\", \"lastName\": \"string\", \"gender\": \"string\","
                    + "\"phoneNumber\": \"string\", \"email\": \"string\"}")))
            @RequestBody Lecturer lecturer) {
        lecturerService.create(lecturer);
        return lecturer;
    }

    @Operation(summary = "Update a lecturer.")
    @PatchMapping("/{id}")
    public Lecturer updateLecturer(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Provide for a lecturer with some id new information.")
            @RequestBody Lecturer lecturer) {
        lecturerService.update(lecturer);
        return lecturer;
    }

    @Operation(summary = "Delete a lecturer by its id.")
    @DeleteMapping("/{id}")
    public String deleteLecturer(@Parameter(description = "Id of a lecturer to be deleted.") @PathVariable("id") int id) {
        lecturerService.deleteById(id);
        return "Lecturer with id: " + id + " was deleted.";
    }
}
