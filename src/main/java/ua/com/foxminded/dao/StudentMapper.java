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
        Group group = new Group();
        Faculty faculty = new Faculty();
        student.setId(rs.getInt("student_id"));
        student.setFirstName(rs.getString("p.first_name"));
        student.setLastName(rs.getString("p.last_name"));
        student.setGender(Gender.valueOf(rs.getString("p.gender")));
        student.setPhoneNumber(rs.getString("p.phone_number"));
        student.setEmail(rs.getString("p.email"));
        group.setId(rs.getInt("group_id"));
        group.setName(rs.getString("group_name"));
        faculty.setId(rs.getInt("faculty_id"));
        faculty.setName(rs.getString("faculty_name"));
        group.setFaculty(faculty);
        student.setGroup(group);
        return student;
    }

}
