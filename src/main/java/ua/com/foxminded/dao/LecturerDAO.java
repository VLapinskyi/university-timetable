package ua.com.foxminded.dao;

import java.util.List;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import ua.com.foxminded.domain.Lecturer;
import ua.com.foxminded.domain.Role;

@Repository
@Transactional
public class LecturerDAO implements GenericDAO<Lecturer> {
    private static final Role ROLE = Role.LECTURER;
    
    private SessionFactory sessionFactory;

    @Autowired
    public LecturerDAO(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void create(Lecturer lecturer) {
        sessionFactory.getCurrentSession().persist(lecturer);
    }

    @Override
    public List<Lecturer> findAll() {
        return sessionFactory.getCurrentSession().createQuery("from Lecturer where role = '" + ROLE + "'", Lecturer.class).getResultList();
    }

    @Override
    public Lecturer findById(int id) {
        return sessionFactory.getCurrentSession().get(Lecturer.class, id);
    }

    @Override
    public void update(Lecturer lecturer) {
        sessionFactory.getCurrentSession().update(lecturer);
    }

    @Override
    public void delete(Lecturer lecturer) {
        sessionFactory.getCurrentSession().delete(lecturer);
    }
}
