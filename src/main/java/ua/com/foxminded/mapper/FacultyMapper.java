package ua.com.foxminded.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import ua.com.foxminded.domain.Faculty;

public class FacultyMapper implements RowMapper<Faculty> {

    @Override
    public Faculty mapRow(ResultSet rs, int rowNum) throws SQLException {
        Faculty faculty = new Faculty();
        faculty.setId(rs.getInt("id"));
        faculty.setName(rs.getString("name"));
        return faculty;
    }
}
