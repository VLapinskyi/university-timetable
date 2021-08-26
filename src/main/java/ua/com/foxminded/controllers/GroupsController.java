package ua.com.foxminded.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import ua.com.foxminded.domain.Group;
import ua.com.foxminded.service.GroupService;

@Controller
@RequestMapping("/groups")
public class GroupsController {
    
    private GroupService groupService;
    
    @Autowired
    public GroupsController(GroupService groupService) {
        this.groupService = groupService;
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
}
