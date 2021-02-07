package ua.com.foxminded.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import ua.com.foxminded.domain.Gender;
import ua.com.foxminded.domain.Lecturer;

public class LecturerMapper implements RowMapper<Lecturer> {

    @Override
    public Lecturer mapRow(ResultSet rs, int rowNum) throws SQLException {
        Lecturer lecturer = new Lecturer();
        lecturer.setId(rs.getInt("id"));
        lecturer.setFirstName(rs.getString("first_name"));
        lecturer.setLastName(rs.getString("last_name"));
        lecturer.setGender(Gender.valueOf(rs.getString("gender")));
        lecturer.setPhoneNumber(rs.getString("phone_number"));
        lecturer.setEmail(rs.getString("email"));
        return lecturer;
    }
}
