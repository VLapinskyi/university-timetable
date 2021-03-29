package ua.com.foxminded.dao;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import ua.com.foxminded.domain.Lecturer;
import ua.com.foxminded.mapper.LecturerMapper;

@Repository
public class LecturerDAO implements GenericDAO<Lecturer> {
    private static final Logger LOGGER = LoggerFactory.getLogger(LecturerDAO.class);
    private static final String ROLE = "lecturer";
    private final JdbcTemplate jdbcTemplate;
    private Environment environment;

    @Autowired
    public LecturerDAO (JdbcTemplate jdbcTemplate, Environment environment) {
        this.jdbcTemplate = jdbcTemplate;
        this.environment = environment;
    }

    @Override
    public void create(Lecturer lecturer) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to insert new lecturer: {}.", lecturer);
        }
        jdbcTemplate.update(environment.getProperty("create.person"), ROLE,
                lecturer.getFirstName(), lecturer.getLastName(), lecturer.getGender().toString(),
                lecturer.getPhoneNumber(), lecturer.getEmail());
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("The lecturer {} was inserted.", lecturer);
        }
    }

    @Override
    public List <Lecturer> findAll() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to find all lecturers.");
        }
        List<Lecturer> resultLecturers = jdbcTemplate.query(environment.getProperty("find.all.people.by.role"), new LecturerMapper(), ROLE);
        
        if (resultLecturers.isEmpty()) {
            LOGGER.warn("There are not any lecturers in the result.");
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("The result is: {}.", resultLecturers);
            }
        }
        
        return resultLecturers;
    }

    @Override
    public Lecturer findById(int id) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to find lecturer by id {}.", id);
        }
        Lecturer resultLecturer = jdbcTemplate.queryForStream(environment.getProperty("find.person.by.id"), new LecturerMapper(), id, ROLE)
                .findAny().orElse(null);
        
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("The result lecturer with id {} is {}.", id, resultLecturer);
        }
        
        return resultLecturer;
    }

    @Override
    public void update(int id, Lecturer lecturer) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to update lecturer {} with id {}.", lecturer, id);
        }
        
        jdbcTemplate.update(environment.getProperty("update.person"), lecturer.getFirstName(), lecturer.getLastName(),
                lecturer.getGender().toString(), lecturer.getPhoneNumber(), lecturer.getEmail(), id, ROLE);
        
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("The lecturer {} with id {} was changed.", lecturer, id);
        }
    }

    @Override
    public void deleteById(int id) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to delete lecturer by id {}.", id);
        }
        
        jdbcTemplate.update(environment.getProperty("delete.person"), id, ROLE);
        
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("The lecturer with id {} was deleted.", id);
        }
    }
}
