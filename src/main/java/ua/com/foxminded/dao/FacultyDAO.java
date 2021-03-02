package ua.com.foxminded.dao;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import ua.com.foxminded.domain.Faculty;
import ua.com.foxminded.mapper.FacultyMapper;

@Repository
public class FacultyDAO implements GenericDAO<Faculty> {
    private static final Logger LOGGER = LoggerFactory.getLogger(FacultyDAO.class);
    private final JdbcTemplate jdbcTemplate;
    private Environment environment;

    @Autowired
    public FacultyDAO(JdbcTemplate jdbcTemplate, Environment environment) {
        this.jdbcTemplate = jdbcTemplate;
        this.environment = environment;
    }

    @Override
    public void create(Faculty faculty) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to insert new faculty: \"{}\".", faculty);
        }

        jdbcTemplate.update(environment.getProperty("create.faculty"), faculty.getName());

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("The faculty \"{}\" was inserted.", faculty);
        }
    }

    @Override
    public List<Faculty> findAll() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to find all faculties.");
        }

        List<Faculty> resultFaculties = jdbcTemplate.query(environment.getProperty("find.all.faculties"),
                new FacultyMapper());

        if (resultFaculties.isEmpty()) {
            LOGGER.warn("There are not any faculties in the result.");
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("The result is: \"{}\".", resultFaculties);
            }
        }
        return resultFaculties;
    }

    @Override
    public Faculty findById(int id) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to find a faculty by id \"{}\".", id);
        }

        Faculty resultFaculty = jdbcTemplate.queryForStream(environment.getProperty("find.faculty.by.id"),
                new FacultyMapper(), id).findAny().orElse(null);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("The result faculty is \"{}\".", resultFaculty);
        }
        return resultFaculty;
    }

    @Override
    public void update(int id, Faculty faculty) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to update faculty with id \"{}\".", id);
        }

        jdbcTemplate.update(environment.getProperty("update.faculty"), faculty.getName(), id);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("The faculty with id \"{}\" was changed.", id);
        }
    }

    @Override
    public void deleteById(int id) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to delete faculty by id \"{}\".", id);
        }

        jdbcTemplate.update(environment.getProperty("delete.faculty"), id);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("The faculty with id \"{}\" was deleted.", id);
        }
    }
}