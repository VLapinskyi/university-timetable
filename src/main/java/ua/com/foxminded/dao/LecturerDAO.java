package ua.com.foxminded.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import ua.com.foxminded.domain.Lecturer;
import ua.com.foxminded.mapper.LecturerMapper;

@Repository
public class LecturerDAO implements GenericDAO<Lecturer> {
    private final JdbcTemplate jdbcTemplate;
    private Environment environment;
    
    @Autowired
    public LecturerDAO (JdbcTemplate jdbcTemplate, Environment environment) {
        this.jdbcTemplate = jdbcTemplate;
        this.environment = environment;
    }

    @Override
    public void create(Lecturer lecturer) {
        jdbcTemplate.update(environment.getProperty("create.lecturer"),
                lecturer.getFirstName(), lecturer.getLastName(), lecturer.getGender().toString(),
                lecturer.getPhoneNumber(), lecturer.getEmail());
    }

    @Override
    public List <Lecturer> findAll() {
        return jdbcTemplate.query(environment.getProperty("find.all.lecturers"), new LecturerMapper());
    }

    @Override
    public Lecturer findById(int id) {
        return jdbcTemplate.queryForStream(environment.getProperty("find.lecturer.by.id"), new LecturerMapper(), id)
                .findAny().orElse(null);
    }

    @Override
    public void update(int id, Lecturer lecturer) {
        jdbcTemplate.update(environment.getProperty("update.lecturer"), lecturer.getFirstName(), lecturer.getLastName(),
                lecturer.getGender().toString(), lecturer.getPhoneNumber(), lecturer.getEmail(), id);
        
    }

    @Override
    public void deleteById(int id) {
        jdbcTemplate.update(environment.getProperty("delete.person"), id);        
    }
}
