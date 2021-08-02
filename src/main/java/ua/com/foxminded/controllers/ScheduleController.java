package ua.com.foxminded.controllers;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import ua.com.foxminded.domain.Group;
import ua.com.foxminded.domain.Lecturer;
import ua.com.foxminded.domain.Lesson;
import ua.com.foxminded.service.GroupService;
import ua.com.foxminded.service.LecturerService;
import ua.com.foxminded.service.LessonService;

@Controller
public class ScheduleController {
    
    private static final String PEOPLE_ROLE_PARAMETER = "people-role-radio";
    private static final String PERIOD_PARAMETER = "period-radio";
    private static final String RADIO_LECTURER_NAME = "lecturer";
    private static final String RADIO_GROUP_NAME = "group";
    private static final String INPUT_FIELD_LECTURER_NAME = "lecturer-value";
    private static final String INPUT_FIELD_GROUP_NAME = "group-value";
    private static final String RADIO_WEEK_NAME = "week";
    private static final String RADIO_MONTH_NAME = "month";
    private static final String INPUT_FIELD_MONTH_NAME = "month-value";
    
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
    
    @GetMapping("/result-schedule")
    public String resultSchedule(HttpServletRequest request, Model model) {
        boolean isLecturer = request.getParameter(PEOPLE_ROLE_PARAMETER).equals(RADIO_LECTURER_NAME);
        boolean isGroup = request.getParameter(PEOPLE_ROLE_PARAMETER).equals(RADIO_GROUP_NAME);
        boolean isWeek = request.getParameter(PERIOD_PARAMETER).equals(RADIO_WEEK_NAME);
        boolean isMonth = request.getParameter(PERIOD_PARAMETER).equals(RADIO_MONTH_NAME);
        int lecturerId = 0;
        int groupId = 0;
        String pageTitle = "";
        
        if (isLecturer) {
            lecturerId = Integer.parseInt(request.getParameter(INPUT_FIELD_LECTURER_NAME));
            Lecturer lecturer = lecturerService.getById(lecturerId);
            model.addAttribute("lecturer", lecturer);
            pageTitle += "Schedule of a lecturer " + lecturer.getFirstName() + " " + lecturer.getLastName();
        } else if (isGroup) {
            groupId = Integer.parseInt(request.getParameter(INPUT_FIELD_GROUP_NAME));
            Group group = groupService.getById(groupId);
            model.addAttribute("group", group);
            pageTitle += "Schedule of a group " + group.getName();
            
        }
        
        if (isWeek) {
            Map<DayOfWeek,List<Lesson>> weekLessons = null;
            pageTitle += " for a week";
            
            if (isLecturer) {
                weekLessons = lessonService.getLecturerWeekLessons(lecturerId);
            } else if (isGroup) {
                weekLessons = lessonService.getGroupWeekLessons(groupId);
            }
            
            model.addAttribute("weekLessons", weekLessons);
            
            
        } else if (isMonth) {
            YearMonth month = YearMonth.parse(request.getParameter(INPUT_FIELD_MONTH_NAME));
            Map<LocalDate, List<Lesson>> monthLessons = null;
            pageTitle += " for a month";
            
            if (isLecturer) {
                monthLessons = lessonService.getLecturerMonthLessons(lecturerId, month);
            } else if (isGroup) {
                monthLessons = lessonService.getGroupMonthLessons(groupId, month);
            }
            model.addAttribute("monthLessons", monthLessons);
            model.addAttribute("month", month.getMonth());
            model.addAttribute("year", month.getYear());
        }
        model.addAttribute("pageTitle", pageTitle);
        return "schedule/result-schedule";
    }
}
