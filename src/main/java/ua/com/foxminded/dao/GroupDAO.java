package ua.com.foxminded.dao;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import ua.com.foxminded.domain.Faculty;
import ua.com.foxminded.domain.Group;
import ua.com.foxminded.mapper.FacultyMapper;
import ua.com.foxminded.mapper.GroupMapper;

@Repository
public class GroupDAO implements GenericDAO<Group> {
    private static final Logger LOGGER = LoggerFactory.getLogger(GroupDAO.class);
    private final JdbcTemplate jdbcTemplate;
    private Environment environment;

    @Autowired
    public GroupDAO (JdbcTemplate jdbcTemplate, Environment environment) {
        this.jdbcTemplate = jdbcTemplate;
        this.environment = environment;
    }    

    @Override
    public void create(Group group) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to insert new group: \"{}\"", group);
        }
        if (group.getId() != 0) {
            LOGGER.warn("The group has already setted id: \"{}\". The setted id will be ignored",
                    group.getId());
        }
        jdbcTemplate.update(environment.getProperty("create.group"), group.getName());
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("The group \"{}\" was inserted.", group);
        }
    }

    @Override
    public List<Group> findAll() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to find all groups");
        }
        
        List<Group> resultGroups = jdbcTemplate.query(environment.getProperty("find.all.groups"),
                new GroupMapper());
        
        if (resultGroups.isEmpty()) {
            LOGGER.warn("There are not any groups in the result");
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("The result is: \"{}\"", resultGroups);
            }
        }
        return resultGroups;
    }

    @Override
    public Group findById(int id) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to find group by id \"{}\"", id);
        }
        Group resultGroup = jdbcTemplate.queryForStream(environment.getProperty("find.group.by.id"),
                new GroupMapper(), id).findAny().orElse(null);
        
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("The result group is \"{}\"", resultGroup);
        }
        return resultGroup;
    }

    @Override
    public void update(int id, Group group) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to update group with id \"{}\"", id);
        }
        
        if (group.getId() != 0 && group.getId() != id) {
            LOGGER.warn("The group has setted field id \"{}\" and it isn't equal to argument id \"{}\", The field id \"{}\" will be ignored.",
                    group.getId(), id, group.getId());
        }
        
        if(group.getFaculty() != null) {
            LOGGER.info("The group has setted faculty. The updated group with id \"{}\" won't get this faculty.", id);
        }
        
        jdbcTemplate.update(environment.getProperty("update.group"), group.getName(), id);
        
        if (group.getName() == null) {
            LOGGER.warn("The group name is null, so the old name was remained");
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("The group with id \"{}\" was changed", id);
            }
        }
    }

    @Override
    public void deleteById(int id) {
        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to delete group  by id \"{}\"", id);
        }
        
        jdbcTemplate.update(environment.getProperty("delete.group"), id);
        
        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("The group with id \"{}\" was deleted", id);
        }
    }

    public void setGroupFaculty (int facultyId, int groupId) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to set faculty with id \"{}\" for group with id \"{}\"",
                    facultyId, groupId);
        }
        jdbcTemplate.update(environment.getProperty("set.group.faculty"), facultyId, groupId);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("The faculty with id \"{}\" was setted for group with id \"{}\"",
                    facultyId, groupId);
        }
    }

    public Faculty getGroupFaculty (int groupId) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to get faculty for group with id \"{}\"");
        }
        
        Faculty resultFaculty = jdbcTemplate.queryForStream(environment.getProperty("get.group.faculty"),
                new FacultyMapper(), groupId).findAny().orElse(null);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("The result is \"{}\"", resultFaculty);
        }
        return resultFaculty;
    }
}