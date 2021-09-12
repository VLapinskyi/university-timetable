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
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

import ua.com.foxminded.domain.Faculty;
import ua.com.foxminded.domain.Group;
import ua.com.foxminded.service.FacultyService;
import ua.com.foxminded.service.GroupService;

@Controller
@RequestMapping("/groups")
public class GroupsController {

	private GroupService groupService;
	private FacultyService facultyService;

	@Autowired
	public GroupsController(GroupService groupService, FacultyService facultyService) {
		this.groupService = groupService;
		this.facultyService = facultyService;
	}

	@GetMapping()
	public String getGroups(Model model) {
		model.addAttribute("pageTitle", "Groups");
		model.addAttribute("groups", groupService.getAll());
		return "groups/groups";
	}

	@GetMapping("/{id}")
	public String getGroup(@PathVariable("id") int id, Model model) {
		Group group = groupService.getById(id);
		model.addAttribute("pageTitle", group.getName());
		model.addAttribute("group", group);
		return "groups/group";
	}

	@GetMapping("/new")
	public String newGroup(@ModelAttribute("group") Group group, Model model) {
		model.addAttribute("pageTitle", "Create a new group");
		model.addAttribute("faculties", facultyService.getAll());
		return "groups/new";
	}

	@PostMapping()
	public String createGroup(@ModelAttribute("group") Group group, @RequestParam("faculty-value") int facultyId) {
		group.setFaculty(facultyService.getById(facultyId));
		groupService.create(group);
		return "redirect:/groups";
	}

	@GetMapping("/{id}/edit")
	public String editGroup(Model model, @PathVariable("id") int id) {
		Group group = groupService.getById(id);
		List<Faculty> faculties = facultyService.getAll();
		faculties.remove(group.getFaculty());
		model.addAttribute("pageTitle", "Edit " + group.getName());
		model.addAttribute("group", group);
		model.addAttribute("faculties", faculties);
		return "groups/edit";
	}

	@PatchMapping("/{id}")
	public String updateGroup(@ModelAttribute("group") Group group, @RequestParam("faculty-value") int facultyId) {
		Faculty faculty = facultyService.getById(facultyId);
		group.setFaculty(faculty);
		groupService.update(group);
		return "redirect:/groups";
	}
	
	@DeleteMapping("/{id}")
	public String deleteGroup(@PathVariable("id") int id) {
		groupService.deleteById(id);
		return "redirect:/groups";
	}
}
