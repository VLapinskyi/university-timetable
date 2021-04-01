package ua.com.foxminded.dao;

import java.util.ArrayList;
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
import ua.com.foxminded.domain.Lecturer;
import ua.com.foxminded.mapper.LecturerMapper;

@Repository
public class LecturerDAO implements GenericDAO<Lecturer> {
    private static final Logger LOGGER = LoggerFactory.getLogger(LecturerDAO.class);
    private static final String ROLE = "lecturer";
    private JdbcTemplate jdbcTemplate;
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

        try {
            jdbcTemplate.update(environment.getProperty("create.person"), ROLE,
                    lecturer.getFirstName(), lecturer.getLastName(), lecturer.getGender().toString(),
                    lecturer.getPhoneNumber(), lecturer.getEmail());
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("The lecturer {} was inserted.", lecturer);
            }
        } catch (DataAccessException dataAccessException) {
            LOGGER.error("Can't create lecturer: {}.", lecturer, dataAccessException);
            throw new DAOException("Can't create lecturer.", dataAccessException);
        }
    }

    @Override
    public List <Lecturer> findAll() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to find all lecturers.");
        }
        List<Lecturer> resultLecturers = new ArrayList<>();
        try {
            resultLecturers = jdbcTemplate.query(environment.getProperty("find.all.people.by.role"), new LecturerMapper(), ROLE);

            if (resultLecturers.isEmpty()) {
                LOGGER.warn("There are not any lecturers in the result.");
            } else {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("The result is: {}.", resultLecturers);
                }
            }
            return resultLecturers;
        } catch (DataAccessException dataAccessException) {
            LOGGER.error("Can't find all lecturers.", dataAccessException);
            throw new DAOException("Can't find all lecturers.", dataAccessException);
        }
    }

    @Override
    public Lecturer findById(int id) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to find lecturer by id {}.", id);
        }
        Lecturer resultLecturer = null;
        try {
            resultLecturer = jdbcTemplate.queryForObject(environment.getProperty("find.person.by.id"), new LecturerMapper(), id, ROLE);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("The result lecturer with id {} is {}.", id, resultLecturer);
            }

            return resultLecturer;
        } catch (EmptyResultDataAccessException emptyResultDataAccessException) {
            LOGGER.error("There is no result when find by id {}.", id, emptyResultDataAccessException);
            throw new DAOException("Can't find lecturer by id.", emptyResultDataAccessException);
        } catch (DataAccessException dataAccessException) {
            LOGGER.error("Can't find lecturer by id {}.", id, dataAccessException);
            throw new DAOException("Can't find lecturer by id.", dataAccessException);
        }
    }

    @Override
    public void update(int id, Lecturer lecturer) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to update lecturer {} with id {}.", lecturer, id);
        }

        try {
            jdbcTemplate.update(environment.getProperty("update.person"), lecturer.getFirstName(), lecturer.getLastName(),
                    lecturer.getGender().toString(), lecturer.getPhoneNumber(), lecturer.getEmail(), id, ROLE);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("The lecturer {} with id {} was changed.", lecturer, id);
            }
        } catch (DataAccessException dataAccessException) {
            LOGGER.error("Can't update lecturer {} by id {}.", lecturer, id, dataAccessException);
            throw new DAOException("Can't update lecturer.", dataAccessException);
        }
    }

    @Override
    public void deleteById(int id) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to delete lecturer by id {}.", id);
        }

        try {
            jdbcTemplate.update(environment.getProperty("delete.person"), id, ROLE);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("The lecturer with id {} was deleted.", id);
            }
        } catch(DataAccessException dataAccessException) {
            LOGGER.error("Can't delete lecturer by id {}.", id, dataAccessException);
            throw new DAOException("Can't delete lecturer by id.", dataAccessException);
        }
    }
}
