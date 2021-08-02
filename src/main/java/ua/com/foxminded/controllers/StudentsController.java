package ua.com.foxminded.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import ua.com.foxminded.domain.Student;
import ua.com.foxminded.service.StudentService;

@Controller
@RequestMapping("/students")
public class StudentsController {
    private StudentService studentService;
    
    @Autowired
    public StudentsController (StudentService studentService) {
        this.studentService = studentService;
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
}