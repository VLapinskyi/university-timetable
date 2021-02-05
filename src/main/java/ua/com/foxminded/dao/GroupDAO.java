package ua.com.foxminded.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import ua.com.foxminded.domain.Group;

@Repository
public class GroupDAO implements GenericDAO<Group> {
    private final JdbcTemplate jdbcTemplate;
    
    @Autowired
    private Environment environment;
    
    @Autowired
    public GroupDAO (JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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
        return jdbcTemplate.queryForStream(environment.getProperty("find.group.by.id"), new GroupMapper(), id)
                .findAny().orElse(null);
    }

    @Override
    public void update(int id, Group group) {
        jdbcTemplate.update(environment.getProperty("update.group"), group.getName(), group.getFaculty().getId(), id);
    }

    @Override
    public void deleteById(int id) {
        jdbcTemplate.update(environment.getProperty("delete.group"), id);       
    }
}
