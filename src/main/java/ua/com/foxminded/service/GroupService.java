package ua.com.foxminded.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ua.com.foxminded.dao.GroupDAO;
import ua.com.foxminded.domain.Group;

@Service
public class GroupService {
    private GroupDAO groupDAO;

    @Autowired
    public GroupService(GroupDAO groupDAO) {
        this.groupDAO = groupDAO;
    }

    public void create(Group group) {
        groupDAO.create(group);
    }

    public List<Group> getAll() {
        return groupDAO.findAll();
    }

    public Group getById(int groupId) {
        return groupDAO.findById(groupId);
    }

    public void update(Group updatedGroup) {
        groupDAO.update(updatedGroup);
    }

    public void deleteById(int groupId) {
        Group group = groupDAO.findById(groupId);
        groupDAO.delete(group);
    }
}
