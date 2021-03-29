package ua.com.foxminded.dao;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(StudentDAO.class);
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
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to insert new student: {}.", student);
        }
        
        jdbcTemplate.update(environment.getProperty("create.person"), ROLE, 
                student.getFirstName(), student.getLastName(), student.getGender().name(),
                student.getPhoneNumber(), student.getEmail());
        
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("The student {} was inserted.", student);
        }
    }

    @Override
    public List<Student> findAll() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to find all students.");
        }
        
        List<Student> resultStudents = jdbcTemplate.query(environment.getProperty("find.all.people.by.role"), new StudentMapper(), ROLE);
        
        if (resultStudents.isEmpty()) {
            LOGGER.warn("There are not any students in the result.");
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("The result is: {}.", resultStudents);
            }
        }
        
        return resultStudents;
    }

    @Override
    public Student findById(int id) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to find student by id {}.", id);
        }
        
        Student resultStudent = jdbcTemplate.queryForStream(environment.getProperty("find.person.by.id"),
                new StudentMapper(), id, ROLE).findAny().orElse(null);
        
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("The result student with id {} is {}.", id, resultStudent);
        }
        
        return resultStudent;
    }

    @Override
    public void update(int id, Student student) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to update student {} with id {}.", student, id);
        }
        
        jdbcTemplate.update(environment.getProperty("update.person"),
                student.getFirstName(), student.getLastName(), student.getGender().toString(),
                student.getPhoneNumber(), student.getEmail(),  id, ROLE);
        
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("The student {} with id {} was changed.", student, id);
        }
    }

    @Override
    public void deleteById(int id) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to delete student by id {}.", id);
        }
        
        jdbcTemplate.update(environment.getProperty("delete.person"), id, ROLE);
        
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("The student with id {} was deleted.", id);
        }
    }

    public void setStudentGroup(int groupId, int studentId) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to set group with id {} for student with id {}.", groupId, studentId);
        }
        
        jdbcTemplate.update(environment.getProperty("set.student.group"), groupId, studentId);
        
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("The group with id {} was setted for student with id {}.", groupId, studentId);
        }
    }

    public Group getStudentGroup(int studentId) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to get group for student with id {}.", studentId);
        }
        
        Group resultGroup = jdbcTemplate.queryForStream(environment.getProperty("get.student.group"), new GroupMapper(), studentId)
                .findFirst().get();
        
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("The result group for student with id {} is {}.", studentId, resultGroup);
        }
        
        return resultGroup;
    }
}
