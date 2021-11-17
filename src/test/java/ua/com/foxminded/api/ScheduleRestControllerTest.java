package ua.com.foxminded.api;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.QueryTimeoutException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.validation.ConstraintViolationException;

import ua.com.foxminded.api.aspects.GeneralRestControllerAspect;
import ua.com.foxminded.api.aspects.ScheduleRestControllerAspect;
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

@WebMvcTest(ScheduleRestController.class)
@Import({AopAutoConfiguration.class, ScheduleRestControllerAspect.class, GeneralRestControllerAspect.class})
class ScheduleRestControllerTest {

    @Autowired
    private ScheduleRestController scheduleRestController;

    @MockBean
    private LessonService lessonService;

    @MockBean
    private LecturerService lecturerService;

    @MockBean
    private GroupService groupService;

    @MockBean
    private LessonTimeService lessonTimeService;
    
    private Group group;
    private Group anotherGroup;
    private Lecturer lecturer;
    private Lecturer anotherLecturer;
    private LessonTime lessonTime;
    private LessonTime anotherLessonTime;

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;

    private RepositoryException repositoryException = new RepositoryException("repository exception",
            new QueryTimeoutException("Exception message"));
    private ServiceException serviceWithRepositoryException = new ServiceException("Service exception", repositoryException);
    private ServiceException serviceWithConstraintViolationException = new ServiceException("Service exception",
            new ConstraintViolationException(null));

    private ServiceException serviceWithIllegalArgumentException = new ServiceException("Service exception",
            new IllegalArgumentException());

    @BeforeEach
    void init() throws Exception {
        ReflectionTestUtils.setField(scheduleRestController, "lessonService", lessonService);
        ReflectionTestUtils.setField(scheduleRestController, "lecturerService", lecturerService);
        ReflectionTestUtils.setField(scheduleRestController, "groupService", groupService);
        ReflectionTestUtils.setField(scheduleRestController, "lessonTimeService", lessonTimeService);
        
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
    void shouldgetLecturerWeekLessons() throws Exception {
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

        Map<DayOfWeek, List<Lesson>> lecturerWeekLessons = new HashMap<>();
        lecturerWeekLessons.put(firstLesson.getDay(), Arrays.asList(firstLesson));
        lecturerWeekLessons.put(secondLesson.getDay(), Arrays.asList(secondLesson));
        
        String expectedResult = objectMapper.writeValueAsString(lecturerWeekLessons);

        when(lessonService.getLecturerWeekLessons(lecturerId)).thenReturn(lecturerWeekLessons);

        mockMvc.perform(get("/lessons")
                .param("lecturer-id", Integer.toString(lecturerId))
                .param("period", "week")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedResult));

        verify(lessonService).getLecturerWeekLessons(lecturerId);
    }

    @Test
    void shouldGetLecturerMonthLessons() throws Exception {
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
        
        String expectedResult = objectMapper.writeValueAsString(lecturerMonthLessons);

        when(lessonService.getLecturerMonthLessons(lecturerId, month)).thenReturn(lecturerMonthLessons);

        mockMvc.perform(get("/lessons")
                .param("lecturer-id", Integer.toString(lecturerId))
                .param("month-value", month.toString())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedResult));

        verify(lessonService).getLecturerMonthLessons(lecturerId, month);
    }

    @Test
    void shouldGetGroupWeekLessons() throws Exception {
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

        Map<DayOfWeek, List<Lesson>> groupWeekLessons = new HashMap<>();
        groupWeekLessons.put(firstLesson.getDay(), Arrays.asList(firstLesson));
        groupWeekLessons.put(secondLesson.getDay(), Arrays.asList(secondLesson));

        when(lessonService.getGroupWeekLessons(groupId)).thenReturn(groupWeekLessons);
        
        String expectedResult = objectMapper.writeValueAsString(groupWeekLessons);

        mockMvc.perform(get("/lessons")
                .param("group-id", Integer.toString(groupId))
                .param("period", "week")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedResult));
        
        verify(lessonService).getGroupWeekLessons(groupId);
    }

    @Test
    void shouldGetGroupMonthLessons() throws Exception {
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
        
        String expectedResult = objectMapper.writeValueAsString(groupMonthLessons);

        when(lessonService.getGroupMonthLessons(groupId, month)).thenReturn(groupMonthLessons);

        mockMvc.perform(get("/lessons")
                .param("group-id", Integer.toString(groupId))
                .param("month-value", month.toString())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedResult));
        
        verify(lessonService).getGroupMonthLessons(groupId, month);
    }

    @Test
    void shouldGetLessonTimeParameters() throws Exception {
        List<LessonTime> lessonTimes = new ArrayList<>(Arrays.asList(lessonTime));
        when(lessonTimeService.getAll()).thenReturn(lessonTimes);

        String expectedResult = objectMapper.writeValueAsString(lessonTimes);
        
        mockMvc.perform(get("/lesson-time-parameters")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedResult));
            
        verify(lessonTimeService).getAll();
    }

    @Test
    void shouldCreateLessonTimeParatemer() throws Exception {
        String testJson = objectMapper.writeValueAsString(lessonTime);
        
        mockMvc.perform(post("/lesson-time-parameters")
                .content(testJson)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(lessonTimeService).create(lessonTime);        
    }

    @Test
    void shouldUpdateLessonTime() throws Exception {
        int testId = 13;
        LessonTime testLessonTime = new LessonTime();
        testLessonTime.setId(testId);
        testLessonTime.setStartTime(LocalTime.of(10, 0));
        testLessonTime.setEndTime(LocalTime.of(11, 0));
        
        String testJson = objectMapper.writeValueAsString(testLessonTime);
        
        mockMvc.perform(patch("/lesson-time-parameters/{id}", testId)
                .content(testJson).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(testJson))
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
        
        verify(lessonTimeService).update(testLessonTime);
    }
    
    @Test
    void shouldDeleteLessonTime() throws Exception {
        int testId = 12;
        
        String expectedResult = "LessonTime with id: " + testId + " was deleted.";
        
        mockMvc.perform(delete("/lesson-time-parameters/{id}", testId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().string(expectedResult))
            .andExpect(status().isOk());
        
        verify(lessonTimeService).deleteById(testId);
    }
    
    @Test
    void shouldCreateLesson() throws Exception {
        Lesson testLesson = new Lesson();
        testLesson.setName("Test lesson");
        testLesson.setAudience("101");
        testLesson.setDay(DayOfWeek.WEDNESDAY);
        
        Lesson expectedLesson = new Lesson();
        expectedLesson.setName("Test lesson");
        expectedLesson.setAudience("101");
        expectedLesson.setDay(DayOfWeek.WEDNESDAY);
        expectedLesson.setGroup(group);
        expectedLesson.setLecturer(lecturer);
        expectedLesson.setLessonTime(lessonTime);
        
        String testJson = objectMapper.writeValueAsString(testLesson);
        String expectedJson = objectMapper.writeValueAsString(expectedLesson);
        
        when(lessonTimeService.getById(lessonTime.getId())).thenReturn(lessonTime);
        when(groupService.getById(group.getId())).thenReturn(group);
        when(lecturerService.getById(lecturer.getId())).thenReturn(lecturer);
        
        mockMvc.perform(post("/lessons")
                .content(testJson)
                .param("lesson-time-id", Integer.toString(lessonTime.getId()))
                .param("group-id", Integer.toString(group.getId()))
                .param("lecturer-id", Integer.toString(lecturer.getId()))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedJson));
                
        
        verify(lessonTimeService).getById(lessonTime.getId());
        verify(groupService).getById(group.getId());
        verify(lecturerService).getById(lecturer.getId());
        verify(lessonService).create(expectedLesson);

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
        
        String testJson = objectMapper.writeValueAsString(lesson);
        
        mockMvc.perform(patch("/lessons/{id}", testId)
                .content(testJson)
                .param("lesson-time-id",Integer.toString(anotherLessonTime.getId()))
                .param("lecturer-id", Integer.toString(anotherLecturer.getId()))
                .param("group-id", Integer.toString(anotherGroup.getId()))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(testJson))
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
                
        verify(lessonTimeService).getById(anotherLessonTime.getId());
        verify(lecturerService).getById(anotherLecturer.getId());
        verify(groupService).getById(anotherGroup.getId());
        verify(lessonService).update(lesson);
    }
    
    @Test
    void shouldDeleteLesson() throws Exception {
        int testId = 23;
        
        String expectedResult = "Lesson with id: " + testId + " was deleted.";
        
        mockMvc.perform(delete("/lessons/{id}", testId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(expectedResult));
        
        verify(lessonService).deleteById(testId);
    }

    @Test
    void shouldReturnError500WhenRepositoryExceptionWhileGetLecturerWeekLessons() throws Exception {
        int lecturerId = 4;

        when(lessonService.getLecturerWeekLessons(lecturerId)).thenThrow(serviceWithRepositoryException);

        mockMvc.perform(get("/lessons")
                .param("lecturer-id", Integer.toString(lecturerId))
                .param("period", "week")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError());        
                
        verify(lessonService).getLecturerWeekLessons(lecturerId);
    }

    @Test
    void shouldReturnError400WhenIllegalArgumentExceptionWhileGetGroupWeekLessons() throws Exception {
        int groupId = 3;
        when(lessonService.getGroupWeekLessons(groupId)).thenThrow(serviceWithIllegalArgumentException);

        mockMvc.perform(get("/lessons")
                .param("group-id", Integer.toString(groupId))
                .param("period", "week")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
        
        verify(lessonService).getGroupWeekLessons(groupId);
    }

    @Test
    void shouldReturnError404WhenEntityIsNotFoundWhileGetGroupMonthLessons() throws Exception {
        int groupId = 2;
        Group group = new Group();
        group.setId(groupId);
        group.setName("QW-12");
        group.setFaculty(new Faculty());

        String monthValue = "2021-03";
        YearMonth month = YearMonth.of(2021, 3);

        when(lessonService.getGroupMonthLessons(groupId, month)).thenThrow(ServiceException.class);

        mockMvc.perform(get("/lessons")
                .param("group-id", Integer.toString(groupId))
                .param("month-value", monthValue)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
        
        verify(lessonService).getGroupMonthLessons(groupId, month);
    }
    
    @Test
    void shouldReturnError500WhenRepositoryExceptionWhileCreateLessonTime() throws Exception {
        LessonTime lessonTime = new LessonTime();
        lessonTime.setStartTime(LocalTime.of(9, 0));
        lessonTime.setEndTime(LocalTime.of(10, 0));
        
        doThrow(serviceWithRepositoryException).when(lessonTimeService).create(lessonTime);
        
        String testJson = objectMapper.writeValueAsString(lessonTime);
        
        mockMvc.perform(post("/lesson-time-parameters")
                .content(testJson)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError());
        
        verify(lessonTimeService).create(lessonTime);
    }
    
    @Test
    void shouldReturnError400WhenConstraintViolationExceptionWhileCreateLessonTime() throws Exception {
        LessonTime lessonTime = new LessonTime();
        lessonTime.setStartTime(LocalTime.of(10, 0));
        lessonTime.setEndTime(LocalTime.of(11, 0));
        
        doThrow(serviceWithConstraintViolationException).when(lessonTimeService).create(lessonTime);
        
        String testJson = objectMapper.writeValueAsString(lessonTime);
        
        mockMvc.perform(post("/lesson-time-parameters")
                .content(testJson)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
        
        verify(lessonTimeService).create(lessonTime);
    }
    
    @Test
    void shouldReturnError400WhenIllegalArgumentExceptionWhileCreateLessonTime() throws Exception {
        LessonTime lessonTime = new LessonTime();
        lessonTime.setStartTime(LocalTime.of(11, 0));
        lessonTime.setEndTime(LocalTime.of(12, 0));
        
        doThrow(serviceWithIllegalArgumentException).when(lessonTimeService).create(lessonTime);
        
        String testJson = objectMapper.writeValueAsString(lessonTime);
        
        mockMvc.perform(post("/lesson-time-parameters")
                .content(testJson)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
        
        verify(lessonTimeService).create(lessonTime);
    }
    
    @Test
    void shouldReturnError500WhenServiceExceptionWhileCreateLessonTime() throws Exception {
        LessonTime lessonTime = new LessonTime();
        lessonTime.setStartTime(LocalTime.of(12, 0));
        lessonTime.setEndTime(LocalTime.of(13, 0));
        
        doThrow(ServiceException.class).when(lessonTimeService).create(lessonTime);
        
        String testJson = objectMapper.writeValueAsString(lessonTime);
        
        mockMvc.perform(post("/lesson-time-parameters")
                .content(testJson)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError());
        
        verify(lessonTimeService).create(lessonTime);
    }
       
    @Test
    void shouldReturnError500WhenRepositoryExceptionWhileUpdateLessonTime() throws Exception {
        int testId = 12;
        LessonTime lessonTime = new LessonTime();
        lessonTime.setId(testId);
        lessonTime.setStartTime(LocalTime.of(14, 0));
        lessonTime.setEndTime(LocalTime.of(15, 0));
        
        doThrow(serviceWithRepositoryException).when(lessonTimeService).update(lessonTime);
        
        String testJson = objectMapper.writeValueAsString(lessonTime);
        
        mockMvc.perform(patch("/lesson-time-parameters/{id}", testId)
                .content(testJson)
                .contentType(MediaType.APPLICATION_JSON))
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
        
        String testJson = objectMapper.writeValueAsString(lessonTime);
        
        mockMvc.perform(patch("/lesson-time-parameters/{id}", testId)
                .content(testJson)
                .contentType(MediaType.APPLICATION_JSON))
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
        
        String testJson = objectMapper.writeValueAsString(lessonTime);
        
        mockMvc.perform(patch("/lesson-time-parameters/{id}", testId)
                .content(testJson)
                .contentType(MediaType.APPLICATION_JSON))
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
        
        String testJson = objectMapper.writeValueAsString(lessonTime);
        
        mockMvc.perform(patch("/lesson-time-parameters/{id}", testId)
                .content(testJson)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError());
        
        verify(lessonTimeService).update(lessonTime);
    }
    
    @Test
    void shouldReturn500WhenRepositoryExceptionWhileDeleteLessonTime() throws Exception {
        int testId = 741;
        
        doThrow(serviceWithRepositoryException).when(lessonTimeService).deleteById(testId);
        
        mockMvc.perform(delete("/lesson-time-parameters/{id}", testId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError());
        
        verify(lessonTimeService).deleteById(testId);
    }
    
    @Test
    void shouldReturn400WhenIllegalArgumentExceptionWhileDeleteLessonTime() throws Exception {
        int testId = 123;
        
        doThrow(serviceWithIllegalArgumentException).when(lessonTimeService).deleteById(testId);
        
        mockMvc.perform(delete("/lesson-time-parameters/{id}", testId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
        
        verify(lessonTimeService).deleteById(testId);
    }
    
    @Test
    void shouldReturn500WhenServiceExceptionWhileDeleteLessonTime() throws Exception {
        int testId = 854;
        
        doThrow(ServiceException.class).when(lessonTimeService).deleteById(testId);
        
        mockMvc.perform(delete("/lesson-time-parameters/{id}", testId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError());
        
        verify(lessonTimeService).deleteById(testId);
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
        
        String testJson = objectMapper.writeValueAsString(lesson);
        
        mockMvc.perform(post("/lessons")
                .content(testJson)
                .param("lesson-time-id", Integer.toString(lessonTime.getId()))
                .param("group-id", Integer.toString(group.getId()))
                .param("lecturer-id", Integer.toString(lecturer.getId()))
                .contentType(MediaType.APPLICATION_JSON))
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
        
        when(groupService.getById(group.getId())).thenReturn(group);
        when(lecturerService.getById(lecturer.getId())).thenReturn(lecturer);
        when(lessonTimeService.getById(lessonTime.getId())).thenReturn(lessonTime);
        
        doThrow(serviceWithConstraintViolationException).when(lessonService).create(lesson);
        
        String testJson = objectMapper.writeValueAsString(lesson);
        
        mockMvc.perform(post("/lessons")
                .content(testJson)
                .param("lesson-time-id", Integer.toString(lessonTime.getId()))
                .param("group-id", Integer.toString(group.getId()))
                .param("lecturer-id", Integer.toString(lecturer.getId()))
                .contentType(MediaType.APPLICATION_JSON))
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
        
        when(groupService.getById(group.getId())).thenReturn(group);
        when(lecturerService.getById(lecturer.getId())).thenReturn(lecturer);
        when(lessonTimeService.getById(lessonTime.getId())).thenReturn(lessonTime);
        
        doThrow(serviceWithIllegalArgumentException).when(lessonService).create(lesson);
        
        String testJson = objectMapper.writeValueAsString(lesson);
        
        mockMvc.perform(post("/lessons")
                .content(testJson)
                .param("lesson-time-id", Integer.toString(lessonTime.getId()))
                .param("group-id", Integer.toString(group.getId()))
                .param("lecturer-id", Integer.toString(lecturer.getId()))
                .contentType(MediaType.APPLICATION_JSON))
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
        
        String testJson = objectMapper.writeValueAsString(lesson);
        
        mockMvc.perform(post("/lessons")
                .content(testJson)
                .param("lesson-time-id", Integer.toString(lessonTime.getId()))
                .param("group-id", Integer.toString(group.getId()))
                .param("lecturer-id", Integer.toString(lecturer.getId()))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError());
        
        verify(lessonTimeService).getById(lessonTime.getId());
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
        
        String testJson = objectMapper.writeValueAsString(lesson);
        
        mockMvc.perform(patch("/lessons/{id}", lessonId)
                .content(testJson)
                .param("lesson-time-id", Integer.toString(lessonTime.getId()))
                .param("group-id", Integer.toString(group.getId()))
                .param("lecturer-id", Integer.toString(lecturer.getId()))
                .contentType(MediaType.APPLICATION_JSON))
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
        
        when(groupService.getById(group.getId())).thenReturn(group);
        when(lecturerService.getById(lecturer.getId())).thenReturn(lecturer);
        when(lessonTimeService.getById(lessonTime.getId())).thenReturn(lessonTime);
        
        doThrow(serviceWithConstraintViolationException).when(lessonService).update(lesson);
        
        String testJson = objectMapper.writeValueAsString(lesson);
        
        mockMvc.perform(patch("/lessons/{id}", lessonId)
                .content(testJson)
                .param("lesson-time-id", Integer.toString(lessonTime.getId()))
                .param("group-id", Integer.toString(group.getId()))
                .param("lecturer-id", Integer.toString(lecturer.getId()))
                .contentType(MediaType.APPLICATION_JSON))
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
        
        when(lessonTimeService.getById(lessonTime.getId())).thenReturn(lessonTime);
        when(lecturerService.getById(lecturer.getId())).thenReturn(lecturer);
        when(groupService.getById(group.getId())).thenReturn(group);
        
        doThrow(serviceWithIllegalArgumentException).when(lessonService).update(lesson);
        
        String testJson = objectMapper.writeValueAsString(lesson);
        
        mockMvc.perform(patch("/lessons/{id}", lessonId)
                .content(testJson)
                .param("lesson-time-id", Integer.toString(lessonTime.getId()))
                .param("group-id", Integer.toString(group.getId()))
                .param("lecturer-id", Integer.toString(lecturer.getId()))
                .contentType(MediaType.APPLICATION_JSON))
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
        
        String testJson = objectMapper.writeValueAsString(lesson);
        
        mockMvc.perform(patch("/lessons/{id}", lessonId)
                .content(testJson)
                .param("lesson-time-id", Integer.toString(lessonTime.getId()))
                .param("group-id", Integer.toString(group.getId()))
                .param("lecturer-id", Integer.toString(lecturer.getId()))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError());
        
        verify(lessonTimeService).getById(lessonTime.getId());
    }
    
    @Test
    void shouldReturnError500WhenRepositoryExceptionWhileDeleteLesson() throws Exception {
        int testId = 2;
        
        doThrow(serviceWithRepositoryException).when(lessonService).deleteById(testId);
        
        mockMvc.perform(delete("/lessons/{id}", testId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError());
        
        verify(lessonService).deleteById(testId);
    }
    
    @Test
    void shouldReturnError400WhenIllegalExceptionWhileDeleteLesson() throws Exception {
        int testId = 41;
        
        doThrow(serviceWithIllegalArgumentException).when(lessonService).deleteById(testId);
        
        mockMvc.perform(delete("/lessons/{id}", testId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
        
        verify(lessonService).deleteById(testId);
    }
    
    @Test
    void shouldReturnError500WhenServiceExceptionWhileDeleteLesson() throws Exception {
        int testId = 63;
        
        doThrow(ServiceException.class).when(lessonService).deleteById(testId);
        
        mockMvc.perform(delete("/lessons/{id}", testId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError());
        
        verify(lessonService).deleteById(testId);
    }
}
