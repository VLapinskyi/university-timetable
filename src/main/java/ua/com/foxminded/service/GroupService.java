package ua.com.foxminded.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ua.com.foxminded.domain.Group;
import ua.com.foxminded.repositories.interfaces.GroupRepository;

@Service
public class GroupService {
    private GroupRepository groupRepository;

    @Autowired
    public GroupService(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    public void create(Group group) {
        groupRepository.save(group);
    }

    public List<Group> getAll() {
        return groupRepository.findAll();
    }

    public Group getById(int groupId) {
        return groupRepository.findById(groupId).get();
    }

    public void update(Group updatedGroup) {
        groupRepository.save(updatedGroup);
    }

    public void deleteById(int groupId) {
        groupRepository.deleteById(groupId);
    }
}
