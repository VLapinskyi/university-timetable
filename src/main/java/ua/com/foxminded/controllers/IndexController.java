package ua.com.foxminded.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

	@GetMapping("/")
	public String getMainPage(Model model) {
		model.addAttribute("pageTitle", "University");
		return "index";
	}
}
