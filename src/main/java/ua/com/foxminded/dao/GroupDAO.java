package ua.com.foxminded.dao;

import java.util.List;

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
    private JdbcTemplate jdbcTemplate;
    private Environment environment;

    @Autowired
    public GroupDAO(JdbcTemplate jdbcTemplate, Environment environment) {
        this.jdbcTemplate = jdbcTemplate;
        this.environment = environment;
    }

    @Override
    public void create(Group group) {
        jdbcTemplate.update(environment.getProperty("create.group"), group.getName());
    }

    @Override
    public List<Group> findAll() {
        return jdbcTemplate.query(environment.getProperty("find.all.groups"), new GroupMapper());
    }

    @Override
    public Group findById(int id) {
        return jdbcTemplate.queryForObject(environment.getProperty("find.group.by.id"), new GroupMapper(), id);
    }

    @Override
    public void update(int id, Group group) {
        jdbcTemplate.update(environment.getProperty("update.group"), group.getName(), id);
    }

    @Override
    public void deleteById(int id) {
        jdbcTemplate.update(environment.getProperty("delete.group"), id);
    }

    public void setGroupFaculty(int facultyId, int groupId) {
        jdbcTemplate.update(environment.getProperty("set.group.faculty"), facultyId, groupId);
    }

    public Faculty getGroupFaculty(int groupId) {
        return jdbcTemplate.queryForObject(environment.getProperty("get.group.faculty"), new FacultyMapper(), groupId);
    }
}