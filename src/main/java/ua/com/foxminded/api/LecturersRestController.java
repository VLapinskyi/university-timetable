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

import ua.com.foxminded.domain.Lecturer;
import ua.com.foxminded.service.LecturerService;

@RestController
@RequestMapping(value = "/lecturers", produces = "application/json")
public class LecturersRestController {

    private LecturerService lecturerService;

    @Autowired
    public LecturersRestController(LecturerService lecturerService) {
        this.lecturerService = lecturerService;
    }

    @GetMapping()
    public List<Lecturer> getLecturers() {
        return lecturerService.getAll();
    }

    @GetMapping("/{id}")
    public Lecturer getLecturer(@PathVariable("id") int id) {
        return lecturerService.getById(id);
    }

    @PostMapping()
    public Lecturer createLecturer(@RequestBody Lecturer lecturer) {
        lecturerService.create(lecturer);
        return lecturer;
    }

    @PatchMapping("/{id}")
    public Lecturer updateLecturer(@RequestBody Lecturer lecturer) {
        lecturerService.update(lecturer);
        return lecturer;
    }

    @DeleteMapping("/{id}")
    public String deleteLecturer(@PathVariable("id") int id) {
        lecturerService.deleteById(id);
        return "Lecturer with id: " + id + " was deleted.";
    }
}
