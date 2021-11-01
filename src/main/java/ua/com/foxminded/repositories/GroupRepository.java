package ua.com.foxminded.repositories;

import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import ua.com.foxminded.domain.Group;

@Repository
@Transactional
public class GroupRepository implements GenericRepository<Group> {
    private EntityManager entityManager;

    @Autowired
    public GroupRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void create(Group group) {
        entityManager.persist(group);
    }

    @Override
    public List<Group> findAll() {
        return entityManager.createQuery("FROM Group", Group.class).getResultList();
    }

    @Override
    public Group findById(int id) {
        return entityManager.find(Group.class, id);
    }

    @Override
    public void update(Group group) {
        entityManager.merge(group);
    }

    @Override
    public void delete(Group group) {
        Group deletedGroup = entityManager.merge(group);
        entityManager.remove(deletedGroup);
    }
}