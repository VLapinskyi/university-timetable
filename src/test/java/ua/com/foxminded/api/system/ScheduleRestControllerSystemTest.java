package ua.com.foxminded.api.system;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.junit5.api.DBRider;

import ua.com.foxminded.domain.Faculty;
import ua.com.foxminded.domain.Gender;
import ua.com.foxminded.domain.Group;
import ua.com.foxminded.domain.Lecturer;
import ua.com.foxminded.domain.Lesson;
import ua.com.foxminded.domain.LessonTime;
import ua.com.foxminded.repositories.exceptions.RepositoryException;
import ua.com.foxminded.service.LessonService;
import ua.com.foxminded.service.LessonTimeService;
import ua.com.foxminded.service.exceptions.ServiceException;

@SpringBootTest
@DBRider
@DBUnit(cacheConnection = false, leakHunter = true)
@TestPropertySource("/application-test.properties")
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
class ScheduleRestControllerSystemTest {

    private final String testData = "/datasets/test-data.xml";

    @SpyBean
    @Autowired
    private LessonService lessonService;

    @SpyBean
    @Autowired
    private LessonTimeService lessonTimeService;

    private Group firstGroup;
    private Group secondGroup;
    private Group thirdGroup;
    private Lecturer firstLecturer;
    private Lecturer secondLecturer;
    private Lecturer thirdLecturer;
    private LessonTime firstLessonTime;
    private LessonTime secondLessonTime;
    private LessonTime thirdLessonTime;

    List<Lesson> lessons = new ArrayList<>();

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void init() throws Exception {

        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        Faculty firstFaculty = new Faculty();
        firstFaculty.setId(1);
        firstFaculty.setName("TestFaculty1");

        Faculty secondFaculty = new Faculty();
        secondFaculty.setId(2);
        secondFaculty.setName("TestFaculty2");

        firstGroup = new Group();
        firstGroup.setId(1);
        firstGroup.setName("TestGroup1");
        firstGroup.setFaculty(firstFaculty);

        secondGroup = new Group();
        secondGroup.setId(2);
        secondGroup.setName("TestGroup2");
        secondGroup.setFaculty(secondFaculty);

        thirdGroup = new Group();
        thirdGroup.setId(3);
        thirdGroup.setName("TestGroup3");
        thirdGroup.setFaculty(firstFaculty);

        firstLecturer = new Lecturer();
        firstLecturer.setId(4);
        firstLecturer.setFirstName("Olena");
        firstLecturer.setLastName("Skladenko");
        firstLecturer.setGender(Gender.FEMALE);
        firstLecturer.setPhoneNumber("+380991111111");
        firstLecturer.setEmail("oskladenko@gmail.com");

        secondLecturer = new Lecturer();
        secondLecturer.setId(5);
        secondLecturer.setFirstName("Ihor");
        secondLecturer.setLastName("Zakharchuk");
        secondLecturer.setGender(Gender.MALE);
        secondLecturer.setPhoneNumber("+380125263741");
        secondLecturer.setEmail("i.zakharchuk@gmail.com");

        thirdLecturer = new Lecturer();
        thirdLecturer.setId(6);
        thirdLecturer.setFirstName("Vasyl");
        thirdLecturer.setLastName("Dudchenko");
        thirdLecturer.setGender(Gender.MALE);
        thirdLecturer.setPhoneNumber("+380457895263");
        thirdLecturer.setEmail("vdudchenko@test.com");

        firstLessonTime = new LessonTime();
        firstLessonTime.setId(1);
        firstLessonTime.setStartTime(LocalTime.of(9, 0));
        firstLessonTime.setEndTime(LocalTime.of(10, 30));

        secondLessonTime = new LessonTime();
        secondLessonTime.setId(2);
        secondLessonTime.setStartTime(LocalTime.of(10, 45));
        secondLessonTime.setEndTime(LocalTime.of(12, 15));

        thirdLessonTime = new LessonTime();
        thirdLessonTime.setId(3);
        thirdLessonTime.setStartTime(LocalTime.of(12, 30));
        thirdLessonTime.setEndTime(LocalTime.of(14, 0));

        List<String> lessonsNames = new ArrayList<>(Arrays.asList("Ukranian", "Music", "Physical Exercises", "Physical Exercises"));
        List<Lecturer> lessonLecturers = new ArrayList<>(Arrays.asList(firstLecturer, secondLecturer, thirdLecturer, thirdLecturer));
        List<Group> lessonGroups = new ArrayList<>(Arrays.asList(firstGroup, thirdGroup, firstGroup, secondGroup));
        List<String> lessonAudiences = new ArrayList<>(Arrays.asList("101", "102", "103", "103"));
        List<DayOfWeek> lessonDays = new ArrayList<>(Arrays.asList(DayOfWeek.SUNDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.WEDNESDAY));
        List<LessonTime> lessonTimes = new ArrayList<>(Arrays.asList(firstLessonTime, firstLessonTime, thirdLessonTime, secondLessonTime));

        for (int i = 0; i < 4; i++) {
            Lesson lesson = new Lesson();
            lesson.setId(i + 1);
            lesson.setName(lessonsNames.get(i));
            lesson.setLecturer(lessonLecturers.get(i));
            lesson.setGroup(lessonGroups.get(i));
            lesson.setAudience(lessonAudiences.get(i));
            lesson.setDay(lessonDays.get(i));
            lesson.setLessonTime(lessonTimes.get(i));
            lessons.add(lesson);
        }
    }

    @Test
    @DataSet(value = testData, cleanBefore = true)
    void shouldGetLecturerWeekLessons() throws Exception {
        int lecturerId = 5;

        Map<DayOfWeek, List<Lesson>> lecturerWeekLessons = new HashMap<>();
        lecturerWeekLessons.put(lessons.get(1).getDay(), Arrays.asList(lessons.get(1)));

        String expectedResult = objectMapper.writeValueAsString(lecturerWeekLessons);

        mockMvc.perform(get("/week-lessons/find-for-lecturer")
                .param("lecturer-id", Integer.toString(lecturerId))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(content().json(expectedResult));

        verify(lessonService).getLecturerWeekLessons(lecturerId);
    }

    @Test
    @DataSet(value = testData, cleanBefore = true)
    void shouldGetLecturerMonthLessons() throws Exception {
        int lecturerId = 5;

        Map<LocalDate, List<Lesson>> lecturerMonthLessons = new HashMap<>();
        YearMonth month = YearMonth.of(2021, 3);

        for (int i = 1; i <= 31; i++) {
            for(int a = 0; a < lessons.size(); a++) {
                if (month.atDay(i).getDayOfWeek().equals(lessons.get(a).getDay())
                        && lessons.get(a).getLecturer().getId() == lecturerId) {
                    lecturerMonthLessons.put(LocalDate.of(month.getYear(), month.getMonthValue(), i),
                            Arrays.asList(lessons.get(a)));
                    System.out.println(lessons.get(a));
                }
            }
        }

        String expectedResult = objectMapper.writeValueAsString(lecturerMonthLessons);

        mockMvc.perform(get("/month-lessons/find-for-lecturer")
                .param("lecturer-id", Integer.toString(lecturerId))
                .param("month-value", month.toString())
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(content().json(expectedResult));

        verify(lessonService).getLecturerMonthLessons(lecturerId, month);
    }

    @Test
    @DataSet(value = testData, cleanBefore = true)
    void shouldGetGroupWeekLessons() throws Exception {
        int groupId = 2;

        Map<DayOfWeek, List<Lesson>> groupWeekLessons = new HashMap<>();
        groupWeekLessons.put(lessons.get(3).getDay(), Arrays.asList(lessons.get(3)));

        String expectedResult = objectMapper.writeValueAsString(groupWeekLessons);

        mockMvc.perform(get("/week-lessons/find-for-group")
                .param("group-id", Integer.toString(groupId))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(content().json(expectedResult));

        verify(lessonService).getGroupWeekLessons(groupId);
    }

    @Test
    @DataSet(value = testData, cleanBefore = true)
    void shouldGetGroupMonthLessons() throws Exception {
        int groupId = 1;
        Map<LocalDate, List<Lesson>> groupMonthLessons = new HashMap<>();
        YearMonth month = YearMonth.of(2021, 2);

        for (int i = 1; i <= 28; i++) {
            for(int a = 0; a < lessons.size(); a++) {
                if (month.atDay(i).getDayOfWeek().equals(lessons.get(a).getDay())
                        && lessons.get(a).getGroup().getId() == groupId) {
                    groupMonthLessons.put(LocalDate.of(month.getYear(), month.getMonthValue(), i),
                            Arrays.asList(lessons.get(a)));
                }
            }
        }

        String expectedResult = objectMapper.writeValueAsString(groupMonthLessons);

        mockMvc.perform(get("/month-lessons/find-for-group")
                .param("group-id", Integer.toString(groupId))
                .param("month-value", month.toString())
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(content().json(expectedResult));

        verify(lessonService).getGroupMonthLessons(groupId, month);
    }

    @Test
    @DataSet(value = testData, cleanBefore = true)
    @ExpectedDataSet("/datasets/lesson-times/all-lesson-times.xml")
    void shouldGetLessonTimeParameters() throws Exception {
        mockMvc.perform(get("/lesson-time-parameters")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        verify(lessonTimeService).getAll();
    }

    @Test
    @DataSet(cleanBefore = true)
    @ExpectedDataSet("/datasets/lesson-times/after-creating.xml")
    void shouldCreateLessonTimeParatemer() throws Exception {
        firstLessonTime.setId(0);
        String testJson = objectMapper.writeValueAsString(firstLessonTime);

        mockMvc.perform(post("/lesson-time-parameters")
                .content(testJson)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());    
    }

    @Test
    @DataSet(value = testData, cleanBefore = true)
    @ExpectedDataSet("/datasets/lesson-times/after-updating.xml")
    void shouldUpdateLessonTime() throws Exception {
        firstLessonTime.setEndTime(LocalTime.of(11, 0));

        String testJson = objectMapper.writeValueAsString(firstLessonTime);

        mockMvc.perform(patch("/lesson-time-parameters/{id}", firstLessonTime.getId())
                .content(testJson).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().json(testJson))
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        verify(lessonTimeService).update(firstLessonTime);
    }

    @Test
    @DataSet(value = testData, cleanBefore = true, disableConstraints = true)
    @ExpectedDataSet("datasets/lesson-times/after-deleting.xml")
    void shouldDeleteLessonTime() throws Exception {
        int testId = 2;

        String expectedResult = "LessonTime with id: " + testId + " was deleted.";

        mockMvc.perform(delete("/lesson-time-parameters/{id}", testId)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().string(expectedResult))
        .andExpect(status().isOk());

        verify(lessonTimeService).deleteById(testId);
    }

    @Test
    @DataSet(value = testData, cleanBefore = true)
    @ExpectedDataSet("/datasets/lessons/after-creating.xml")
    void shouldCreateLesson() throws Exception {
        Lesson lesson = new Lesson();
        lesson.setName("test");
        lesson.setAudience("101");
        lesson.setDay(DayOfWeek.FRIDAY);
        lesson.setGroup(firstGroup);
        lesson.setLecturer(firstLecturer);
        lesson.setLessonTime(secondLessonTime);
        
        String testJson = objectMapper.writeValueAsString(lesson);

        mockMvc.perform(post("/lessons")
                .content(testJson)
                .param("lesson-time-id", Integer.toString(secondLessonTime.getId()))
                .param("group-id", Integer.toString(firstGroup.getId()))
                .param("lecturer-id", Integer.toString(lesson.getLecturer().getId()))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

    }

    @Test
    @DataSet(value = testData, cleanBefore = true)
    @ExpectedDataSet("/datasets/lessons/after-updating.xml")
    void shouldUpdateLesson() throws Exception {
        Lesson lesson = lessons.get(3);
        lesson.setDay(DayOfWeek.FRIDAY);

        String testJson = objectMapper.writeValueAsString(lesson);

        mockMvc.perform(patch("/lessons/{id}", lesson.getId())
                .content(testJson)
                .param("lesson-time-id",Integer.toString(lesson.getLessonTime().getId()))
                .param("lecturer-id", Integer.toString(lesson.getLecturer().getId()))
                .param("group-id", Integer.toString(lesson.getGroup().getId()))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().json(testJson))
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DataSet(value = testData, cleanBefore = true, disableConstraints = true)
    @ExpectedDataSet("/datasets/lessons/after-deleting.xml")
    void shouldDeleteLesson() throws Exception {
        int testId = 4;

        String expectedResult = "Lesson with id: " + testId + " was deleted.";

        mockMvc.perform(delete("/lessons/{id}", testId)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().string(expectedResult));

        verify(lessonService).deleteById(testId);
    }

    @Test
    void shouldReturnError400WhenIllegalArgumentExceptionWhileGetGroupWeekLessons() throws Exception {
        int groupId = -3;

        mockMvc.perform(get("/week-lessons/find-for-group")
                .param("group-id", Integer.toString(groupId))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnError500WhenRepositoryExceptionWhileCreateLessonTime() throws Exception {
        LessonTime lessonTime = new LessonTime();
        lessonTime.setStartTime(LocalTime.of(9, 0));
        lessonTime.setEndTime(LocalTime.of(10, 0));

        doThrow(new ServiceException("Service exception", new RepositoryException())).when(lessonTimeService).create(lessonTime);

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
        lessonTime.setStartTime(LocalTime.of(12, 0));
        lessonTime.setEndTime(LocalTime.of(11, 0));

        String testJson = objectMapper.writeValueAsString(lessonTime);

        mockMvc.perform(post("/lesson-time-parameters")
                .content(testJson)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnError400WhenIllegalArgumentExceptionWhileCreateLessonTime() throws Exception {
        LessonTime lessonTime = null;

        String testJson = objectMapper.writeValueAsString(lessonTime);

        mockMvc.perform(post("/lesson-time-parameters")
                .content(testJson)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnError400WhenConstraintViolationExceptionWhileUpdateLessonTime() throws Exception {
        int testId = 1;
        LessonTime lessonTime = new LessonTime();
        lessonTime.setId(testId);
        lessonTime.setStartTime(LocalTime.of(17, 0));
        lessonTime.setEndTime(LocalTime.of(16, 0));

        String testJson = objectMapper.writeValueAsString(lessonTime);

        mockMvc.perform(patch("/lesson-time-parameters/{id}", testId)
                .content(testJson)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnError400WhenIllegalArgumentExceptionWhileUpdateLessonTime() throws Exception {
        int testId = 5;
        LessonTime lessonTime = null;

        String testJson = objectMapper.writeValueAsString(lessonTime);

        mockMvc.perform(patch("/lesson-time-parameters/{id}", testId)
                .content(testJson)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
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
    }

    @Test
    void shouldReturn500WhenRepositoryExceptionWhileDeleteLessonTime() throws Exception {
        int testId = 741;

        mockMvc.perform(delete("/lesson-time-parameters/{id}", testId)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError());

        verify(lessonTimeService).deleteById(testId);
    }

    @Test
    void shouldReturn400WhenIllegalArgumentExceptionWhileDeleteLessonTime() throws Exception {
        int testId = -123;

        mockMvc.perform(delete("/lesson-time-parameters/{id}", testId)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
    }

    @Test
    @DataSet(value = testData, disableConstraints = true)
    void shouldReturn500WhenServiceExceptionWhileDeleteLessonTime() throws Exception {
        int testId = 1;

        doThrow(ServiceException.class).when(lessonTimeService).deleteById(testId);

        mockMvc.perform(delete("/lesson-time-parameters/{id}", testId)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError());

        verify(lessonTimeService).deleteById(testId);
    }

    @Test
    void shouldReturnError500WhenRepositoryExceptionWhileCreateLesson() throws Exception {
        Lesson lesson = lessons.get(0);

        String testJson = objectMapper.writeValueAsString(lesson);

        mockMvc.perform(post("/lessons")
                .content(testJson)
                .param("lesson-time-id", Integer.toString(lesson.getLessonTime().getId()))
                .param("group-id", Integer.toString(lesson.getGroup().getId()))
                .param("lecturer-id", Integer.toString(lesson.getLecturer().getId()))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError());
    }

    @Test
    @DataSet(value = testData, cleanBefore = true)
    void shouldReturnError400WhenConstraintViolationExceptionWhileCreateLesson() throws Exception {
        Lesson lesson = new Lesson();
        lesson.setName(" Test lesson");
        lesson.setAudience("102");
        lesson.setDay(DayOfWeek.TUESDAY);
        lesson.setGroup(firstGroup);
        lesson.setLecturer(firstLecturer);
        lesson.setLessonTime(firstLessonTime);

        String testJson = objectMapper.writeValueAsString(lesson);

        mockMvc.perform(post("/lessons")
                .content(testJson)
                .param("lesson-time-id", Integer.toString(firstLessonTime.getId()))
                .param("group-id", Integer.toString(firstGroup.getId()))
                .param("lecturer-id", Integer.toString(firstLecturer.getId()))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
    }

    @Test
    @DataSet(value = testData, cleanBefore = true)
    void shouldReturnError400WhenIllegalArgumentExceptionWhileCreateLesson() throws Exception {
        Lesson lesson = new Lesson();
        lesson.setId(-3);
        lesson.setName("Test lesson");
        lesson.setAudience("100");
        lesson.setDay(DayOfWeek.WEDNESDAY);
        lesson.setGroup(firstGroup);
        lesson.setLecturer(firstLecturer);
        lesson.setLessonTime(firstLessonTime);

        String testJson = objectMapper.writeValueAsString(lesson);

        mockMvc.perform(post("/lessons")
                .content(testJson)
                .param("lesson-time-id", Integer.toString(firstLessonTime.getId()))
                .param("group-id", Integer.toString(firstGroup.getId()))
                .param("lecturer-id", Integer.toString(firstLecturer.getId()))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnError500WhenServiceExceptionWhileCreateLesson() throws Exception {
        Lesson lesson = new Lesson();
        lesson.setName("Test lesson");
        lesson.setAudience("104");
        lesson.setDay(DayOfWeek.THURSDAY);
        lesson.setGroup(secondGroup);
        lesson.setLecturer(secondLecturer);
        lesson.setLessonTime(secondLessonTime);

        String testJson = objectMapper.writeValueAsString(lesson);

        mockMvc.perform(post("/lessons")
                .content(testJson)
                .param("lesson-time-id", Integer.toString(secondLessonTime.getId()))
                .param("group-id", Integer.toString(secondGroup.getId()))
                .param("lecturer-id", Integer.toString(secondLecturer.getId()))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError());
    }

    @Test
    void shouldReturnError500WhenRepositoryExceptionWhileUpdateLesson() throws Exception {
        int lessonId = 3;
        Lesson lesson = new Lesson();
        lesson.setId(lessonId);
        lesson.setName("Test lesson");
        lesson.setAudience("10");
        lesson.setDay(DayOfWeek.MONDAY);
        lesson.setGroup(secondGroup);
        lesson.setLecturer(secondLecturer);
        lesson.setLessonTime(secondLessonTime);

        String testJson = objectMapper.writeValueAsString(lesson);

        mockMvc.perform(patch("/lessons/{id}", lessonId)
                .content(testJson)
                .param("lesson-time-id", Integer.toString(secondLessonTime.getId()))
                .param("group-id", Integer.toString(secondGroup.getId()))
                .param("lecturer-id", Integer.toString(secondLecturer.getId()))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError());
    }

    @Test
    @DataSet(value = testData, cleanBefore = true)
    void shouldReturnError400WhenConstraintViolationExceptionWhileUpdateLesson() throws Exception {
        int lessonId = 2;
        Lesson lesson = new Lesson();
        lesson.setId(lessonId);
        lesson.setName(" Test lesson");
        lesson.setAudience("110");
        lesson.setDay(DayOfWeek.TUESDAY);
        lesson.setGroup(firstGroup);
        lesson.setLecturer(firstLecturer);
        lesson.setLessonTime(firstLessonTime);

        String testJson = objectMapper.writeValueAsString(lesson);

        mockMvc.perform(patch("/lessons/{id}", lessonId)
                .content(testJson)
                .param("lesson-time-id", Integer.toString(firstLessonTime.getId()))
                .param("group-id", Integer.toString(firstGroup.getId()))
                .param("lecturer-id", Integer.toString(firstLecturer.getId()))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnError400WhenIllegalArgumentExceptionWhileUpdateLesson() throws Exception {
        int lessonId = 14;
        Lesson lesson = null;

        String testJson = objectMapper.writeValueAsString(lesson);

        mockMvc.perform(patch("/lessons/{id}", lessonId)
                .content(testJson)
                .param("lesson-time-id", Integer.toString(firstLessonTime.getId()))
                .param("group-id", Integer.toString(firstGroup.getId()))
                .param("lecturer-id", Integer.toString(firstLecturer.getId()))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnError500WhenServiceExceptionWhileUpdateLesson() throws Exception {
        int lessonId = 21;
        Lesson lesson = new Lesson();
        lesson.setId(lessonId);
        lesson.setName("Test lesson");
        lesson.setAudience("20");
        lesson.setDay(DayOfWeek.MONDAY);
        lesson.setGroup(secondGroup);
        lesson.setLecturer(secondLecturer);
        lesson.setLessonTime(secondLessonTime);

        String testJson = objectMapper.writeValueAsString(lesson);

        mockMvc.perform(patch("/lessons/{id}", lessonId)
                .content(testJson)
                .param("lesson-time-id", Integer.toString(secondLessonTime.getId()))
                .param("group-id", Integer.toString(secondGroup.getId()))
                .param("lecturer-id", Integer.toString(secondLecturer.getId()))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError());
    }

    @Test
    void shouldReturnError500WhenRepositoryExceptionWhileDeleteLesson() throws Exception {
        int testId = 2;

        mockMvc.perform(delete("/lessons/{id}", testId)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError());

        verify(lessonService).deleteById(testId);
    }

    @Test
    void shouldReturnError400WhenIllegalExceptionWhileDeleteLesson() throws Exception {
        int testId = -41;

        mockMvc.perform(delete("/lessons/{id}", testId)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
    }

    @Test
    @DataSet(value = testData, cleanBefore = true, disableConstraints = true)
    void shouldReturnError500WhenServiceExceptionWhileDeleteLesson() throws Exception {
        int testId = 4;

        doThrow(ServiceException.class).when(lessonService).deleteById(testId);

        mockMvc.perform(delete("/lessons/{id}", testId)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError());

        verify(lessonService).deleteById(testId);
    }
}
