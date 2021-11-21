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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import ua.com.foxminded.domain.Group;
import ua.com.foxminded.domain.Student;
import ua.com.foxminded.service.GroupService;
import ua.com.foxminded.service.StudentService;

@Tag(name = "students", description = "This controller operates with student's information.")
@RestController
@RequestMapping(value = "/students", produces = "application/json")
public class StudentsRestController {
    private StudentService studentService;
    private GroupService groupService;

    @Autowired
    public StudentsRestController(StudentService studentService, GroupService groupService) {
        this.studentService = studentService;
        this.groupService = groupService;
    }

    @Operation(summary = "Get all students.")
    @GetMapping()
    public List<Student> getStudents() {
        return studentService.getAll();
    }

    @Operation(summary = "Get a student by its id.")
    @GetMapping("/{id}")
    public Student getStudent(@Parameter(description = "Id of a student to be getted.") @PathVariable("id") int id) {
        return studentService.getById(id);
    }

    @Operation(summary = "Create a student.")
    @PostMapping()
    public Student createStudent(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Provide for creating a student information without student-id.",
            content =  @Content(examples = @ExampleObject(value = "{\"firstName\": \"string\", \"lastName\": \"string\", \"gender\": \"string\","
                    + "\"phoneNumber\": \"string\", \"email\": \"string\"}")))
            @RequestBody Student student,
            @Parameter(description = "Provide for a student group-id") @RequestParam("group-id") int groupId) {
        student.setGroup(groupService.getById(groupId));
        studentService.create(student);
        return student;
    }

    @Operation(summary = "Update a student.")
    @PatchMapping("/{id}")
    public Student updateStudent(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Provide for a student with some id new information.",
            content =  @Content(examples = @ExampleObject(value = "{\"id\": 0, \"firstName\": \"string\", \"lastName\": \"string\", \"gender\": \"string\","
                    + "\"phoneNumber\": \"string\", \"email\": \"string\"}")))
            @RequestBody Student student,
            @Parameter(description = "Provide for a student an id of an actual group") @RequestParam("group-id") int groupId) {
        Group group = groupService.getById(groupId);
        student.setGroup(group);
        studentService.update(student);
        return student;
    }

    @Operation(summary = "Delete a student by its id.")
    @DeleteMapping("/{id}")
    public String deleteStudent(@Parameter(description = "Id of a student to be deleted.") @PathVariable("id") int id) {
        studentService.deleteById(id);
        return "Student with id: " + id + " was deleted.";
    }
}