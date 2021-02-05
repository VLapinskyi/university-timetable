package ua.com.foxminded.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import ua.com.foxminded.domain.Faculty;
import ua.com.foxminded.domain.Group;

public class GroupMapper implements RowMapper<Group> {

    @Override
    public Group mapRow(ResultSet rs, int rowNum) throws SQLException {
        Group group = new Group();
        group.setId(rs.getInt("group_id"));
        group.setName(rs.getString("group_name"));
        if (rs.getInt("faculty_id") > 0) {
            Faculty faculty = new Faculty();
            faculty.setId(rs.getInt("faculty_id"));
            faculty.setName(rs.getString("faculty_name"));
            group.setFaculty(faculty);
        }
        return group;
    }
}
