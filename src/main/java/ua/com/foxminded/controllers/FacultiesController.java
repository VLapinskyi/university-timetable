package ua.com.foxminded.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import ua.com.foxminded.domain.Faculty;
import ua.com.foxminded.service.FacultyService;

@Controller
@RequestMapping("/faculties")
public class FacultiesController {

	private FacultyService facultyService;

	@Autowired
	public FacultiesController(FacultyService facultyService) {
		this.facultyService = facultyService;
	}

	@GetMapping()
	public String getFaculties(Model model) {
		model.addAttribute("pageTitle", "Faculties");
		model.addAttribute("faculties", facultyService.getAll());
		return "faculties/faculties";
	}

	@GetMapping("/{id}")
	public String getFaculty(@PathVariable("id") int id, Model model) {
		Faculty faculty = facultyService.getById(id);
		model.addAttribute("pageTitle", faculty.getName());
		model.addAttribute("faculty", faculty);

		return "faculties/faculty";
	}

	@GetMapping("/new")
	public String newFaculty(@ModelAttribute("faculty") Faculty faculty, Model model) {
		model.addAttribute("pageTitle", "Create a new faculty");
		return "faculties/new";
	}

	@PostMapping()
	public String createFaculty(@ModelAttribute("faculty") Faculty faculty) {
		facultyService.create(faculty);
		return "redirect:/faculties";
	}

	@GetMapping("/{id}/edit")
	public String editFaculty(Model model, @PathVariable("id") int id) {
		Faculty faculty = facultyService.getById(id);
		model.addAttribute("pageTitle", "Edit " + faculty.getName());
		model.addAttribute("faculty", faculty);
		return "faculties/edit";
	}

	@PatchMapping("/{id}")
	public String updateFaculty(@ModelAttribute("faculty") Faculty faculty) {
		facultyService.update(faculty);
		return "redirect:/faculties";
	}
	
	@DeleteMapping("/{id}")
	public String deleteFaculty(@PathVariable("id") int id) {
		facultyService.deleteById(id);
		return "redirect:/faculties";
	}
}
