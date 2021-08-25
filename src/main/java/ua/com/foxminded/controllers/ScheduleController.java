package ua.com.foxminded.controllers;

import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import ua.com.foxminded.domain.Group;
import ua.com.foxminded.domain.Lecturer;
import ua.com.foxminded.service.GroupService;
import ua.com.foxminded.service.LecturerService;
import ua.com.foxminded.service.LessonService;

@Controller
public class ScheduleController {

    
    private LessonService lessonService;
    private LecturerService lecturerService;
    private GroupService groupService;
    
    @Autowired
    public ScheduleController (LessonService lessonService, LecturerService lecturerService, GroupService groupService) {
        this.lessonService = lessonService;
        this.lecturerService = lecturerService;
        this.groupService = groupService;
    }
    
    @GetMapping("/search-schedule")
    public String searchSchedule (Model model) {
        model.addAttribute("pageTitle", "Search schedule");
        model.addAttribute("lecturers", lecturerService.getAll());
        model.addAttribute("groups", groupService.getAll());
        return "schedule/search-schedule";
    }
    
    @GetMapping(path="/result-schedule", params={"people-role-radio=lecturer", "lecturer-value", "period-radio=week"})
    public String getLecturerWeekSchedule (@RequestParam("lecturer-value") int lecturerId, Model model) {
        Lecturer lecturer = lecturerService.getById(lecturerId);
        model.addAttribute("lecturer", lecturer);
        model.addAttribute("weekLessons", lessonService.getLecturerWeekLessons(lecturerId));
        model.addAttribute("pageTitle", "Schedule of a lecturer " + lecturer.getFirstName() + " " + lecturer.getLastName() + " for a week");
        
        return "schedule/lecturer-week-schedule";
    }
    
    @GetMapping(path="/result-schedule", params={"people-role-radio=lecturer", "lecturer-value", "period-radio=month", "month-value"})
    public String getLecturerMonthSchedule (@RequestParam("lecturer-value") int lecturerId, @RequestParam("month-value") String monthValue, Model model) {
        Lecturer lecturer = lecturerService.getById(lecturerId);
        YearMonth month = YearMonth.parse(monthValue);
        model.addAttribute("lecturer", lecturer);
        model.addAttribute("monthLessons", lessonService.getLecturerMonthLessons(lecturerId, month));
        model.addAttribute("pageTitle", "Schedule of a lecturer " + lecturer.getFirstName() + " " + lecturer.getLastName() + " for a month");
        model.addAttribute("month", month.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH));
        model.addAttribute("year", month.getYear());
        
        return "schedule/lecturer-month-schedule";
    }
    
    @GetMapping(path="/result-schedule", params={"people-role-radio=group", "group-value", "period-radio=week"})
    public String getGroupWeekSchedule (@RequestParam("group-value") int groupId, Model model) {
        Group group = groupService.getById(groupId);
        model.addAttribute("group", group);
        model.addAttribute("weekLessons", lessonService.getGroupWeekLessons(groupId));
        model.addAttribute("pageTitle", "Schedule of a group " + group.getName() + " for a week");
        
        return "schedule/group-week-schedule";
    }
    
    @GetMapping(path="/result-schedule", params={"people-role-radio=group", "group-value", "period-radio=month", "month-value"})
    public String getGroupMonthSchedule (@RequestParam("group-value") int groupId, @RequestParam("month-value") String monthValue, Model model) {
        Group group = groupService.getById(groupId);
        YearMonth month = YearMonth.parse(monthValue);
        model.addAttribute("group", group);
        model.addAttribute("monthLessons", lessonService.getGroupMonthLessons(groupId, month));
        model.addAttribute("pageTitle", "Schedule of a group " + group.getName() + " for a month");
        model.addAttribute("month", month.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH));
        model.addAttribute("year", month.getYear());
        
        return "schedule/group-month-schedule";
    }
}
