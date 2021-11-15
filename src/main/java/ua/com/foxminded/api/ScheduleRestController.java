package ua.com.foxminded.api;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ua.com.foxminded.domain.Group;
import ua.com.foxminded.domain.Lecturer;
import ua.com.foxminded.domain.Lesson;
import ua.com.foxminded.domain.LessonTime;
import ua.com.foxminded.service.GroupService;
import ua.com.foxminded.service.LecturerService;
import ua.com.foxminded.service.LessonService;
import ua.com.foxminded.service.LessonTimeService;

@RestController
@RequestMapping(produces = "application/json")
public class ScheduleRestController {

    private LessonService lessonService;
    private LecturerService lecturerService;
    private GroupService groupService;
    private LessonTimeService lessonTimeService;

    @Autowired
    public ScheduleRestController(LessonService lessonService, LecturerService lecturerService, GroupService groupService,
            LessonTimeService lessonTimeService) {
        this.lessonService = lessonService;
        this.lecturerService = lecturerService;
        this.groupService = groupService;
        this.lessonTimeService = lessonTimeService;
    }

    @GetMapping(path = "/lessons", params = {"lecturer-id", "period=week" })
    public Map<DayOfWeek, List<Lesson>> getLecturerWeekSchedule(@RequestParam("lecturer-id") int lecturerId) {
        return lessonService.getLecturerWeekLessons(lecturerId);
    }

    @GetMapping(path = "/lessons", params = {"lecturer-id", "month-value" })
    public Map<LocalDate, List<Lesson>> getLecturerMonthSchedule(@RequestParam("lecturer-id") int lecturerId,
            @RequestParam("month-value") String monthValue) {

        YearMonth month = YearMonth.parse(monthValue);
        return lessonService.getLecturerMonthLessons(lecturerId, month);
    }

    @GetMapping(path = "/lessons", params = { "group-id", "period=week" })
    public Map<DayOfWeek, List<Lesson>> getGroupWeekSchedule(@RequestParam("group-id") int groupId) {
        
        return lessonService.getGroupWeekLessons(groupId);
    }
    @GetMapping(path = "/lessons", params = { "group-id", "month-value" })
    public Map<LocalDate, List<Lesson>> getGroupMonthSchedule(@RequestParam("group-id") int groupId,
            @RequestParam("month-value") String monthValue) {
        
        YearMonth month = YearMonth.parse(monthValue);
        return lessonService.getGroupMonthLessons(groupId, month);
    }

    @GetMapping("/lesson-time-parameters")
    public List<LessonTime> getLessonTimeParameters() {
        
        return lessonTimeService.getAll();
    }

    @PostMapping("/lesson-time-parameters")
    public LessonTime createLessonTimeParameter(@RequestBody LessonTime lessonTime) {
        lessonTimeService.create(lessonTime);
        return lessonTime;
    }

    @PatchMapping("/lesson-time-parameters/{id}")
    public LessonTime updateLessonTimeParameter(@RequestBody LessonTime lessonTime) {
        lessonTimeService.update(lessonTime);
        return lessonTime;
    }

    @DeleteMapping("/lesson-time-parameters/{id}")
    public String deleteLessonTimeParameter(@PathVariable("id") int id) {
        lessonTimeService.deleteById(id);
        return "LessonTime with id: " + id + " was deleted.";
    }

    @PatchMapping("/lessons/{id}")
    public Lesson updateLesson(@RequestBody Lesson lesson, @RequestParam Map<String, String> allParams) {
        LessonTime lessonTime = lessonTimeService.getById(Integer.parseInt(allParams.get("lesson-time-id")));
        Lecturer lecturer = lecturerService.getById(Integer.parseInt(allParams.get("lecturer-id")));
        Group group = groupService.getById(Integer.parseInt(allParams.get("group-id")));

        lesson.setLessonTime(lessonTime);
        lesson.setLecturer(lecturer);
        lesson.setGroup(group);

        lessonService.update(lesson);
        return lesson;
    }

    @PostMapping("/lessons")
    public Lesson createLesson(@RequestBody Lesson lesson, @RequestParam Map<String, String> allParams) {
        LessonTime lessonTime = lessonTimeService.getById(Integer.parseInt(allParams.get("lesson-time-id")));
        Group group = groupService.getById(Integer.parseInt(allParams.get("group-id")));
        Lecturer lecturer = lecturerService.getById(Integer.parseInt(allParams.get("lecturer-id")));

        lesson.setLessonTime(lessonTime);
        lesson.setGroup(group);
        lesson.setLecturer(lecturer);

        lessonService.create(lesson);
        return lesson;
    }

    @DeleteMapping("/lessons/{id}")
    public String deleteLesson(@PathVariable("id") int id) {
        lessonService.deleteById(id);
        return "Lesson with id: " + id + " was deleted.";
    }
}
