package ua.com.foxminded.controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import ua.com.foxminded.domain.Gender;
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
    
    @GetMapping("/new")
    public String newLecturer(@ModelAttribute("lecturer") Lecturer lecturer, Model model) {
        model.addAttribute("genders", Gender.values());
        return "/lecturers/new";
    }
    
    @PostMapping()
    public String createLecturer(@ModelAttribute("lecturer") Lecturer lecturer, @RequestParam("gender-value") String gender) {
        lecturer.setGender(Gender.valueOf(gender));
        lecturerService.create(lecturer);
        return "redirect:/lecturers";
    }
    
    @GetMapping("/{id}/edit")
    public String editLecturer(Model model, @PathVariable("id") int id) {
        Lecturer lecturer = lecturerService.getById(id);
        List<Gender> genders = new ArrayList<>(Arrays.asList(Gender.values()));
        genders.remove(lecturer.getGender());
        model.addAttribute("pageTitle", "Edit " + lecturer.getFirstName() + " " + lecturer.getLastName());
        model.addAttribute("lecturer", lecturer);
        model.addAttribute("genders", genders);
        return "lecturers/edit";
    }
    
    @PatchMapping("/{id}")
    public String updateLecturer(@ModelAttribute("lecturer") Lecturer lecturer, @RequestParam("gender-value") Gender gender) {
        lecturer.setGender(gender);
        lecturerService.update(lecturer);
        return "redirect:/lecturers";
    }
}
