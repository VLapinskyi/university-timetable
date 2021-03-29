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
import ua.com.foxminded.domain.Faculty;
import ua.com.foxminded.domain.Group;
import ua.com.foxminded.mapper.FacultyMapper;
import ua.com.foxminded.mapper.GroupMapper;

@Repository
public class GroupDAO implements GenericDAO<Group> {
    private static final Logger LOGGER = LoggerFactory.getLogger(GroupDAO.class);
    private JdbcTemplate jdbcTemplate;
    private Environment environment;

    @Autowired
    public GroupDAO (JdbcTemplate jdbcTemplate, Environment environment) {
        this.jdbcTemplate = jdbcTemplate;
        this.environment = environment;
    }    

    @Override
    public void create(Group group) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to insert new group: {}.", group);
        }

        try {
            jdbcTemplate.update(environment.getProperty("create.group"), group.getName());
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("The group {} was inserted.", group);
            }
        } catch (DataAccessException dataAccessException) {
            LOGGER.error("Can't create group: {}.", group, dataAccessException);
            throw new DAOException("Can't create group.", dataAccessException);
        }
    }

    @Override
    public List<Group> findAll() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to find all groups.");
        }
        List<Group> resultGroups = new ArrayList<>();
        try {
            resultGroups = jdbcTemplate.query(environment.getProperty("find.all.groups"),
                    new GroupMapper());

            if (resultGroups.isEmpty()) {
                LOGGER.warn("There are not any groups in the result.");
            } else {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("The result is: {}.", resultGroups);
                }
            }
            return resultGroups;
        } catch (DataAccessException dataAccessException) {
            LOGGER.error("Can't find all groups.", dataAccessException);
            throw new DAOException("Can't find all groups.", dataAccessException);
        }
    }

    @Override
    public Group findById(int id) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to find a group by id {}.", id);
        }
        Group resultGroup = null;
        try {
            resultGroup = jdbcTemplate.queryForObject(environment.getProperty("find.group.by.id"),
                    new GroupMapper(), id);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("The result group with id {} is {}.", id, resultGroup);
            }
            return resultGroup;
        } catch (EmptyResultDataAccessException emptyResultDataAccessException) {
            LOGGER.error("There is no result when find by id {}.", id, emptyResultDataAccessException);
            throw new DAOException("There is no result when find by id.", emptyResultDataAccessException);
        } catch (DataAccessException dataAccessException) {
            LOGGER.error("Can't find group by id {}.", id, dataAccessException);
            throw new DAOException("Can't find faculty by id", dataAccessException);
        }
    }

    @Override
    public void update(int id, Group group) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to update group {} with id {}.", group, id);
        }

        try {
            jdbcTemplate.update(environment.getProperty("update.group"), group.getName(), id);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("The group {} with id {} was changed.", group, id);
            }
        } catch (DataAccessException dataAccessException) {
            LOGGER.error("Can't update group {} by id {}.", group, id, dataAccessException);
            throw new DAOException("Can't update group", dataAccessException);
        }
    }

    @Override
    public void deleteById(int id) {
        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to delete group by id {}.", id);
        }

        try {
            jdbcTemplate.update(environment.getProperty("delete.group"), id);
            if(LOGGER.isDebugEnabled()) {
                LOGGER.debug("The group with id {} was deleted.", id);
            }
        } catch (DataAccessException dataAccessException) {
            LOGGER.error("Can't delete group by id {}.", id, dataAccessException);
            throw new DAOException("Can't delete group by id.", dataAccessException);
        }
    }

    public void setGroupFaculty (int facultyId, int groupId) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to set faculty with id {} for group with id {}.",
                    facultyId, groupId);
        }

        try {
            jdbcTemplate.update(environment.getProperty("set.group.faculty"), facultyId, groupId);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("The faculty with id {} was setted for group with id {}.",
                        facultyId, groupId);
            }
        } catch (DataAccessException dataAccessException) {
            LOGGER.error("Can't set faculty with id {} for group with id {}.", facultyId, groupId, dataAccessException);
            throw new DAOException("Can't set faculty for group.", dataAccessException);
        }
    }

    public Faculty getGroupFaculty (int groupId) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to get faculty for group with id {}.", groupId);
        }
        Faculty resultFaculty = null;
        try {
            resultFaculty = jdbcTemplate.queryForObject(environment.getProperty("get.group.faculty"),
                    new FacultyMapper(), groupId);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("The result faculty for group with id {} is {}.", groupId, resultFaculty);
            }
            return resultFaculty;
        } catch (DataAccessException dataAccessException) {
            LOGGER.error("Can't get faculty for group with id {}.", groupId, dataAccessException);
            throw new DAOException("Can't get faculty for group.", dataAccessException);
        }
    }
}