package ua.com.foxminded.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;

import org.springframework.jdbc.core.RowMapper;

import ua.com.foxminded.domain.Faculty;
import ua.com.foxminded.domain.Gender;
import ua.com.foxminded.domain.Group;
import ua.com.foxminded.domain.Lecturer;
import ua.com.foxminded.domain.Lesson;
import ua.com.foxminded.domain.LessonTime;

public class LessonMapper implements RowMapper<Lesson> {

    @Override
    public Lesson mapRow(ResultSet rs, int rowNum) throws SQLException {
        Faculty faculty = new Faculty();
        faculty.setId(rs.getInt("faculty_id"));
        faculty.setName(rs.getString("faculty_name"));
        
        Group group = new Group();
        group.setId(rs.getInt("group_id"));
        group.setName(rs.getString("group_name"));
        group.setFaculty(faculty);
        
        Lecturer lecturer = new Lecturer();
        lecturer.setId(rs.getInt("lecturer_id"));
        lecturer.setFirstName(rs.getString("first_name"));
        lecturer.setLastName(rs.getString("last_name"));
        lecturer.setGender(Gender.valueOf(rs.getString("gender")));
        if (rs.getString("phone_number") != null) {
            lecturer.setPhoneNumber(rs.getString("phone_number"));
        }
        if (rs.getString("email") != null) {
            lecturer.setEmail(rs.getString("email"));
        }
        
        LessonTime lessonTime = new LessonTime();
        lessonTime.setId(rs.getInt("lesson_time_id"));
        lessonTime.setStartTime(rs.getTime("start_time").toLocalTime());
        lessonTime.setEndTime(rs.getTime("end_time").toLocalTime());
        
        Lesson lesson = new Lesson();
        lesson.setId(rs.getInt("lesson_id"));
        lesson.setName(rs.getString("lesson_name"));        
        lesson.setLecturer(lecturer);       
        lesson.setGroup(group);        
        lesson.setAudience(rs.getString("audience"));        
        lesson.setLessonTime(lessonTime);        
        lesson.setDay(DayOfWeek.of(rs.getInt("week_day")));        
        return lesson;
    }

}
