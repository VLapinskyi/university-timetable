package ua.com.foxminded.repositories;

import java.util.List;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import ua.com.foxminded.domain.Group;

@Repository
@Transactional
public class GroupRepository implements GenericRepository<Group> {
    private SessionFactory sessionFactory;

    @Autowired
    public GroupRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void create(Group group) {
        sessionFactory.getCurrentSession().persist(group);
    }

    @Override
    public List<Group> findAll() {
        return sessionFactory.getCurrentSession().createQuery("FROM Group", Group.class).getResultList();
    }

    @Override
    public Group findById(int id) {
        return sessionFactory.getCurrentSession().get(Group.class, id);
    }

    @Override
    public void update(Group group) {
        sessionFactory.getCurrentSession().update(group);
    }

    @Override
    public void delete(Group group) {
        sessionFactory.getCurrentSession().delete(group);
    }
}