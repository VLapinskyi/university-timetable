package ua.com.foxminded.controllers;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.persistence.QueryTimeoutException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import jakarta.validation.ConstraintViolationException;
import ua.com.foxminded.domain.Faculty;
import ua.com.foxminded.domain.Gender;
import ua.com.foxminded.domain.Group;
import ua.com.foxminded.domain.Lecturer;
import ua.com.foxminded.domain.Lesson;
import ua.com.foxminded.domain.LessonTime;
import ua.com.foxminded.repositories.exceptions.RepositoryException;
import ua.com.foxminded.service.GroupService;
import ua.com.foxminded.service.LecturerService;
import ua.com.foxminded.service.LessonService;
import ua.com.foxminded.service.LessonTimeService;
import ua.com.foxminded.service.exceptions.ServiceException;
import ua.com.foxminded.settings.SpringConfiguration;
import ua.com.foxminded.settings.SpringTestConfiguration;

@ContextConfiguration(classes = { SpringConfiguration.class, SpringTestConfiguration.class})
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
class ScheduleControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ScheduleController scheduleController;

    @Mock
    private LessonService lessonService;

    @Mock
    private LecturerService lecturerService;

    @Mock
    private GroupService groupService;

    @Mock
    private LessonTimeService lessonTimeService;
    
    private Group group;
    private Group anotherGroup;
    private Lecturer lecturer;
    private Lecturer anotherLecturer;
    private LessonTime lessonTime;
    private LessonTime anotherLessonTime;

    private MockMvc mockMvc;

    private RepositoryException repositoryException = new RepositoryException("repository exception",
            new QueryTimeoutException("Exception message"));
    private ServiceException serviceWithRepositoryException = new ServiceException("Service exception", repositoryException);
    private ServiceException serviceWithConstraintViolationException = new ServiceException("Service exception",
            new ConstraintViolationException(null));

    private ServiceException serviceWithIllegalArgumentException = new ServiceException("Service exception",
            new IllegalArgumentException());

    @BeforeEach
    void init() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(scheduleController, "lessonService", lessonService);
        ReflectionTestUtils.setField(scheduleController, "lecturerService", lecturerService);
        ReflectionTestUtils.setField(scheduleController, "groupService", groupService);
        ReflectionTestUtils.setField(scheduleController, "lessonTimeService", lessonTimeService);
        
        group = new Group();
        group.setId(1);
        group.setName("Test group");
        group.setFaculty(new Faculty());
        
        anotherGroup = new Group();
        anotherGroup.setId(2);
        anotherGroup.setName("Second group");
        anotherGroup.setFaculty(new Faculty());
        
        lecturer = new Lecturer();
        lecturer.setId(1);
        lecturer.setFirstName("Nataliia");
        lecturer.setLastName("Khodorkovska");
        lecturer.setGender(Gender.FEMALE);
        lecturer.setPhoneNumber("+380503698541");
        lecturer.setEmail("NKhodorkovska@test.com");
        
        anotherLecturer = new Lecturer();
        anotherLecturer.setId(2);
        anotherLecturer.setFirstName("Vasyl");
        anotherLecturer.setLastName("Didenko");
        anotherLecturer.setGender(Gender.MALE);
        anotherLecturer.setPhoneNumber("+380459874521");
        anotherLecturer.setEmail("VDidenko@test.com");
        
        lessonTime = new LessonTime();
        lessonTime.setId(1);
        lessonTime.setStartTime(LocalTime.of(15, 0));
        lessonTime.setEndTime(LocalTime.of(16, 0));
        
        anotherLessonTime = new LessonTime();
        anotherLessonTime.setId(2);
        anotherLessonTime.setStartTime(LocalTime.of(17, 0));
        anotherLessonTime.setEndTime(LocalTime.of(18, 0));
    }

    @Test
    void shouldAddToModelLecturerAndGroupListsWhenSearchSchedule() throws Exception {
        Lecturer firstLecturer = new Lecturer();
        firstLecturer.setId(1);
        firstLecturer.setFirstName("Taras");
        firstLecturer.setLastName("Tarasov");
        firstLecturer.setGender(Gender.MALE);
        firstLecturer.setEmail("tarastarasov@test.com");
        firstLecturer.setPhoneNumber("+380987654321");

        Lecturer secondLecturer = new Lecturer();
        secondLecturer.setId(2);
        secondLecturer.setFirstName("Serhii");
        secondLecturer.setLastName("Serhiiev");
        secondLecturer.setGender(Gender.MALE);
        secondLecturer.setEmail("serhiievserhii@test.com");
        secondLecturer.setPhoneNumber("+380459876321");

        when(lecturerService.getAll()).thenReturn(Arrays.asList(firstLecturer, secondLecturer));

        Faculty faculty = new Faculty();
        faculty.setId(1);
        faculty.setName("Faculty");

        Group firstGroup = new Group();
        firstGroup.setId(1);
        firstGroup.setName("First group");
        firstGroup.setFaculty(faculty);

        Group secondGroup = new Group();
        secondGroup.setId(2);
        secondGroup.setName("Second group");
        secondGroup.setFaculty(faculty);

        when(groupService.getAll()).thenReturn(Arrays.asList(firstGroup, secondGroup));

        mockMvc.perform(get("/search-schedule")).andExpect(status().isOk())
        .andExpect(view().name("schedule/search-schedule/search-schedule"))
        .andExpect(model().attribute("pageTitle", equalTo("Search schedule")))
        .andExpect(model().attribute("lecturers", hasSize(2)))
        .andExpect(model().attribute("lecturers",
                hasItem(allOf(hasProperty("id", is(1)), hasProperty("firstName", is("Taras")),
                        hasProperty("lastName", is("Tarasov")), hasProperty("gender", equalTo(Gender.MALE)),
                        hasProperty("email", is("tarastarasov@test.com")),
                        hasProperty("phoneNumber", is("+380987654321"))))))
        .andExpect(model().attribute("lecturers",
                hasItem(allOf(hasProperty("id", is(2)), hasProperty("firstName", is("Serhii")),
                        hasProperty("lastName", is("Serhiiev")), hasProperty("gender", equalTo(Gender.MALE)),
                        hasProperty("email", is("serhiievserhii@test.com")),
                        hasProperty("phoneNumber", is("+380459876321"))))))
        .andExpect(model().attribute("groups", hasSize(2)))
        .andExpect(model().attribute("groups",
                hasItem(allOf(hasProperty("id", is(1)), hasProperty("name", is("First group")),
                        hasProperty("faculty", equalTo(faculty))))))
        .andExpect(model().attribute("groups", hasItem(allOf(hasProperty("id", is(2)),
                hasProperty("name", is("Second group")), hasProperty("faculty", equalTo(faculty))))));

        verify(lecturerService).getAll();
        verify(groupService).getAll();
    }

    @Test
    void shouldAddToModelLecturerWeekLessonsWhenResultSchedule() throws Exception {
        int lecturerId = 1;

        Lecturer lecturer = new Lecturer();
        lecturer.setId(lecturerId);
        lecturer.setFirstName("Taras");
        lecturer.setLastName("Tarasov");
        lecturer.setGender(Gender.MALE);
        lecturer.setEmail("tarastarasov@test.com");
        lecturer.setPhoneNumber("+380987654321");

        Faculty faculty = new Faculty();
        faculty.setId(1);
        faculty.setName("Faculty");

        Group firstGroup = new Group();
        firstGroup.setId(1);
        firstGroup.setName("QW-1");
        firstGroup.setFaculty(faculty);

        Group secondGroup = new Group();
        secondGroup.setId(2);
        firstGroup.setName("QW-2");
        firstGroup.setFaculty(faculty);

        LessonTime firstLessonTime = new LessonTime();
        firstLessonTime.setId(1);
        firstLessonTime.setStartTime(LocalTime.of(9, 0));
        firstLessonTime.setEndTime(LocalTime.of(11, 0));

        LessonTime secondLessonTime = new LessonTime();
        secondLessonTime.setId(2);
        secondLessonTime.setStartTime(LocalTime.of(12, 0));
        secondLessonTime.setEndTime(LocalTime.of(14, 0));

        Lesson firstLesson = new Lesson();
        firstLesson.setId(1);
        firstLesson.setName("Lesson-1");
        firstLesson.setAudience("101");
        firstLesson.setLecturer(lecturer);
        firstLesson.setGroup(firstGroup);
        firstLesson.setDay(DayOfWeek.MONDAY);
        firstLesson.setLessonTime(firstLessonTime);

        Lesson secondLesson = new Lesson();
        secondLesson.setId(2);
        secondLesson.setName("Lesson-2");
        secondLesson.setAudience("102");
        secondLesson.setLecturer(lecturer);
        secondLesson.setGroup(secondGroup);
        secondLesson.setDay(DayOfWeek.WEDNESDAY);
        secondLesson.setLessonTime(secondLessonTime);

        when(lecturerService.getById(lecturerId)).thenReturn(lecturer);

        Map<DayOfWeek, List<Lesson>> lecturerWeekLessons = new HashMap<>();
        lecturerWeekLessons.put(firstLesson.getDay(), Arrays.asList(firstLesson));
        lecturerWeekLessons.put(secondLesson.getDay(), Arrays.asList(secondLesson));

        when(lessonService.getLecturerWeekLessons(lecturerId)).thenReturn(lecturerWeekLessons);

        mockMvc.perform(get("/lessons").param("people-role-radio", "lecturer")
                .param("lecturer-value", Integer.toString(lecturerId)).param("period-radio", "week"))
        .andExpect(status().isOk()).andExpect(view().name("schedule/search-schedule/lecturer-week-schedule"))
        .andExpect(model().attribute("pageTitle",
                equalTo("Schedule of a lecturer " + lecturer.getFirstName() + " " + lecturer.getLastName()
                + " for a week")))
        .andExpect(model().attribute("lecturer", equalTo(lecturer)))
        .andExpect(model().attribute("weekLessons", equalTo(lecturerWeekLessons)));

        verify(lecturerService).getById(lecturerId);
        verify(lessonService).getLecturerWeekLessons(lecturerId);
    }

    @Test
    void shouldAddToModelLecturerMonthLessonsWhenResultSchedule() throws Exception {
        int lecturerId = 1;

        Lecturer lecturer = new Lecturer();
        lecturer.setId(lecturerId);
        lecturer.setFirstName("Ivan");
        lecturer.setLastName("Zakharchuk");
        lecturer.setGender(Gender.MALE);
        lecturer.setEmail("ivanzakharchuk@test.com");
        lecturer.setPhoneNumber("+380946985741");

        Faculty faculty = new Faculty();
        faculty.setId(1);
        faculty.setName("Faculty");

        Group firstGroup = new Group();
        firstGroup.setId(1);
        firstGroup.setName("AS-1");
        firstGroup.setFaculty(faculty);

        Group secondGroup = new Group();
        secondGroup.setId(2);
        firstGroup.setName("AS-2");
        firstGroup.setFaculty(faculty);

        LessonTime firstLessonTime = new LessonTime();
        firstLessonTime.setId(1);
        firstLessonTime.setStartTime(LocalTime.of(9, 0));
        firstLessonTime.setEndTime(LocalTime.of(10, 0));

        LessonTime secondLessonTime = new LessonTime();
        secondLessonTime.setId(2);
        secondLessonTime.setStartTime(LocalTime.of(11, 0));
        secondLessonTime.setEndTime(LocalTime.of(12, 0));

        Lesson firstLesson = new Lesson();
        firstLesson.setId(1);
        firstLesson.setName("Lesson-1");
        firstLesson.setAudience("103");
        firstLesson.setLecturer(lecturer);
        firstLesson.setGroup(firstGroup);
        firstLesson.setDay(DayOfWeek.TUESDAY);
        firstLesson.setLessonTime(firstLessonTime);

        Lesson secondLesson = new Lesson();
        secondLesson.setId(2);
        secondLesson.setName("Lesson-2");
        secondLesson.setAudience("107");
        secondLesson.setLecturer(lecturer);
        secondLesson.setGroup(secondGroup);
        secondLesson.setDay(DayOfWeek.FRIDAY);
        secondLesson.setLessonTime(secondLessonTime);

        when(lecturerService.getById(lecturerId)).thenReturn(lecturer);

        Map<LocalDate, List<Lesson>> lecturerMonthLessons = new HashMap<>();
        YearMonth month = YearMonth.of(2021, 3);

        for (int i = 1; i <= 31; i++) {
            if (month.atDay(i).getDayOfWeek().equals(firstLesson.getDay())) {
                lecturerMonthLessons.put(LocalDate.of(month.getYear(), month.getMonthValue(), i),
                        Arrays.asList(firstLesson));
            } else if (month.atDay(i).getDayOfWeek().equals(secondLesson.getDay())) {
                lecturerMonthLessons.put(LocalDate.of(month.getYear(), month.getMonthValue(), i),
                        Arrays.asList(secondLesson));
            }
        }

        when(lessonService.getLecturerMonthLessons(lecturerId, month)).thenReturn(lecturerMonthLessons);

        mockMvc.perform(get("/lessons").param("people-role-radio", "lecturer")
                .param("lecturer-value", Integer.toString(lecturerId)).param("period-radio", "month")
                .param("month-value", "2021-03")).andExpect(status().isOk())
        .andExpect(view().name("schedule/search-schedule/lecturer-month-schedule"))
        .andExpect(model().attribute("pageTitle",
                equalTo("Schedule of a lecturer " + lecturer.getFirstName() + " " + lecturer.getLastName()
                + " for a month")))
        .andExpect(model().attribute("lecturer", equalTo(lecturer)))
        .andExpect(model().attribute("monthLessons", equalTo(lecturerMonthLessons)))
        .andExpect(
                model().attribute("month", equalTo(Month.MARCH.getDisplayName(TextStyle.FULL, Locale.ENGLISH))))
        .andExpect(model().attribute("year", is(2021)));

        verify(lecturerService).getById(lecturerId);
        verify(lessonService).getLecturerMonthLessons(lecturerId, month);
    }

    @Test
    void shouldAddToModelGroupWeekLessonsWhenResultSchedule() throws Exception {
        Faculty faculty = new Faculty();
        faculty.setId(1);
        faculty.setName("Faculty");

        int groupId = 1;

        Group group = new Group();
        group.setId(groupId);
        group.setName("SD-1");
        group.setFaculty(faculty);

        Lecturer firstLecturer = new Lecturer();
        firstLecturer.setId(1);
        firstLecturer.setFirstName("Roman");
        firstLecturer.setLastName("Dudchenko");
        firstLecturer.setGender(Gender.MALE);
        firstLecturer.setEmail("dudchenkoroman@test.com");
        firstLecturer.setPhoneNumber("+3803216549871");

        Lecturer secondLecturer = new Lecturer();
        secondLecturer.setId(2);
        secondLecturer.setFirstName("Nataliia");
        secondLecturer.setLastName("Sirhiyenko");
        secondLecturer.setGender(Gender.FEMALE);
        secondLecturer.setEmail("nataliiasirhiyenko@test.com");
        secondLecturer.setPhoneNumber("+380459865321");

        LessonTime firstLessonTime = new LessonTime();
        firstLessonTime.setId(1);
        firstLessonTime.setStartTime(LocalTime.of(9, 0));
        firstLessonTime.setEndTime(LocalTime.of(11, 0));

        LessonTime secondLessonTime = new LessonTime();
        secondLessonTime.setId(2);
        secondLessonTime.setStartTime(LocalTime.of(12, 0));
        secondLessonTime.setEndTime(LocalTime.of(14, 0));

        Lesson firstLesson = new Lesson();
        firstLesson.setId(1);
        firstLesson.setName("Lesson-1");
        firstLesson.setAudience("101");
        firstLesson.setLecturer(firstLecturer);
        firstLesson.setGroup(group);
        firstLesson.setDay(DayOfWeek.WEDNESDAY);
        firstLesson.setLessonTime(firstLessonTime);

        Lesson secondLesson = new Lesson();
        secondLesson.setId(2);
        secondLesson.setName("Lesson-2");
        secondLesson.setAudience("102");
        secondLesson.setLecturer(secondLecturer);
        secondLesson.setGroup(group);
        secondLesson.setDay(DayOfWeek.THURSDAY);
        secondLesson.setLessonTime(secondLessonTime);

        when(groupService.getById(groupId)).thenReturn(group);

        Map<DayOfWeek, List<Lesson>> groupWeekLessons = new HashMap<>();
        groupWeekLessons.put(firstLesson.getDay(), Arrays.asList(firstLesson));
        groupWeekLessons.put(secondLesson.getDay(), Arrays.asList(secondLesson));

        when(lessonService.getGroupWeekLessons(groupId)).thenReturn(groupWeekLessons);

        mockMvc.perform(get("/lessons").param("people-role-radio", "group")
                .param("group-value", Integer.toString(groupId)).param("period-radio", "week"))
        .andExpect(status().isOk()).andExpect(view().name("schedule/search-schedule/group-week-schedule"))
        .andExpect(model().attribute("pageTitle",
                equalTo("Schedule of a group " + group.getName() + " for a week")))
        .andExpect(model().attribute("group", equalTo(group)))
        .andExpect(model().attribute("weekLessons", equalTo(groupWeekLessons)));

        verify(groupService).getById(groupId);
        verify(lessonService).getGroupWeekLessons(groupId);
    }

    @Test
    void shouldAddToModelGroupMonthLessonsWhenResultSchedule() throws Exception {
        Faculty faculty = new Faculty();
        faculty.setId(1);
        faculty.setName("Faculty");

        int groupId = 1;

        Group group = new Group();
        group.setId(groupId);
        group.setName("QW-1");
        group.setFaculty(faculty);

        Lecturer firstLecturer = new Lecturer();
        firstLecturer.setId(1);
        firstLecturer.setFirstName("Ivan");
        firstLecturer.setLastName("Ivanov");
        firstLecturer.setGender(Gender.MALE);
        firstLecturer.setEmail("ivanovivan@test.com");
        firstLecturer.setPhoneNumber("+380123456789");

        Lecturer secondLecturer = new Lecturer();
        secondLecturer.setId(2);
        secondLecturer.setFirstName("Vasyl");
        secondLecturer.setLastName("Vasyliev");
        secondLecturer.setGender(Gender.MALE);
        secondLecturer.setEmail("vasylievvasyl@test.com");
        secondLecturer.setPhoneNumber("+380987654321");

        LessonTime firstLessonTime = new LessonTime();
        firstLessonTime.setId(1);
        firstLessonTime.setStartTime(LocalTime.of(9, 0));
        firstLessonTime.setEndTime(LocalTime.of(11, 0));

        LessonTime secondLessonTime = new LessonTime();
        secondLessonTime.setId(2);
        secondLessonTime.setStartTime(LocalTime.of(12, 0));
        secondLessonTime.setEndTime(LocalTime.of(14, 0));

        Lesson firstLesson = new Lesson();
        firstLesson.setId(1);
        firstLesson.setName("Lesson-1");
        firstLesson.setAudience("101");
        firstLesson.setLecturer(firstLecturer);
        firstLesson.setGroup(group);
        firstLesson.setDay(DayOfWeek.MONDAY);
        firstLesson.setLessonTime(firstLessonTime);

        Lesson secondLesson = new Lesson();
        secondLesson.setId(2);
        secondLesson.setName("Lesson-2");
        secondLesson.setAudience("102");
        secondLesson.setLecturer(secondLecturer);
        secondLesson.setGroup(group);
        secondLesson.setDay(DayOfWeek.WEDNESDAY);
        secondLesson.setLessonTime(secondLessonTime);

        when(groupService.getById(groupId)).thenReturn(group);

        Map<LocalDate, List<Lesson>> groupMonthLessons = new HashMap<>();
        YearMonth month = YearMonth.of(2021, 2);

        for (int i = 1; i <= 28; i++) {
            if (month.atDay(i).getDayOfWeek().equals(firstLesson.getDay())) {
                groupMonthLessons.put(LocalDate.of(month.getYear(), month.getMonthValue(), i),
                        Arrays.asList(firstLesson));
            } else if (month.atDay(i).getDayOfWeek().equals(secondLesson.getDay())) {
                groupMonthLessons.put(LocalDate.of(month.getYear(), month.getMonthValue(), i),
                        Arrays.asList(secondLesson));
            }
        }

        when(lessonService.getGroupMonthLessons(groupId, month)).thenReturn(groupMonthLessons);

        mockMvc.perform(
                get("/lessons").param("people-role-radio", "group").param("group-value", Integer.toString(groupId))
                .param("period-radio", "month").param("month-value", "2021-02"))
        .andExpect(status().isOk()).andExpect(view().name("schedule/search-schedule/group-month-schedule"))
        .andExpect(model().attribute("pageTitle",
                equalTo("Schedule of a group " + group.getName() + " for a month")))
        .andExpect(model().attribute("group", equalTo(group)))
        .andExpect(model().attribute("monthLessons", equalTo(groupMonthLessons)))
        .andExpect(model().attribute("month",
                equalTo(Month.FEBRUARY.getDisplayName(TextStyle.FULL, Locale.ENGLISH))))
        .andExpect(model().attribute("year", is(2021)));

        verify(groupService).getById(groupId);
        verify(lessonService).getGroupMonthLessons(groupId, month);
    }

    @Test
    void shouldGetLessonTimeParameters() throws Exception {
        when(lessonTimeService.getAll()).thenReturn(Arrays.asList(lessonTime));

        mockMvc.perform(get("/lesson-time-parameters"))
        .andExpect(view().name("schedule/lesson-time-parameters/lesson-time-parameters"))
        .andExpect(model().attribute("pageTitle", "Lesson time parameters"))
        .andExpect(model().attribute("lessonTimes", equalTo(Arrays.asList(lessonTime))));

        verify(lessonTimeService).getAll();
    }

    @Test
    void shouldGenerateRightPageWhenNewLessonTime() throws Exception {
        mockMvc.perform(get("/lesson-time-parameters/new"))
        .andExpect(status().isOk())
        .andExpect(view().name("schedule/lesson-time-parameters/new"))
        .andExpect(model().attribute("pageTitle", "Create new lesson time parameter"));
    }

    @Test
    void shouldCreateLessonTimeParatemer() throws Exception {
        mockMvc.perform(post("/lesson-time-parameters").flashAttr("lessonTime", lessonTime))
        .andExpect(view().name("redirect:/lesson-time-parameters"))
        .andExpect(status().is3xxRedirection());

        verify(lessonTimeService).create(lessonTime);        
    }

    @Test
    void shouldGenerateRightPageWhenEditLessonTime() throws Exception {
        int testId = 3;
        LessonTime testLessonTime = new LessonTime();
        testLessonTime.setId(testId);
        testLessonTime.setStartTime(LocalTime.of(11, 0));
        testLessonTime.setEndTime(LocalTime.of(12, 0));
        
        when(lessonTimeService.getById(testId)).thenReturn(testLessonTime);
        
        mockMvc.perform(get("/lesson-time-parameters/{id}/edit", testId))
        .andExpect(view().name("/schedule/lesson-time-parameters/edit"))
        .andExpect(model().attribute("pageTitle", "Edit lesson time parameter"))
        .andExpect(model().attribute("lessonTime", equalTo(testLessonTime)));
        
        verify(lessonTimeService).getById(testId);

    }
    
    @Test
    void shouldUpdateLessonTime() throws Exception {
        int testId = 13;
        LessonTime testLessonTime = new LessonTime();
        testLessonTime.setId(testId);
        testLessonTime.setStartTime(LocalTime.of(10, 0));
        testLessonTime.setEndTime(LocalTime.of(11, 0));
        
        mockMvc.perform(patch("/lesson-time-parameters/{id}", testId).flashAttr("lessonTime", testLessonTime))
        .andExpect(view().name("redirect:/lesson-time-parameters"))
        .andExpect(status().is3xxRedirection());
        
        verify(lessonTimeService).update(testLessonTime);
    }
    
    @Test
    void shouldDeleteLessonTime() throws Exception {
        int testId = 12;
        
        mockMvc.perform(delete("/lesson-time-parameters/{id}", testId))
        .andExpect(view().name("redirect:/lesson-time-parameters"))
        .andExpect(status().is3xxRedirection());
        
        verify(lessonTimeService).deleteById(testId);
    }
    
    @Test
    void shouldGenerateRightPageWhenNewLesson() throws Exception {
        when(groupService.getAll()).thenReturn(Arrays.asList(group));
        when(lecturerService.getAll()).thenReturn(Arrays.asList(lecturer));
        when(lessonTimeService.getAll()).thenReturn(Arrays.asList(lessonTime));
        
        mockMvc.perform(get("/lessons-new"))
        .andExpect(status().isOk())
        .andExpect(view().name("schedule/lessons/new"))
        .andExpect(model().attribute("pageTitle", "Create a new lesson"))
        .andExpect(model().attribute("groups", equalTo(Arrays.asList(group))))
        .andExpect(model().attribute("lecturers", equalTo(Arrays.asList(lecturer))))
        .andExpect(model().attribute("lessonTimes", equalTo(Arrays.asList(lessonTime))));
        
        verify(groupService).getAll();
        verify(lecturerService).getAll();
        verify(lessonTimeService).getAll();
    }
    
    @Test
    void shouldCreateLesson() throws Exception {
        Lesson testLesson = new Lesson();
        testLesson.setName("Test lesson");
        testLesson.setAudience("101");
        
        Lesson expectedLesson = new Lesson();
        expectedLesson.setName("Test lesson");
        expectedLesson.setAudience("101");
        expectedLesson.setDay(DayOfWeek.WEDNESDAY);
        expectedLesson.setGroup(group);
        expectedLesson.setLecturer(lecturer);
        expectedLesson.setLessonTime(lessonTime);
        
        
        when(lessonTimeService.getById(lessonTime.getId())).thenReturn(lessonTime);
        when(groupService.getById(group.getId())).thenReturn(group);
        when(lecturerService.getById(lecturer.getId())).thenReturn(lecturer);
        
        mockMvc.perform(post("/search-schedule")
                .flashAttr("lesson", testLesson)
                .param("day-value", DayOfWeek.WEDNESDAY.toString())
                .param("lesson-time-value", Integer.toString(lessonTime.getId()))
                .param("group-value", Integer.toString(group.getId()))
                .param("lecturer-value", Integer.toString(lecturer.getId())))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/search-schedule"))
        .andExpect(model().attribute("lesson", equalTo(testLesson)));
        
        verify(lessonTimeService).getById(lessonTime.getId());
        verify(groupService).getById(group.getId());
        verify(lecturerService).getById(lecturer.getId());
        verify(lessonService).create(expectedLesson);

    }
    
    @Test
    void shouldGenerateRightPageWhenEditLesson() throws Exception {
        int testId = 2;
        Lesson lesson = new Lesson();
        lesson.setId(testId);
        lesson.setName("Test lesson");
        lesson.setAudience("103");
        lesson.setDay(DayOfWeek.FRIDAY);
        lesson.setGroup(group);
        lesson.setLecturer(lecturer);
        lesson.setLessonTime(lessonTime);
        
        List<LessonTime> allLessonTimes = new ArrayList<>(Arrays.asList(lessonTime, anotherLessonTime));
        List<LessonTime> expectedLessonTimeList = new ArrayList<>(Arrays.asList(anotherLessonTime));
        
        List<DayOfWeek> allDays = new ArrayList<>(Arrays.asList(DayOfWeek.values()));
        List<DayOfWeek> expectedDayList = new ArrayList<>();
        expectedDayList.addAll(allDays);
        expectedDayList.remove(lesson.getDay());
        
        List<Lecturer> allLecturers = new ArrayList<>(Arrays.asList(lecturer, anotherLecturer));
        List<Lecturer> expectedLecturerList = new ArrayList<>(Arrays.asList(anotherLecturer));
        
        List<Group> allGroups = new ArrayList<>(Arrays.asList(group, anotherGroup));
        List<Group> expectedGroups = new ArrayList<>(Arrays.asList(anotherGroup));
        
        when(lessonService.getById(testId)).thenReturn(lesson);
        when(lessonTimeService.getAll()).thenReturn(allLessonTimes);
        when(lecturerService.getAll()).thenReturn(allLecturers);
        when(groupService.getAll()).thenReturn(allGroups);
        
        mockMvc.perform(get("/lessons/{id}/edit", testId))
        .andExpect(status().isOk())
        .andExpect(view().name("schedule/lessons/edit"))
        .andExpect(model().attribute("pageTitle", "Edit a lesson " + lesson.getName()))
        .andExpect(model().attribute("lesson", equalTo(lesson)))
        .andExpect(model().attribute("days", equalTo(expectedDayList)))
        .andExpect(model().attribute("lessonTimes", equalTo(expectedLessonTimeList)))
        .andExpect(model().attribute("lecturers", equalTo(expectedLecturerList)))
        .andExpect(model().attribute("groups", equalTo(expectedGroups)));
        
        verify(lessonService).getById(testId);
        verify(lessonTimeService).getAll();
        verify(lecturerService).getAll();
        verify(groupService).getAll();
    }
    
    @Test
    void shouldUpdateLesson() throws Exception {
        int testId = 3;
        Lesson lesson = new Lesson();
        lesson.setId(testId);
        lesson.setName("Test lesson");
        lesson.setAudience("101");
        lesson.setDay(DayOfWeek.THURSDAY);
        lesson.setGroup(anotherGroup);
        lesson.setLecturer(anotherLecturer);
        lesson.setLessonTime(anotherLessonTime);
        
        when(lessonTimeService.getById(anotherLessonTime.getId())).thenReturn(anotherLessonTime);
        when(lecturerService.getById(anotherLecturer.getId())).thenReturn(anotherLecturer);
        when(groupService.getById(anotherGroup.getId())).thenReturn(anotherGroup);
        
        
        mockMvc.perform(patch("/lessons/{id}", testId)
                .flashAttr("lesson", lesson)
                .param("day-value", DayOfWeek.THURSDAY.toString())
                .param("lesson-time-value", Integer.toString(anotherLessonTime.getId()))
                .param("lecturer-value", Integer.toString(anotherLecturer.getId()))
                .param("group-value", Integer.toString(anotherGroup.getId())))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/search-schedule"));
        
        verify(lessonTimeService).getById(anotherLessonTime.getId());
        verify(lecturerService).getById(anotherLecturer.getId());
        verify(groupService).getById(anotherGroup.getId());
        verify(lessonService).update(lesson);
    }
    
    @Test
    void shouldDeleteLesson() throws Exception {
        int testId = 23;
        
        mockMvc.perform(delete("/lessons/{id}", testId))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/search-schedule"));
        
        verify(lessonService).deleteById(testId);
    }

    @Test
    void shouldReturnError500WhenRepositoryExceptionWhileSearchSchedule() throws Exception {
        when(groupService.getAll()).thenThrow(serviceWithRepositoryException);

        mockMvc.perform(get("/search-schedule")).andExpect(status().isInternalServerError());
        verify(lecturerService).getAll();
        verify(groupService).getAll();
    }

    @Test
    void shouldReturnError404WhenServiceExceptionWhileSearchSchedule() throws Exception {
        when(lecturerService.getAll()).thenThrow(ServiceException.class);

        mockMvc.perform(get("/search-schedule")).andExpect(status().isNotFound());
        verify(lecturerService).getAll();
    }
    

    @Test
    void shouldReturnError500WhenRepositoryExceptionWhileResultSchedule() throws Exception {
        int lecturerId = 4;

        when(lecturerService.getById(lecturerId)).thenThrow(serviceWithRepositoryException);

        mockMvc.perform(get("/lessons").param("people-role-radio", "lecturer")
                .param("lecturer-value", Integer.toString(lecturerId)).param("period-radio", "week"))
        .andExpect(status().isInternalServerError());
        verify(lecturerService).getById(lecturerId);
    }

    @Test
    void shouldReturnError400WhenIllegalArgumentExceptionWhileResultSchedule() throws Exception {
        int groupId = 3;
        when(groupService.getById(groupId)).thenThrow(serviceWithIllegalArgumentException);

        mockMvc.perform(get("/lessons").param("people-role-radio", "group")
                .param("group-value", Integer.toString(groupId)).param("period-radio", "week"))
        .andExpect(status().isBadRequest());
        verify(groupService).getById(groupId);
    }

    @Test
    void shouldReturnError404WhenEntityIsNotFoundWhileResultSchedule() throws Exception {
        int groupId = 2;
        Group group = new Group();
        group.setId(groupId);
        group.setName("QW-12");
        group.setFaculty(new Faculty());

        String monthValue = "2021-03";
        YearMonth month = YearMonth.of(2021, 3);

        when(groupService.getById(groupId)).thenReturn(group);
        when(lessonService.getGroupMonthLessons(groupId, month)).thenThrow(ServiceException.class);

        mockMvc.perform(
                get("/lessons").param("people-role-radio", "group").param("group-value", Integer.toString(groupId))
                .param("period-radio", "month").param("month-value", monthValue))
        .andExpect(status().isNotFound());
        verify(groupService).getById(groupId);
        verify(lessonService).getGroupMonthLessons(groupId, month);
    }
    
    @Test
    void shouldReturnError500WhenRepositoryExceptionWhileCreateLessonTime() throws Exception {
        LessonTime lessonTime = new LessonTime();
        lessonTime.setStartTime(LocalTime.of(9, 0));
        lessonTime.setEndTime(LocalTime.of(10, 0));
        
        doThrow(serviceWithRepositoryException).when(lessonTimeService).create(lessonTime);
        
        mockMvc.perform(post("/lesson-time-parameters")
                .flashAttr("lessonTime", lessonTime))
        .andExpect(status().isInternalServerError());
        
        verify(lessonTimeService).create(lessonTime);
    }
    
    @Test
    void shouldReturnError400WhenConstraintViolationExceptionWhileCreateLessonTime() throws Exception {
        LessonTime lessonTime = new LessonTime();
        lessonTime.setStartTime(LocalTime.of(10, 0));
        lessonTime.setEndTime(LocalTime.of(11, 0));
        
        doThrow(serviceWithConstraintViolationException).when(lessonTimeService).create(lessonTime);
        
        mockMvc.perform(post("/lesson-time-parameters")
                .flashAttr("lessonTime", lessonTime))
        .andExpect(status().isBadRequest());
        
        verify(lessonTimeService).create(lessonTime);
    }
    
    @Test
    void shouldReturnError400WhenIllegalArgumentExceptionWhileCreateLessonTime() throws Exception {
        LessonTime lessonTime = new LessonTime();
        lessonTime.setStartTime(LocalTime.of(11, 0));
        lessonTime.setEndTime(LocalTime.of(12, 0));
        
        doThrow(serviceWithIllegalArgumentException).when(lessonTimeService).create(lessonTime);
        
        mockMvc.perform(post("/lesson-time-parameters")
                .flashAttr("lessonTime", lessonTime))
        .andExpect(status().isBadRequest());
        
        verify(lessonTimeService).create(lessonTime);
    }
    
    @Test
    void shouldReturnError500WhenServiceExceptionWhileCreateLessonTime() throws Exception {
        LessonTime lessonTime = new LessonTime();
        lessonTime.setStartTime(LocalTime.of(12, 0));
        lessonTime.setEndTime(LocalTime.of(13, 0));
        
        doThrow(ServiceException.class).when(lessonTimeService).create(lessonTime);
        
        mockMvc.perform(post("/lesson-time-parameters")
                .flashAttr("lessonTime", lessonTime))
        .andExpect(status().isInternalServerError());
        
        verify(lessonTimeService).create(lessonTime);
    }
    
    @Test
    void shouldReturnError500WhenRepositoryExceptionWhileEditLessonTime() throws Exception {
        int testId = 12;
        
        doThrow(serviceWithRepositoryException).when(lessonTimeService).getById(testId);
        
        mockMvc.perform(get("/lesson-time-parameters/{id}/edit", testId))
        .andExpect(status().isInternalServerError());
        
        verify(lessonTimeService).getById(testId);
    }
    
    @Test
    void shouldReturnError500WhenServiceExceptionWhileEditLessonTime() throws Exception {
        int testId = 4;
        
        doThrow(ServiceException.class).when(lessonTimeService).getById(testId);
        
        mockMvc.perform(get("/lesson-time-parameters/{id}/edit", testId))
        .andExpect(status().isInternalServerError());
        
        verify(lessonTimeService).getById(testId);
    }
    
    @Test
    void shouldReturnError500WhenRepositoryExceptionWhileUpdateLessonTime() throws Exception {
        int testId = 12;
        LessonTime lessonTime = new LessonTime();
        lessonTime.setId(testId);
        lessonTime.setStartTime(LocalTime.of(14, 0));
        lessonTime.setEndTime(LocalTime.of(15, 0));
        
        doThrow(serviceWithRepositoryException).when(lessonTimeService).update(lessonTime);
        
        mockMvc.perform(patch("/lesson-time-parameters/{id}", testId)
                .flashAttr("lessonTime", lessonTime))
        .andExpect(status().isInternalServerError());
        
        verify(lessonTimeService).update(lessonTime);
    }
    
    @Test
    void shouldReturnError400WhenConstraintViolationExceptionWhileUpdateLessonTime() throws Exception {
        int testId = 10;
        LessonTime lessonTime = new LessonTime();
        lessonTime.setId(testId);
        lessonTime.setStartTime(LocalTime.of(15, 0));
        lessonTime.setEndTime(LocalTime.of(16, 0));
        
        doThrow(serviceWithConstraintViolationException).when(lessonTimeService).update(lessonTime);
        
        mockMvc.perform(patch("/lesson-time-parameters/{id}", testId)
                .flashAttr("lessonTime", lessonTime))
        .andExpect(status().isBadRequest());
        
        verify(lessonTimeService).update(lessonTime);
    }
    
    @Test
    void shouldReturnError400WhenIllegalArgumentExceptionWhileUpdateLessonTime() throws Exception {
        int testId = 5;
        LessonTime lessonTime = new LessonTime();
        lessonTime.setId(testId);
        lessonTime.setStartTime(LocalTime.of(16, 0));
        lessonTime.setEndTime(LocalTime.of(17, 0));
        
        doThrow(serviceWithIllegalArgumentException).when(lessonTimeService).update(lessonTime);
        
        mockMvc.perform(patch("/lesson-time-parameters/{id}", testId)
                .flashAttr("lessonTime", lessonTime))
        .andExpect(status().isBadRequest());
        
        verify(lessonTimeService).update(lessonTime);
    }
    
    @Test
    void shouldReturnError500WhenServiceExceptionWhileUpdateLessonTime() throws Exception {
        int testId = 1;
        LessonTime lessonTime = new LessonTime();
        lessonTime.setId(testId);
        lessonTime.setStartTime(LocalTime.of(16, 0));
        lessonTime.setEndTime(LocalTime.of(17, 0));
        
        doThrow(ServiceException.class).when(lessonTimeService).update(lessonTime);
        
        mockMvc.perform(patch("/lesson-time-parameters/{id}", testId)
                .flashAttr("lessonTime", lessonTime))
        .andExpect(status().isInternalServerError());
        
        verify(lessonTimeService).update(lessonTime);
    }
    
    @Test
    void shouldReturn500WhenRepositoryExceptionWhileDeleteLessonTime() throws Exception {
        int testId = 741;
        
        doThrow(serviceWithRepositoryException).when(lessonTimeService).deleteById(testId);
        
        mockMvc.perform(delete("/lesson-time-parameters/{id}", testId))
        .andExpect(status().isInternalServerError());
        
        verify(lessonTimeService).deleteById(testId);
    }
    
    @Test
    void shouldReturn400WhenIllegalArgumentExceptionWhileDeleteLessonTime() throws Exception {
        int testId = 123;
        
        doThrow(serviceWithIllegalArgumentException).when(lessonTimeService).deleteById(testId);
        
        mockMvc.perform(delete("/lesson-time-parameters/{id}", testId))
        .andExpect(status().isBadRequest());
        
        verify(lessonTimeService).deleteById(testId);
    }
    
    @Test
    void shouldReturn500WhenServiceExceptionWhileDeleteLessonTime() throws Exception {
        int testId = 854;
        
        doThrow(ServiceException.class).when(lessonTimeService).deleteById(testId);
        
        mockMvc.perform(delete("/lesson-time-parameters/{id}", testId))
        .andExpect(status().isInternalServerError());
        
        verify(lessonTimeService).deleteById(testId);
    }
    
    @Test
    void shouldReturn500WhenRepositoryExceptionWhileNewLesson() throws Exception {
        doThrow(serviceWithRepositoryException).when(groupService).getAll();
        
        mockMvc.perform(get("/lessons-new"))
        .andExpect(status().isInternalServerError());
        
        verify(groupService).getAll();
    }
    
    @Test
    void shouldReturn500WhenServiceExceptionWhileNewLesson() throws Exception {
        doThrow(ServiceException.class).when(lecturerService).getAll();
        
        mockMvc.perform(get("/lessons-new"))
        .andExpect(status().isInternalServerError());
        
        verify(groupService).getAll();
        verify(lecturerService).getAll();
    }
    
    @Test
    void shouldReturnError500WhenRepositoryExceptionWhileCreateLesson() throws Exception {
        Lesson lesson = new Lesson();
        lesson.setName("Test lesson");
        lesson.setAudience("101");
        lesson.setDay(DayOfWeek.MONDAY);
        lesson.setGroup(group);
        lesson.setLecturer(lecturer);
        lesson.setLessonTime(lessonTime);
        
        doThrow(serviceWithRepositoryException).when(lessonTimeService).getById(lessonTime.getId());
        
        mockMvc.perform(post("/search-schedule")
                .flashAttr("lesson", lesson)
                .param("day-value", DayOfWeek.MONDAY.toString())
                .param("lesson-time-value", Integer.toString(lessonTime.getId()))
                .param("group-value", Integer.toString(group.getId()))
                .param("lecturer-value", Integer.toString(lecturer.getId())))
        .andExpect(status().isInternalServerError());
        
        verify(lessonTimeService).getById(lessonTime.getId());
    }
    
    @Test
    void shouldReturnError400WhenConstraintViolationExceptionWhileCreateLesson() throws Exception {
        Lesson lesson = new Lesson();
        lesson.setName(" Test lesson");
        lesson.setAudience("102");
        lesson.setDay(DayOfWeek.TUESDAY);
        lesson.setGroup(group);
        lesson.setLecturer(lecturer);
        lesson.setLessonTime(lessonTime);
        
        doThrow(serviceWithConstraintViolationException).when(lessonService).create(lesson);
        
        mockMvc.perform(post("/search-schedule")
                .flashAttr("lesson", lesson)
                .param("day-value", DayOfWeek.TUESDAY.toString())
                .param("lesson-time-value", Integer.toString(lessonTime.getId()))
                .param("group-value", Integer.toString(group.getId()))
                .param("lecturer-value", Integer.toString(lecturer.getId())))
        .andExpect(status().isBadRequest());
        
        verify(lessonTimeService).getById(lessonTime.getId());
        verify(lecturerService).getById(lecturer.getId());
        verify(groupService).getById(group.getId());
        verify(lessonService).create(lesson);
    }
    
    @Test
    void shouldReturnError400WhenIllegalArgumentExceptionWhileCreateLesson() throws Exception {
        Lesson lesson = new Lesson();
        lesson.setId(3);
        lesson.setName("Test lesson");
        lesson.setAudience("100");
        lesson.setDay(DayOfWeek.WEDNESDAY);
        lesson.setGroup(group);
        lesson.setLecturer(lecturer);
        lesson.setLessonTime(lessonTime);
        
        doThrow(serviceWithIllegalArgumentException).when(lessonService).create(lesson);
        
        mockMvc.perform(post("/search-schedule")
                .flashAttr("lesson", lesson)
                .param("day-value", DayOfWeek.TUESDAY.toString())
                .param("lesson-time-value", Integer.toString(lessonTime.getId()))
                .param("group-value", Integer.toString(group.getId()))
                .param("lecturer-value", Integer.toString(lecturer.getId())))
        .andExpect(status().isBadRequest());
        
        verify(lessonTimeService).getById(lessonTime.getId());
        verify(lecturerService).getById(lecturer.getId());
        verify(groupService).getById(group.getId());
        verify(lessonService).create(lesson);
    }
    
    @Test
    void shouldReturnError500WhenServiceExceptionWhileCreateLesson() throws Exception {
        Lesson lesson = new Lesson();
        lesson.setName("Test lesson");
        lesson.setAudience("104");
        lesson.setDay(DayOfWeek.THURSDAY);
        lesson.setGroup(group);
        lesson.setLecturer(lecturer);
        lesson.setLessonTime(lessonTime);
        
        doThrow(ServiceException.class).when(lessonTimeService).getById(lessonTime.getId());
        
        mockMvc.perform(post("/search-schedule")
                .flashAttr("lesson", lesson)
                .param("day-value", DayOfWeek.MONDAY.toString())
                .param("lesson-time-value", Integer.toString(lessonTime.getId()))
                .param("group-value", Integer.toString(group.getId()))
                .param("lecturer-value", Integer.toString(lecturer.getId())))
        .andExpect(status().isInternalServerError());
        
        verify(lessonTimeService).getById(lessonTime.getId());
    }
    
    @Test
    void shouldReturnError500WhenRepositoryExceptionWhileEditLesson() throws Exception {
        int lessonId = 12;
        Lesson lesson = new Lesson();
        lesson.setId(lessonId);
        lesson.setName("Test lesson");
        lesson.setAudience("4");
        lesson.setDay(DayOfWeek.THURSDAY);
        lesson.setGroup(group);
        lesson.setLecturer(lecturer);
        lesson.setLessonTime(lessonTime);
        
        doThrow(serviceWithRepositoryException).when(lessonService).getById(lessonId);
        
        mockMvc.perform(get("/lessons/{id}/edit", lessonId)
                .flashAttr("lesson", lesson)
                .param("day-value", DayOfWeek.THURSDAY.toString())
                .param("lesson-time-value", Integer.toString(lessonTime.getId()))
                .param("group-value", Integer.toString(group.getId()))
                .param("lecturer-value", Integer.toString(lecturer.getId())))
        .andExpect(status().isInternalServerError());
        
        verify(lessonService).getById(lessonId);
    }
    
    @Test
    void shouldReturnError500WhenServiceExceptionWhileEditLesson() throws Exception {
        int lessonId = 2;
        Lesson lesson = new Lesson();
        lesson.setId(lessonId);
        lesson.setName("Test lesson");
        lesson.setAudience("32");
        lesson.setDay(DayOfWeek.FRIDAY);
        lesson.setGroup(group);
        lesson.setLecturer(lecturer);
        lesson.setLessonTime(lessonTime);
        
        doThrow(ServiceException.class).when(lessonService).getById(lessonId);
        
        mockMvc.perform(get("/lessons/{id}/edit", lessonId)
                .flashAttr("lesson", lesson)
                .param("day-value", DayOfWeek.FRIDAY.toString())
                .param("lesson-time-value", Integer.toString(lessonTime.getId()))
                .param("group-value", Integer.toString(group.getId()))
                .param("lecturer-value", Integer.toString(lecturer.getId())))
        .andExpect(status().isInternalServerError());
        
        verify(lessonService).getById(lessonId);
    }
    
    @Test
    void shouldReturnError500WhenRepositoryExceptionWhileUpdateLesson() throws Exception {
        int lessonId = 3;
        Lesson lesson = new Lesson();
        lesson.setId(lessonId);
        lesson.setName("Test lesson");
        lesson.setAudience("10");
        lesson.setDay(DayOfWeek.MONDAY);
        lesson.setGroup(group);
        lesson.setLecturer(lecturer);
        lesson.setLessonTime(lessonTime);
        
        doThrow(serviceWithRepositoryException).when(lessonTimeService).getById(lessonTime.getId());
        
        mockMvc.perform(patch("/lessons/{id}", lessonId)
                .flashAttr("lesson", lesson)
                .param("day-value", DayOfWeek.MONDAY.toString())
                .param("lesson-time-value", Integer.toString(lessonTime.getId()))
                .param("group-value", Integer.toString(group.getId()))
                .param("lecturer-value", Integer.toString(lecturer.getId())))
        .andExpect(status().isInternalServerError());
        
        verify(lessonTimeService).getById(lessonTime.getId());
    }
    
    @Test
    void shouldReturnError400WhenConstraintViolationExceptionWhileUpdateLesson() throws Exception {
        int lessonId = 2;
        Lesson lesson = new Lesson();
        lesson.setId(lessonId);
        lesson.setName(" Test lesson");
        lesson.setAudience("110");
        lesson.setDay(DayOfWeek.TUESDAY);
        lesson.setGroup(group);
        lesson.setLecturer(lecturer);
        lesson.setLessonTime(lessonTime);
        
        doThrow(serviceWithConstraintViolationException).when(lessonService).update(lesson);
        
        mockMvc.perform(patch("/lessons/{id}", lessonId)
                .flashAttr("lesson", lesson)
                .param("day-value", DayOfWeek.TUESDAY.toString())
                .param("lesson-time-value", Integer.toString(lessonTime.getId()))
                .param("group-value", Integer.toString(group.getId()))
                .param("lecturer-value", Integer.toString(lecturer.getId())))
        .andExpect(status().isBadRequest());
        
        verify(lessonTimeService).getById(lessonTime.getId());
        verify(lecturerService).getById(lecturer.getId());
        verify(groupService).getById(group.getId());
        verify(lessonService).update(lesson);
    }
    
    @Test
    void shouldReturnError400WhenIllegalArgumentExceptionWhileUpdateLesson() throws Exception {
        int lessonId = 14;
        Lesson lesson = new Lesson();
        lesson.setId(lessonId);
        lesson.setName("Test lesson");
        lesson.setAudience("110");
        lesson.setDay(DayOfWeek.TUESDAY);
        lesson.setGroup(group);
        lesson.setLecturer(lecturer);
        lesson.setLessonTime(lessonTime);
        
        doThrow(serviceWithIllegalArgumentException).when(lessonService).update(lesson);
        
        mockMvc.perform(patch("/lessons/{id}", lessonId)
                .flashAttr("lesson", lesson)
                .param("day-value", DayOfWeek.TUESDAY.toString())
                .param("lesson-time-value", Integer.toString(lessonTime.getId()))
                .param("group-value", Integer.toString(group.getId()))
                .param("lecturer-value", Integer.toString(lecturer.getId())))
        .andExpect(status().isBadRequest());
        
        verify(lessonTimeService).getById(lessonTime.getId());
        verify(lecturerService).getById(lecturer.getId());
        verify(groupService).getById(group.getId());
        verify(lessonService).update(lesson);
    }
    
    @Test
    void shouldReturnError500WhenServiceExceptionWhileUpdateLesson() throws Exception {
        int lessonId = 21;
        Lesson lesson = new Lesson();
        lesson.setId(lessonId);
        lesson.setName("Test lesson");
        lesson.setAudience("20");
        lesson.setDay(DayOfWeek.MONDAY);
        lesson.setGroup(group);
        lesson.setLecturer(lecturer);
        lesson.setLessonTime(lessonTime);
        
        doThrow(ServiceException.class).when(lessonTimeService).getById(lessonTime.getId());
        
        mockMvc.perform(patch("/lessons/{id}", lessonId)
                .flashAttr("lesson", lesson)
                .param("day-value", DayOfWeek.MONDAY.toString())
                .param("lesson-time-value", Integer.toString(lessonTime.getId()))
                .param("group-value", Integer.toString(group.getId()))
                .param("lecturer-value", Integer.toString(lecturer.getId())))
        .andExpect(status().isInternalServerError());
        
        verify(lessonTimeService).getById(lessonTime.getId());
    }
    
    @Test
    void shouldReturnError500WhenRepositoryExceptionWhileDeleteLesson() throws Exception {
        int testId = 2;
        
        doThrow(serviceWithRepositoryException).when(lessonService).deleteById(testId);
        
        mockMvc.perform(delete("/lessons/{id}", testId))
        .andExpect(status().isInternalServerError());
        
        verify(lessonService).deleteById(testId);
    }
    
    @Test
    void shouldReturnError400WhenIllegalExceptionWhileDeleteLesson() throws Exception {
        int testId = 41;
        
        doThrow(serviceWithIllegalArgumentException).when(lessonService).deleteById(testId);
        
        mockMvc.perform(delete("/lessons/{id}", testId))
        .andExpect(status().isBadRequest());
        
        verify(lessonService).deleteById(testId);
    }
    
    @Test
    void shouldReturnError500WhenServiceExceptionWhileDeleteLesson() throws Exception {
        int testId = 63;
        
        doThrow(ServiceException.class).when(lessonService).deleteById(testId);
        
        mockMvc.perform(delete("/lessons/{id}", testId))
        .andExpect(status().isInternalServerError());
        
        verify(lessonService).deleteById(testId);
    }
}
