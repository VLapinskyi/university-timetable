package ua.com.foxminded.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import ua.com.foxminded.domain.LessonTime;

public class LessonTimeMapper implements RowMapper<LessonTime> {

    @Override
    public LessonTime mapRow(ResultSet rs, int rowNum) throws SQLException {
        LessonTime lessonTime = new LessonTime();
        lessonTime.setId(rs.getInt("id"));
        lessonTime.setStartTime(rs.getTime("start_date").toLocalTime());
        lessonTime.setEndTime(rs.getTime("end_time").toLocalTime());
        return lessonTime;
    }

}
