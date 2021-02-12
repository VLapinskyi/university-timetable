package ua.com.foxminded.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import ua.com.foxminded.domain.Faculty;
import ua.com.foxminded.mapper.FacultyMapper;

@Repository
public class FacultyDAO implements GenericDAO<Faculty> {
    private final JdbcTemplate jdbcTemplate;
    private Environment environment;
    
    @Autowired
    public FacultyDAO(JdbcTemplate jdbcTemplate, Environment environment) {
        this.jdbcTemplate = jdbcTemplate;
        this.environment = environment;
    }
    
    @Override
    public void create(Faculty faculty) {
        jdbcTemplate.update(environment.getProperty("create.faculty"), faculty.getName());
    }

    @Override
    public List<Faculty> findAll() {
        return jdbcTemplate.query(environment.getProperty("find.all.faculties"), new FacultyMapper());
    }

    @Override
    public Faculty findById(int id) {
        return jdbcTemplate.queryForStream(environment.getProperty("find.faculty.by.id"), new FacultyMapper(), id)
                .findAny().orElse(null);
    }

    @Override
    public void update(int id, Faculty faculty) {
        jdbcTemplate.update(environment.getProperty("update.faculty"), faculty.getName(), id);        
    }

    @Override
    public void deleteById(int id) {
        jdbcTemplate.update(environment.getProperty("delete.faculty"), id);
        
    }
}
