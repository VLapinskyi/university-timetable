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
    private static final String ROLE = "student";
    private final JdbcTemplate jdbcTemplate;
    private Environment environment;
    
    @Autowired
    public StudentDAO (JdbcTemplate jdbcTemplate, Environment environment) {
        this.jdbcTemplate = jdbcTemplate;
        this.environment = environment;
    }

    @Override
    public void create(Student student) {
      jdbcTemplate.update(environment.getProperty("create.person"), ROLE, 
              student.getFirstName(), student.getLastName(), student.getGender().name(),
              student.getPhoneNumber(), student.getEmail());
    }

    @Override
    public List<Student> findAll() {
        return jdbcTemplate.query(environment.getProperty("find.all.people.by.role"), new StudentMapper(), ROLE);
    }

    @Override
    public Student findById(int id) {
        return jdbcTemplate.queryForStream(environment.getProperty("find.person.by.id"),
                new StudentMapper(), id, ROLE).findAny().orElse(null);
    }

    @Override
    public void update(int id, Student student) {
        jdbcTemplate.update(environment.getProperty("update.person"),
                student.getFirstName(), student.getLastName(), student.getGender().toString(),
                student.getPhoneNumber(), student.getEmail(),  id, ROLE);
        
    }

    @Override
    public void deleteById(int id) {
        jdbcTemplate.update(environment.getProperty("delete.person"), id, ROLE);
    } 
}
