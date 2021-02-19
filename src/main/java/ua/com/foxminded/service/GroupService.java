package ua.com.foxminded.service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ua.com.foxminded.dao.GroupDAO;
import ua.com.foxminded.domain.Group;

@Service
public class GroupService {
    private GroupDAO groupDAO;

    @Autowired
    public GroupService (GroupDAO groupDAO) {
        this.groupDAO = groupDAO;
    }

    public void createGroup(int facultyId, Group group) {
        groupDAO.create(group);
        Optional<Group> createdGroup = groupDAO.findAll().stream().max(Comparator.comparing(Group :: getId));
        int groupId = 0;
        if(createdGroup.isPresent()) {
            groupId = createdGroup.get().getId();
        }
        groupDAO.setGroupFaculty(facultyId, groupId);
    }

    public List<Group> getAllGroups() {
        List<Group> groups = groupDAO.findAll();
        groups.stream().forEach(group -> group.setFaculty(groupDAO.getGroupFaculty(group.getId())));
        return groups;
    }

    public Group getGroupById(int groupId) {
        Group group = groupDAO.findById(groupId);
        group.setFaculty(groupDAO.getGroupFaculty(groupId));
        return group;
    }

    public void updateGroup (int groupId, Group updatedGroup) {
        groupDAO.update(groupId, updatedGroup);
        groupDAO.setGroupFaculty(updatedGroup.getFaculty().getId(), groupId);
    }

    public void deleteGroupById (int groupId) {
        groupDAO.deleteById(groupId);
    }

    public List<Group> getGroupsFromFaculty(int facultyId) {
        List<Group> allGroups = groupDAO.findAll();
        allGroups.stream().forEach(group -> group.setFaculty(groupDAO.getGroupFaculty(group.getId())));
        return allGroups.stream().filter(group -> group.getFaculty().getId() == facultyId)
                .collect(Collectors.toList());
    }
}
