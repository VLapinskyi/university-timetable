package ua.com.foxminded.controllers;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import ua.com.foxminded.dao.exceptions.DAOException;
import ua.com.foxminded.domain.Faculty;
import ua.com.foxminded.domain.Gender;
import ua.com.foxminded.domain.Group;
import ua.com.foxminded.domain.Lecturer;
import ua.com.foxminded.domain.Lesson;
import ua.com.foxminded.domain.LessonTime;
import ua.com.foxminded.service.GroupService;
import ua.com.foxminded.service.LecturerService;
import ua.com.foxminded.service.LessonService;
import ua.com.foxminded.service.exceptions.ServiceException;
import ua.com.foxminded.settings.SpringConfiguration;


@ContextConfiguration(classes = {SpringConfiguration.class})
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
    
    private MockMvc mockMvc;
    
    private DAOException daoException = new DAOException("DAO exception", new QueryTimeoutException("Exception message"));
    private ServiceException serviceWithDAOException = new ServiceException("Service exception", daoException);
    
    private ServiceException serviceWithIllegalArgumentException = new ServiceException("Service exception", new IllegalArgumentException());
    
    
    @BeforeEach
    void init() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(scheduleController, "lessonService", lessonService);
        ReflectionTestUtils.setField(scheduleController, "lecturerService", lecturerService);
        ReflectionTestUtils.setField(scheduleController, "groupService", groupService);
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
        
        mockMvc.perform(get("/search-schedule"))
        .andExpect(status().isOk())
        .andExpect(view().name("schedule/search-schedule/search-schedule"))
        .andExpect(model().attribute("pageTitle", equalTo("Search schedule")))
        .andExpect(model().attribute("lecturers", hasSize(2)))
        .andExpect(model().attribute("lecturers", hasItem(
                allOf(
                        hasProperty("id", is(1)),
                        hasProperty("firstName", is("Taras")),
                        hasProperty("lastName", is("Tarasov")),
                        hasProperty("gender", equalTo(Gender.MALE)),
                        hasProperty("email", is("tarastarasov@test.com")),
                        hasProperty("phoneNumber", is("+380987654321"))
                ))))
        .andExpect(model().attribute("lecturers", hasItem(
                allOf(
                        hasProperty("id", is(2)),
                        hasProperty("firstName", is("Serhii")),
                        hasProperty("lastName", is("Serhiiev")),
                        hasProperty("gender", equalTo(Gender.MALE)),
                        hasProperty("email", is("serhiievserhii@test.com")),
                        hasProperty("phoneNumber", is("+380459876321"))
                        ))))
        .andExpect(model().attribute("groups", hasSize(2)))
        .andExpect(model().attribute("groups", hasItem(
                allOf(
                        hasProperty("id", is(1)),
                        hasProperty("name", is("First group")),
                        hasProperty("faculty", equalTo(faculty))
                ))))
        .andExpect(model().attribute("groups", hasItem(
                allOf(
                        hasProperty("id", is(2)),
                        hasProperty("name", is("Second group")),
                        hasProperty("faculty", equalTo(faculty))
                        ))));
        
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
        
        mockMvc.perform(get("/lessons")
                .param("people-role-radio", "lecturer")
                .param("lecturer-value", Integer.toString(lecturerId))
                .param("period-radio", "week"))
        .andExpect(status().isOk())
        .andExpect(view().name("schedule/search-schedule/lecturer-week-schedule"))
        .andExpect(model().attribute("pageTitle", equalTo("Schedule of a lecturer " + lecturer.getFirstName() + " " + lecturer.getLastName()
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
        
        mockMvc.perform(get("/lessons")
                .param("people-role-radio", "lecturer")
                .param("lecturer-value", Integer.toString(lecturerId))
                .param("period-radio", "month")
                .param("month-value", "2021-03"))
        .andExpect(status().isOk())
        .andExpect(view().name("schedule/search-schedule/lecturer-month-schedule"))
        .andExpect(model().attribute("pageTitle", equalTo("Schedule of a lecturer " + lecturer.getFirstName() + " " + lecturer.getLastName()
                + " for a month")))  
        .andExpect(model().attribute("lecturer", equalTo(lecturer)))
        .andExpect(model().attribute("monthLessons", equalTo(lecturerMonthLessons)))
        .andExpect(model().attribute("month", equalTo(Month.MARCH.getDisplayName(TextStyle.FULL, Locale.ENGLISH))))
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
        
        mockMvc.perform(get("/lessons")
                .param("people-role-radio", "group")
                .param("group-value", Integer.toString(groupId))
                .param("period-radio", "week"))
        .andExpect(status().isOk())
        .andExpect(view().name("schedule/search-schedule/group-week-schedule"))
        .andExpect(model().attribute("pageTitle", equalTo("Schedule of a group " + group.getName() 
                + " for a week")))  
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
        
        mockMvc.perform(get("/lessons")
                .param("people-role-radio", "group")
                .param("group-value", Integer.toString(groupId))
                .param("period-radio", "month")
                .param("month-value", "2021-02"))
        .andExpect(status().isOk())
        .andExpect(view().name("schedule/search-schedule/group-month-schedule"))
        .andExpect(model().attribute("pageTitle", equalTo("Schedule of a group " + group.getName() 
                + " for a month")))  
        .andExpect(model().attribute("group", equalTo(group)))
        .andExpect(model().attribute("monthLessons", equalTo(groupMonthLessons)))
        .andExpect(model().attribute("month", equalTo(Month.FEBRUARY.getDisplayName(TextStyle.FULL, Locale.ENGLISH))))
        .andExpect(model().attribute("year", is(2021)));
        
        verify(groupService).getById(groupId);
        verify(lessonService).getGroupMonthLessons(groupId, month);
    }
    
    @Test
    void shouldReturnError500WhenDAOExceptionWhileSearchSchedule() throws Exception {
        when(groupService.getAll()).thenThrow(serviceWithDAOException);
        
        mockMvc.perform(get("/search-schedule"))
            .andExpect(status().isInternalServerError());
        verify(lecturerService).getAll();
        verify(groupService).getAll();
    }
    
    @Test
    void shouldReturnError404WhenServiceExceptionWhileSearchSchedule() throws Exception {
        when(lecturerService.getAll()).thenThrow(ServiceException.class);
        
        mockMvc.perform(get("/search-schedule"))
            .andExpect(status().isNotFound());
        verify(lecturerService).getAll();
    }
    
    @Test
    void shouldReturnError500WhenDAOExceptionWhileResultSchedule() throws Exception {
        int lecturerId = 4;
        
        when(lecturerService.getById(lecturerId)).thenThrow(serviceWithDAOException);
        
        mockMvc.perform(get("/lessons")
                .param("people-role-radio", "lecturer")
                .param("lecturer-value", Integer.toString(lecturerId))
                .param("period-radio", "week"))
            .andExpect(status().isInternalServerError());
        verify(lecturerService).getById(lecturerId);
    }
    
    @Test
    void shouldReturnError400WhenIllegalArgumentExceptionWhileResultSchedule() throws Exception {
        int groupId = 3;
        when(groupService.getById(groupId)).thenThrow(serviceWithIllegalArgumentException);
        
        mockMvc.perform(get("/lessons")
                .param("people-role-radio", "group")
                .param("group-value", Integer.toString(groupId))
                .param("period-radio", "week"))
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
        
        mockMvc.perform(get("/lessons")
                .param("people-role-radio", "group")
                .param("group-value", Integer.toString(groupId))
                .param("period-radio", "month")
                .param("month-value", monthValue))
            .andExpect(status().isNotFound());
        verify(groupService).getById(groupId);
        verify(lessonService).getGroupMonthLessons(groupId, month);
    }
}
