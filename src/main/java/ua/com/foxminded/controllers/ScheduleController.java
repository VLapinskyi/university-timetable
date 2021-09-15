package ua.com.foxminded.controllers;

import java.time.DayOfWeek;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import ua.com.foxminded.domain.Group;
import ua.com.foxminded.domain.Lecturer;
import ua.com.foxminded.domain.Lesson;
import ua.com.foxminded.domain.LessonTime;
import ua.com.foxminded.service.GroupService;
import ua.com.foxminded.service.LecturerService;
import ua.com.foxminded.service.LessonService;
import ua.com.foxminded.service.LessonTimeService;

@Controller
public class ScheduleController {

    private LessonService lessonService;
    private LecturerService lecturerService;
    private GroupService groupService;
    private LessonTimeService lessonTimeService;

    @Autowired
    public ScheduleController(LessonService lessonService, LecturerService lecturerService, GroupService groupService,
            LessonTimeService lessonTimeService) {
        this.lessonService = lessonService;
        this.lecturerService = lecturerService;
        this.groupService = groupService;
        this.lessonTimeService = lessonTimeService;
    }

    @GetMapping("/search-schedule")
    public String searchSchedule(Model model) {
        model.addAttribute("pageTitle", "Search schedule");
        model.addAttribute("lecturers", lecturerService.getAll());
        model.addAttribute("groups", groupService.getAll());
        return "schedule/search-schedule/search-schedule";
    }

    @GetMapping(path = "/lessons", params = { "people-role-radio=lecturer", "lecturer-value", "period-radio=week" })
    public String getLecturerWeekSchedule(@RequestParam("lecturer-value") int lecturerId, Model model) {
        Lecturer lecturer = lecturerService.getById(lecturerId);
        model.addAttribute("lecturer", lecturer);
        model.addAttribute("weekLessons", lessonService.getLecturerWeekLessons(lecturerId));
        model.addAttribute("pageTitle",
                "Schedule of a lecturer " + lecturer.getFirstName() + " " + lecturer.getLastName() + " for a week");

        return "schedule/search-schedule/lecturer-week-schedule";
    }

    @GetMapping(path = "/lessons", params = { "people-role-radio=lecturer", "lecturer-value", "period-radio=month",
            "month-value" })
    public String getLecturerMonthSchedule(@RequestParam("lecturer-value") int lecturerId,
            @RequestParam("month-value") String monthValue, Model model) {
        Lecturer lecturer = lecturerService.getById(lecturerId);
        YearMonth month = YearMonth.parse(monthValue);
        model.addAttribute("lecturer", lecturer);
        model.addAttribute("monthLessons", lessonService.getLecturerMonthLessons(lecturerId, month));
        model.addAttribute("pageTitle",
                "Schedule of a lecturer " + lecturer.getFirstName() + " " + lecturer.getLastName() + " for a month");
        model.addAttribute("month", month.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH));
        model.addAttribute("year", month.getYear());

        return "schedule/search-schedule/lecturer-month-schedule";
    }

    @GetMapping(path = "/lessons", params = { "people-role-radio=group", "group-value", "period-radio=week" })
    public String getGroupWeekSchedule(@RequestParam("group-value") int groupId, Model model) {
        Group group = groupService.getById(groupId);
        model.addAttribute("group", group);
        model.addAttribute("weekLessons", lessonService.getGroupWeekLessons(groupId));
        model.addAttribute("pageTitle", "Schedule of a group " + group.getName() + " for a week");

        return "schedule/search-schedule/group-week-schedule";
    }

    @GetMapping(path = "/lessons", params = { "people-role-radio=group", "group-value", "period-radio=month",
            "month-value" })
    public String getGroupMonthSchedule(@RequestParam("group-value") int groupId,
            @RequestParam("month-value") String monthValue, Model model) {
        Group group = groupService.getById(groupId);
        YearMonth month = YearMonth.parse(monthValue);
        model.addAttribute("group", group);
        model.addAttribute("monthLessons", lessonService.getGroupMonthLessons(groupId, month));
        model.addAttribute("pageTitle", "Schedule of a group " + group.getName() + " for a month");
        model.addAttribute("month", month.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH));
        model.addAttribute("year", month.getYear());

        return "schedule/search-schedule/group-month-schedule";
    }

    @GetMapping("/lesson-time-parameters")
    public String getLessonTimeParameters(Model model) {
        model.addAttribute("pageTitle", "Lesson time parameters");
        model.addAttribute("lessonTimes", lessonTimeService.getAll());
        return "schedule/lesson-time-parameters/lesson-time-parameters";
    }

    @GetMapping("/lesson-time-parameters/new")
    public String addLessonParatemer(@ModelAttribute("lessonTime") LessonTime lessonTime) {
        return "schedule/lesson-time-parameters/new";
    }

    @PostMapping("lesson-time-parameters")
    public String addLessonTimeParameter(@ModelAttribute("lessonTIme") LessonTime lessonTime) {
        lessonTimeService.create(lessonTime);
        return "redirect:/lesson-time-parameters";
    }

    @GetMapping("/lesson-time-parameters/{id}/edit")
    public String editLessonTimeParameter(Model model, @PathVariable("id") int id) {
        LessonTime lessonTime = lessonTimeService.getById(id);
        model.addAttribute("pageTitle", "Edit lesson time parameter");
        model.addAttribute("lessonTime", lessonTime);
        return "/schedule/lesson-time-parameters/edit";
    }

    @PatchMapping("/lesson-time-parameters/{id}")
    public String updateLessonTimeParameter(@ModelAttribute("lessonTime") LessonTime lessonTime) {
        lessonTimeService.update(lessonTime);
        return "redirect:/lesson-time-parameters";
    }

    @DeleteMapping("{lesson-time-parameters/{id}")
    public String deleteLessonTimeParameter(@PathVariable("id") int id) {
        lessonTimeService.deleteById(id);
        return "redirect:/lesson-time-parameters";
    }

    @GetMapping("/lessons-new")
    public String addLesson(@ModelAttribute("lesson") Lesson lesson, Model model) {
        model.addAttribute("groups", groupService.getAll());
        model.addAttribute("lecturers", lecturerService.getAll());
        model.addAttribute("lessonTimes", lessonTimeService.getAll());
        return "/schedule/lessons/new";
    }

    @GetMapping("lessons/{id}/edit")
    public String editLesson(Model model, @PathVariable("id") int id) {
        Lesson lesson = lessonService.getById(id);

        List<DayOfWeek> days = new ArrayList<>(Arrays.asList(DayOfWeek.values()));
        days.remove(lesson.getDay());

        List<LessonTime> lessonTimes = lessonTimeService.getAll();
        lessonTimes.remove(lesson.getLessonTime());

        List<Lecturer> lecturers = lecturerService.getAll();
        lecturers.remove(lesson.getLecturer());

        List<Group> groups = groupService.getAll();
        groups.remove(lesson.getGroup());

        model.addAttribute("pageTitle", "Edit a lesson " + lesson.getName());
        model.addAttribute("lesson", lesson);
        model.addAttribute("days", days);
        model.addAttribute("lessonTimes", lessonTimes);
        model.addAttribute("lecturers", lecturers);
        model.addAttribute("groups", groups);
        return "/schedule/lessons/edit";
    }

    @PatchMapping("/lessons/{id}")
    public String updateLesson(@ModelAttribute("lesson") Lesson lesson, @RequestParam Map<String, String> allParams) {
        DayOfWeek day = DayOfWeek.valueOf(allParams.get("day-value"));
        LessonTime lessonTime = lessonTimeService.getById(Integer.parseInt(allParams.get("lesson-time-value")));
        Lecturer lecturer = lecturerService.getById(Integer.parseInt(allParams.get("lecturer-value")));
        Group group = groupService.getById(Integer.parseInt(allParams.get("group-value")));

        lesson.setDay(day);
        lesson.setLessonTime(lessonTime);
        lesson.setLecturer(lecturer);
        lesson.setGroup(group);

        lessonService.update(lesson);
        return "redirect:/search-schedule";
    }

    @PostMapping("/search-schedule")
    public String createLesson(@ModelAttribute("lesson") Lesson lesson, @RequestParam Map<String, String> allParams) {
        LessonTime lessonTime = lessonTimeService.getById(Integer.parseInt(allParams.get("lesson-time-value")));
        DayOfWeek day = DayOfWeek.valueOf(allParams.get("day-value"));
        Group group = groupService.getById(Integer.parseInt(allParams.get("group-value")));
        Lecturer lecturer = lecturerService.getById(Integer.parseInt(allParams.get("lecturer-value")));

        lesson.setLessonTime(lessonTime);
        lesson.setDay(day);
        lesson.setGroup(group);
        lesson.setLecturer(lecturer);

        lessonService.create(lesson);
        return "redirect:/search-schedule";
    }

    @DeleteMapping("/lessons/{id}")
    public String deleteLesson(@PathVariable("id") int id) {
        lessonService.deleteById(id);
        return "redirect:/search-schedule";
    }
}
