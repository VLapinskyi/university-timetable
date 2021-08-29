package ua.com.foxminded.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import ua.com.foxminded.domain.Gender;
import ua.com.foxminded.domain.Student;
import ua.com.foxminded.service.GroupService;
import ua.com.foxminded.service.StudentService;

@Controller
@RequestMapping("/students")
public class StudentsController {
    private StudentService studentService;
    private GroupService groupService;
    
    @Autowired
    public StudentsController (StudentService studentService, GroupService groupService) {
        this.studentService = studentService;
        this.groupService = groupService;
    }
    
    @GetMapping()
    public String getStudents(Model model) {
        model.addAttribute("pageTitle", "Students");
        model.addAttribute("students", studentService.getAll());
        return "students/students";
    }
    
    @GetMapping("/{id}")
    public String getStudent(@PathVariable("id") int id, Model model) {
        Student student = studentService.getById(id);
        model.addAttribute("pageTitle", student.getFirstName() + " " + student.getLastName());
        model.addAttribute("student", student);
        return "students/student";
    }
    
    @GetMapping("/new")
    public String newStudent(@ModelAttribute("student") Student student, Model model) {
        model.addAttribute("groups", groupService.getAll());
        return "/students/new";
    }
    
    @PostMapping()
    public String createStudent(@ModelAttribute("student") Student student, @RequestParam("group-value") int groupId, @RequestParam("gender-value") String gender) {
        student.setGender(Gender.valueOf(gender));
        student.setGroup(groupService.getById(groupId));
        studentService.create(student);
        return "redirect:/students";
    }
}