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
import org.springframework.web.bind.annotation.RestController;

import ua.com.foxminded.domain.Faculty;
import ua.com.foxminded.service.FacultyService;

@RestController
@RequestMapping(value = "/faculties", produces = "application/json")
public class FacultiesRestController {

    private FacultyService facultyService;

    @Autowired
    public FacultiesRestController(FacultyService facultyService) {
        this.facultyService = facultyService;
    }

    @GetMapping()
    public List<Faculty> getFaculties() {
        return facultyService.getAll();
    }

    @GetMapping(value = "/{id}")
    public Faculty getFaculty(@PathVariable("id") int id) {
        return facultyService.getById(id);
    }

    @PostMapping()
    public Faculty createFaculty(@RequestBody Faculty faculty) {
        facultyService.create(faculty);
        return faculty;
    }

    @PatchMapping("/{id}")
    public Faculty updateFaculty(@RequestBody Faculty faculty) {
        facultyService.update(faculty);
        return faculty;
    }

    @DeleteMapping("/{id}")
    public String deleteFaculty(@PathVariable("id") int id) {
        facultyService.deleteById(id);
        return "Faculty with id: " + id + " was deleted.";
    }
}
