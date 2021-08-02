package ua.com.foxminded.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import ua.com.foxminded.domain.Group;
import ua.com.foxminded.domain.Student;
import ua.com.foxminded.mapper.GroupMapper;
import ua.com.foxminded.mapper.StudentMapper;

@Repository
public class StudentDAO implements GenericDAO<Student> {
    private static final String ROLE = "student";

    private JdbcTemplate jdbcTemplate;

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
        return jdbcTemplate.queryForObject(environment.getProperty("find.person.by.id"),
                new StudentMapper(), id, ROLE);
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

    public void setStudentGroup(int groupId, int studentId) {
        jdbcTemplate.update(environment.getProperty("set.student.group"), groupId, studentId);
    }

    public Group getStudentGroup(int studentId) {
        return  jdbcTemplate.queryForObject(environment.getProperty("get.student.group"), new GroupMapper(), studentId);
    }
}
