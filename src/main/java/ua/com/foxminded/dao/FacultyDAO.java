package ua.com.foxminded.dao;

import java.util.List;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import ua.com.foxminded.domain.Faculty;

@Repository
@Transactional
public class FacultyDAO implements GenericDAO<Faculty> {
    private SessionFactory sessionFactory;

    @Autowired
    public FacultyDAO(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void create(Faculty faculty) {
        sessionFactory.getCurrentSession().persist(faculty);
    }

    @Override
    public List<Faculty> findAll() {
        return sessionFactory.getCurrentSession().createQuery("from Faculty", Faculty.class).getResultList();
    }

    @Override
    public Faculty findById(int id) {
        return sessionFactory.getCurrentSession().get(Faculty.class, id);
    }

    @Override
    public void update(int id, Faculty faculty) {
        sessionFactory.getCurrentSession().update(faculty);
    }

    @Override
    public void delete(Faculty faculty) {
        sessionFactory.getCurrentSession().delete(faculty);
    }
}