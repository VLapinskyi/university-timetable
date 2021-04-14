package ua.com.foxminded.service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ua.com.foxminded.dao.GroupDAO;
import ua.com.foxminded.dao.exceptions.DAOException;
import ua.com.foxminded.domain.Faculty;
import ua.com.foxminded.domain.Group;
import ua.com.foxminded.service.exceptions.NotValidObjectException;
import ua.com.foxminded.service.exceptions.ServiceException;

@Service
public class GroupService {
    private static final Logger LOGGER = LoggerFactory.getLogger(GroupService.class);
    private GroupDAO groupDAO;
    private FacultyService facultyService; 

    @Autowired
    public GroupService (GroupDAO groupDAO, FacultyService facultyService) {
        this.groupDAO = groupDAO;
        this.facultyService = facultyService;
    }

    public void createGroup(Group group) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to create new group: {}.", group);
        }

        try {
            validateGroup(group);
            validateGroupFaculty(group);
            
            if (group.getId() != 0) {
                NotValidObjectException notValidObjectException = new NotValidObjectException("The group has setted id and it is different from zero.");
                LOGGER.error("The group {} has setted id {} and it is different from zero.", group, group.getId(), notValidObjectException);
                throw notValidObjectException;
            }
            
            groupDAO.create(group);
            Optional<Group> createdGroup = groupDAO.findAll().stream().max(Comparator.comparing(Group :: getId));
            int groupId = 0;
            if(createdGroup.isPresent()) {
                groupId = createdGroup.get().getId();
            }
            groupDAO.setGroupFaculty(group.getFaculty().getId(), groupId);
            
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("The group {} was created.", group);
            }
            
        } catch (NotValidObjectException notValidObjectException) {
            throw new ServiceException("The group is not valid.", notValidObjectException);
        } catch (DAOException daoException) {
            LOGGER.error("Can't create group {}.", group, daoException);
            throw new ServiceException("Can't create group", daoException);
        }
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
    
    void validateGroup (Group group) {
        if (group == null) {
            NotValidObjectException notValidObjectException = new NotValidObjectException("The group is null.");
            LOGGER.error("The group {} is null.", group, notValidObjectException);
            throw notValidObjectException;
        }
        
        if (group.getName() == null || group.getName().trim().length() < 2) {
            NotValidObjectException notValidObjectException = new NotValidObjectException("The group's name is not valid.");
            LOGGER.error("The group's name {} is not valid.", group.getName(), notValidObjectException);
            throw notValidObjectException;
        }
    }
    
    void validateGroupFaculty (Group group) {
        Faculty groupFaculty = group.getFaculty();
        if (groupFaculty.getId() < 1) {
            NotValidObjectException notValidObjectException = new NotValidObjectException("The group has a faculty with incorrect id.");
            LOGGER.error("The group {} has a faculty {} with incorrect id {}.", group, groupFaculty, groupFaculty.getId(), notValidObjectException);
            throw notValidObjectException;
        }
        
        facultyService.validateFaculty(groupFaculty);
    }
}
