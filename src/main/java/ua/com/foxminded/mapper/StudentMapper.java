package ua.com.foxminded.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import ua.com.foxminded.domain.Gender;
import ua.com.foxminded.domain.Student;

public class StudentMapper implements RowMapper<Student> {

    @Override
    public Student mapRow(ResultSet rs, int rowNum) throws SQLException {
        Student student = new Student();
        student.setId(rs.getInt("id"));
        student.setFirstName(rs.getString("first_name"));
        student.setLastName(rs.getString("last_name"));
        student.setGender(Gender.valueOf(rs.getString("gender")));
        student.setPhoneNumber(rs.getString("phone_number"));
        student.setEmail(rs.getString("email"));
        return student;
    }

}
