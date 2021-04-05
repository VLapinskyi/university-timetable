package ua.com.foxminded.dao;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import ua.com.foxminded.dao.exceptions.DAOException;
import ua.com.foxminded.domain.Group;
import ua.com.foxminded.domain.Student;
import ua.com.foxminded.mapper.GroupMapper;
import ua.com.foxminded.mapper.StudentMapper;

@Repository
public class StudentDAO implements GenericDAO<Student> {
    private static final Logger LOGGER = LoggerFactory.getLogger(StudentDAO.class);
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
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to insert new student: {}.", student);
        }
        try {
            jdbcTemplate.update(environment.getProperty("create.person"), ROLE, 
                    student.getFirstName(), student.getLastName(), student.getGender().name(),
                    student.getPhoneNumber(), student.getEmail());

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("The student {} was inserted.", student);
            }
        } catch (DataAccessException dataAccessException) {
            LOGGER.error("Can't create student {}.", student, dataAccessException);
            throw new DAOException("Can't create student.", dataAccessException);
        }
    }

    @Override
    public List<Student> findAll() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to find all students.");
        }

        try {
            List<Student> resultStudents = jdbcTemplate.query(environment.getProperty("find.all.people.by.role"), new StudentMapper(), ROLE);

            if (resultStudents.isEmpty()) {
                LOGGER.warn("There are not any students in the result.");
            } else {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("The result is: {}.", resultStudents);
                }
            }
            return resultStudents;
        } catch (DataAccessException dataAccessException) {
            LOGGER.error("Can't find all students.", dataAccessException);
            throw new DAOException("Can't find all students.", dataAccessException);
        }
    }

    @Override
    public Student findById(int id) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to find student by id {}.", id);
        }
        try {
            Student resultStudent = jdbcTemplate.queryForObject(environment.getProperty("find.person.by.id"),
                    new StudentMapper(), id, ROLE);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("The result student with id {} is {}.", id, resultStudent);
            }
            return resultStudent;
        } catch (EmptyResultDataAccessException emptyResultDataAccessException) {
            LOGGER.error("There is no result when find by id {}.", id , emptyResultDataAccessException);
            throw new DAOException("There is no result when find by id.", emptyResultDataAccessException);
        } catch (DataAccessException dataAccessException) {
            LOGGER.error("Can't find student by id {}.", id , dataAccessException);
            throw new DAOException("Can't find student by id.", dataAccessException);
        }
    }

    @Override
    public void update(int id, Student student) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to update student {} with id {}.", student, id);
        }
        try {
            jdbcTemplate.update(environment.getProperty("update.person"),
                    student.getFirstName(), student.getLastName(), student.getGender().toString(),
                    student.getPhoneNumber(), student.getEmail(),  id, ROLE);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("The student {} with id {} was changed.", student, id);
            }
        } catch (DataAccessException dataAccessException) {
            LOGGER.error("Can't update student {} by id {}.", student, id ,dataAccessException);
            throw new DAOException("Can't update student.", dataAccessException);
        }
    }

    @Override
    public void deleteById(int id) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to delete student by id {}.", id);
        }

        try {
            jdbcTemplate.update(environment.getProperty("delete.person"), id, ROLE);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("The student with id {} was deleted.", id);
            }
        } catch (DataAccessException dataAccessException) {
            LOGGER.error("Can't delete student by id {}.", id, dataAccessException);
            throw new DAOException("Can't delete student by id.", dataAccessException);
        }
    }

    public void setStudentGroup(int groupId, int studentId) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to set group with id {} for student with id {}.", groupId, studentId);
        }

        try {
            jdbcTemplate.update(environment.getProperty("set.student.group"), groupId, studentId);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("The group with id {} was setted for student with id {}.", groupId, studentId);
            }
        } catch (DataAccessException dataAccessException) {
            LOGGER.error("Can't set group with id {} for student with id {}.", groupId, studentId, dataAccessException);
            throw new DAOException("Can't set group for student.", dataAccessException);
        }
    }

    public Group getStudentGroup(int studentId) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to get group for student with id {}.", studentId);
        }

        try {
            Group resultGroup = jdbcTemplate.queryForObject(environment.getProperty("get.student.group"), new GroupMapper(), studentId);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("The result group for student with id {} is {}.", studentId, resultGroup);
            }
            return resultGroup;
        } catch (EmptyResultDataAccessException emptyResultDataAccessException) {
            LOGGER.error("There is no result when find group for student by student id {}.", studentId, emptyResultDataAccessException);
            throw new DAOException("There is no result when find group for student by student id ", emptyResultDataAccessException);
        } catch (DataAccessException dataAccessException) {
            LOGGER.error("Can't get group for student with id {}.", studentId, dataAccessException);
            throw new DAOException("Can't get group for student by id.", dataAccessException);
        }
    }
}
