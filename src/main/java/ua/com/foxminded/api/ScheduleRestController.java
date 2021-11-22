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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import ua.com.foxminded.domain.Group;
import ua.com.foxminded.domain.Lecturer;
import ua.com.foxminded.domain.Lesson;
import ua.com.foxminded.domain.LessonTime;
import ua.com.foxminded.service.GroupService;
import ua.com.foxminded.service.LecturerService;
import ua.com.foxminded.service.LessonService;
import ua.com.foxminded.service.LessonTimeService;

@Tag(name = "schedule", description = "This controller operates with schedule's information.")
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

    @Operation(summary = "Get week lessons for a lecturer.")
    @GetMapping("/week-lessons/find-for-lecturer")
    public Map<DayOfWeek, List<Lesson>> getLecturerWeekSchedule(@Parameter(description = "Id of a lecturer") @RequestParam("lecturer-id") int lecturerId) {
        return lessonService.getLecturerWeekLessons(lecturerId);
    }

    @Operation(summary = "Get month lessons for a lecturer.")
    @GetMapping("/month-lessons/find-for-lecturer")
    public Map<LocalDate, List<Lesson>> getLecturerMonthSchedule(@Parameter(description = "Id of a lecturer") @RequestParam("lecturer-id") int lecturerId,
            @Parameter(description = "Month value should be like \"yyyy-mm\"", schema = @Schema(pattern = "\\d\\d\\d\\d-\\d\\d")) @RequestParam("month-value") String monthValue) {

        YearMonth month = YearMonth.parse(monthValue);
        return lessonService.getLecturerMonthLessons(lecturerId, month);
    }

    @Operation(summary = "Get week lessons for a group.")
    @GetMapping("/week-lessons/find-for-group")
    public Map<DayOfWeek, List<Lesson>> getGroupWeekSchedule(@Parameter(description = "Id of a group") @RequestParam("group-id") int groupId) {
        
        return lessonService.getGroupWeekLessons(groupId);
    }
    
    @Operation(summary = "Get month lessons for a group.")
    @GetMapping("/month-lessons/find-for-group")
    public Map<LocalDate, List<Lesson>> getGroupMonthSchedule(@Parameter(description = "Id of a group") @RequestParam("group-id") int groupId,
            @Parameter(description = "Month value should be like \"yyyy-mm\"", schema = @Schema(pattern = "\\d\\d\\d\\d-\\d\\d")) @RequestParam("month-value") String monthValue) {
        
        YearMonth month = YearMonth.parse(monthValue);
        return lessonService.getGroupMonthLessons(groupId, month);
    }

    @Operation(summary = "Get all lesson-time-parameters.")
    @GetMapping("/lesson-time-parameters")
    public List<LessonTime> getLessonTimeParameters() {
        
        return lessonTimeService.getAll();
    }

    @Operation(summary = "Create a lesson-time-paratemeter.")
    @PostMapping("/lesson-time-parameters")
    public LessonTime createLessonTimeParameter(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Provide for creating a lesson-time-parameter start time and end time without id.", 
            content = @Content(examples = @ExampleObject(value = "{\"startTime\": \"hh:mm:ss\", \"endTime\": \"hh:mm:ss\"}")))
            @RequestBody LessonTime lessonTime) {
        lessonTimeService.create(lessonTime);
        return lessonTime;
    }

    @Operation(summary = "Update a lesson-time-parameter.")
    @PatchMapping("/lesson-time-parameters/{id}")
    public LessonTime updateLessonTimeParameter(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Provide for a lesson-time-parameter with some id new information.")
            @RequestBody LessonTime lessonTime) {
        lessonTimeService.update(lessonTime);
        return lessonTime;
    }

    @Operation(summary = "Delete a lesson-time-parameter by its id.")
    @DeleteMapping("/lesson-time-parameters/{id}")
    public String deleteLessonTimeParameter(@Parameter(description = "Id of a lesson-time-parameter to be deleted") @PathVariable("id") int id) {
        lessonTimeService.deleteById(id);
        return "LessonTime with id: " + id + " was deleted.";
    }

    @Operation(summary = "Update a lesson.")
    @PatchMapping("/lessons/{id}")
    public Lesson updateLesson(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Provide for a lesson with some id new information.",
            content = @Content(examples = @ExampleObject(value = "{\"id\": 0, \"name\": \"string\", \"audience\": \"string\", \"day\": \"string\"}")))
            @RequestBody Lesson lesson,
            @Parameter(description = "Provide for a lesson an id of an actual lesson-time-parameter") @RequestParam("lesson-time-id") int lessonTimeId,
            @Parameter(description = "Provide for a lesson an id of an actual lecturer") @RequestParam("lecturer-id") int lecturerId,
            @Parameter(description = "Provide for a lesson an id of an actual group") @RequestParam("group-id") int groupId) {
        LessonTime lessonTime = lessonTimeService.getById(lessonTimeId);
        Lecturer lecturer = lecturerService.getById(lecturerId);
        Group group = groupService.getById(groupId);

        lesson.setLessonTime(lessonTime);
        lesson.setLecturer(lecturer);
        lesson.setGroup(group);

        lessonService.update(lesson);
        return lesson;
    }

    @Operation(summary = "Create a lesson.")
    @PostMapping("/lessons")
    public Lesson createLesson(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Provide for a lesson with an information information without lesson-id.",
            content = @Content(examples = @ExampleObject(value = "{\"name\": \"string\", \"audience\": \"string\", \"day\": \"string\"}")))
            @RequestBody Lesson lesson,
            @Parameter(description = "Provide for a lesson an id of an actual lesson-time-parameter") @RequestParam("lesson-time-id") int lessonTimeId,
            @Parameter(description = "Provide for a lesson an id of an actual lecturer") @RequestParam("lecturer-id") int lecturerId,
            @Parameter(description = "Provide for a lesson an id of an actual group") @RequestParam("group-id") int groupId){
        LessonTime lessonTime = lessonTimeService.getById(lessonTimeId);
        Group group = groupService.getById(groupId);
        Lecturer lecturer = lecturerService.getById(lecturerId);

        lesson.setLessonTime(lessonTime);
        lesson.setGroup(group);
        lesson.setLecturer(lecturer);

        lessonService.create(lesson);
        return lesson;
    }

    @Operation(summary = "Delete a lesson by its id.")
    @DeleteMapping("/lessons/{id}")
    public String deleteLesson(@Parameter(description = "Id of a lesson to be deleted") @PathVariable("id") int id) {
        lessonService.deleteById(id);
        return "Lesson with id: " + id + " was deleted.";
    }
}
