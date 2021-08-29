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
    public String newGroup (@ModelAttribute("group") Group group, Model model) {
        model.addAttribute("faculties", facultyService.getAll());
        return "groups/new";
    }
    
    @PostMapping()
    public String createGroup(@ModelAttribute("group") Group group, @RequestParam("faculty-value") int facultyId) {
        group.setFaculty(facultyService.getById(facultyId));
        groupService.create(group);
        return "redirect:/groups";
    }
}
