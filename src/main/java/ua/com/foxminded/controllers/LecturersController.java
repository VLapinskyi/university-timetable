package ua.com.foxminded.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import ua.com.foxminded.domain.Lecturer;
import ua.com.foxminded.service.LecturerService;

@Controller
@RequestMapping("/lecturers")
public class LecturersController {
    
    private LecturerService lecturerService;
    
    @Autowired
    public LecturersController(LecturerService lecturerService) {
        this.lecturerService = lecturerService;
    }
    
    @GetMapping()
    public String getLecturers(Model model) {
        model.addAttribute("pageTitle", "Lecturers");
        model.addAttribute("lecturers", lecturerService.getAll());
        return "lecturers/lecturers";
    }
    
    @GetMapping("/{id}")
    public String getLecturer(@PathVariable("id") int id, Model model) {
        Lecturer lecturer = lecturerService.getById(id);
        model.addAttribute("pageTitle", lecturer.getFirstName() + " " + lecturer.getLastName());
        model.addAttribute("lecturer", lecturer);
        return "lecturers/lecturer";
    }
}
