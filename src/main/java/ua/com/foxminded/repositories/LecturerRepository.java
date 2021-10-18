package ua.com.foxminded.repositories;

import java.util.List;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import ua.com.foxminded.domain.Lecturer;
import ua.com.foxminded.domain.Role;

@Repository
@Transactional
public class LecturerRepository implements GenericRepository<Lecturer> {
    
    private SessionFactory sessionFactory;

    @Autowired
    public LecturerRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void create(Lecturer lecturer) {
        sessionFactory.getCurrentSession().persist(lecturer);
    }

    @Override
    public List<Lecturer> findAll() {
        return sessionFactory.getCurrentSession().createQuery("from Lecturer where role = '" + Role.LECTURER + "'", Lecturer.class).getResultList();
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
