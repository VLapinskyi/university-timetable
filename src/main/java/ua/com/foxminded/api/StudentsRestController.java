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

import ua.com.foxminded.domain.Group;
import ua.com.foxminded.domain.Student;
import ua.com.foxminded.service.GroupService;
import ua.com.foxminded.service.StudentService;

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

    @GetMapping()
    public List<Student> getStudents() {
        return studentService.getAll();
    }

    @GetMapping("/{id}")
    public Student getStudent(@PathVariable("id") int id) {
        return studentService.getById(id);
    }

    @PostMapping()
    public Student createStudent(@RequestBody Student student, @RequestParam("group-id") int groupId) {
        student.setGroup(groupService.getById(groupId));
        studentService.create(student);
        return student;
    }

    @PatchMapping("/{id}")
    public Student updateStudent(@RequestBody Student student, @RequestParam("group-id") int groupId) {
        Group group = groupService.getById(groupId);
        student.setGroup(group);
        studentService.update(student);
        return student;
    }

    @DeleteMapping("/{id}")
    public String deleteStudent(@PathVariable("id") int id) {
        studentService.deleteById(id);
        return "Student with id: " + id + " was deleted.";
    }
}