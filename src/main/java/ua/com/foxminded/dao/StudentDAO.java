package ua.com.foxminded.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import ua.com.foxminded.domain.Student;
import ua.com.foxminded.mapper.StudentMapper;

@Repository
public class StudentDAO implements GenericDAO<Student> {
    private final JdbcTemplate jdbcTemplate;
    private Environment environment;
    
    @Autowired
    public StudentDAO (JdbcTemplate jdbcTemplate, Environment environment) {
        this.jdbcTemplate = jdbcTemplate;
        this.environment = environment;
    }

    @Override
    public void create(Student student) {
      jdbcTemplate.update(environment.getProperty("create.student"), 
              student.getFirstName(), student.getLastName(), student.getGender().name(),
              student.getPhoneNumber(), student.getEmail(),
              student.getGroup() == null ? null : student.getGroup().getId());
    }

    @Override
    public List<Student> findAll() {
        return jdbcTemplate.query(environment.getProperty("find.all.students"), new StudentMapper());
    }

    @Override
    public Student findById(int id) {
        return jdbcTemplate.queryForStream(environment.getProperty("find.student.by.id"),
                new StudentMapper(), id).findAny().orElse(null);
    }

    @Override
    public void update(int id, Student student) {
        jdbcTemplate.update(environment.getProperty("update.student"),
                student.getFirstName(), student.getLastName(), student.getGender().toString(),
                student.getPhoneNumber(), student.getEmail(), student.getGroup().getId(),  id);
        
    }

    @Override
    public void deleteById(int id) {
        jdbcTemplate.update(environment.getProperty("delete.person"), id);
    } 
}
