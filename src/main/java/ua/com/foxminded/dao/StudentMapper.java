package ua.com.foxminded.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import ua.com.foxminded.domain.Faculty;
import ua.com.foxminded.domain.Gender;
import ua.com.foxminded.domain.Group;
import ua.com.foxminded.domain.Student;

public class StudentMapper implements RowMapper<Student> {

    @Override
    public Student mapRow(ResultSet rs, int rowNum) throws SQLException {
        Student student = new Student();
        student.setId(rs.getInt("student_id"));
        student.setFirstName(rs.getString("first_name"));
        student.setLastName(rs.getString("last_name"));
        student.setGender(Gender.valueOf(rs.getString("gender")));
        student.setPhoneNumber(rs.getString("phone_number"));
        student.setEmail(rs.getString("email"));
        if(rs.getInt("group_id") > 0) {
            Group group = new Group();
            group.setId(rs.getInt("group_id"));
            group.setName(rs.getString("group_name"));
            if (rs.getInt("faculty_id") > 0) {
                Faculty faculty = new Faculty();
                faculty.setId(rs.getInt("faculty_id"));
                faculty.setName(rs.getString("faculty_name"));
                group.setFaculty(faculty);
            }
            student.setGroup(group);
        }
        return student;
    }

}
