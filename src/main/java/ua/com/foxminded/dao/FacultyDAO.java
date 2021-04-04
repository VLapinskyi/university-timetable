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
import ua.com.foxminded.domain.Faculty;
import ua.com.foxminded.mapper.FacultyMapper;

@Repository
public class FacultyDAO implements GenericDAO<Faculty> {
    private static final Logger LOGGER = LoggerFactory.getLogger(FacultyDAO.class);
    private JdbcTemplate jdbcTemplate;
    private Environment environment;

    @Autowired
    public FacultyDAO(JdbcTemplate jdbcTemplate, Environment environment) {
        this.jdbcTemplate = jdbcTemplate;
        this.environment = environment;
    }

    @Override
    public void create(Faculty faculty) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to insert new faculty: {}.", faculty);
        }

        try {
            jdbcTemplate.update(environment.getProperty("create.faculty"), faculty.getName());
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("The faculty {} was inserted.", faculty);
            }
        } catch (DataAccessException dataAccessException) {            
            LOGGER.error("Can't create faculty: {}.", faculty, dataAccessException);
            throw new DAOException("Can't create faculty.", dataAccessException);
        }
    }

    @Override
    public List<Faculty> findAll() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to find all faculties.");
        }
        try {
            List<Faculty> resultFaculties = jdbcTemplate.query(environment.getProperty("find.all.faculties"),
                    new FacultyMapper());
            if (resultFaculties.isEmpty()) {
                LOGGER.warn("There are not any faculties in the result.");
            } else {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("The result is: {}.", resultFaculties);
                }
            }
            return resultFaculties;

        } catch (DataAccessException dataAccessException) {
            LOGGER.error("Can't find all faculties.", dataAccessException);
            throw new DAOException("Can't find all faculties", dataAccessException);
        }
    }

    @Override
    public Faculty findById(int id) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to find a faculty by id {}.", id);
        }
        
        try {
            Faculty resultFaculty = jdbcTemplate.queryForObject(environment.getProperty("find.faculty.by.id"),
                    new FacultyMapper(), id);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("The result faculty with id {} is {}.", id, resultFaculty);
            }
            return resultFaculty;
        } catch (EmptyResultDataAccessException emptyResultDataAccessException) {
            LOGGER.error("There is no result when find by id {}.", id, emptyResultDataAccessException);
            throw new DAOException("There is no result when find by id.", emptyResultDataAccessException);
        } catch (DataAccessException dataAccessException) {
            LOGGER.error("Can't find faculty by id {}.", id, dataAccessException);
            throw new DAOException("Can't find faculty by id", dataAccessException);
        }     
    }

    @Override
    public void update(int id, Faculty faculty) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to update faculty {} with id {}.", faculty, id);
        }
        try {
            jdbcTemplate.update(environment.getProperty("update.faculty"), faculty.getName(), id);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("The faculty {} with id {} was changed.", faculty, id);
            }
        } catch (DataAccessException dataAccessException) {
            LOGGER.error("Can't update faculty {} by id {}.", faculty, id, dataAccessException);
            throw new DAOException("Can't update faculty", dataAccessException);
        }


    }

    @Override
    public void deleteById(int id) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to delete faculty by id {}.", id);
        }
        try {
            jdbcTemplate.update(environment.getProperty("delete.faculty"), id);
        } catch (DataAccessException dataAccessException) {
            LOGGER.error("Can't delete faculty by id {}.", id, dataAccessException);
            throw new DAOException("Can't delete faculty by id.", dataAccessException);
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("The faculty with id {} was deleted.", id);
        }
    }
}