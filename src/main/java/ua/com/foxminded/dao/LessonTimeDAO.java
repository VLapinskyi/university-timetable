package ua.com.foxminded.dao;

import java.util.List;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import ua.com.foxminded.domain.LessonTime;

@Repository
@Transactional
public class LessonTimeDAO implements GenericDAO<LessonTime> {
    private SessionFactory sessionFactory;

    @Autowired
    public LessonTimeDAO(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void create(LessonTime lessonTime) {
        sessionFactory.getCurrentSession().persist(lessonTime);
    }

    @Override
    public List<LessonTime> findAll() {
        return sessionFactory.getCurrentSession().createQuery("from LessonTime", LessonTime.class).getResultList();
    }

    @Override
    public LessonTime findById(int id) {
        return sessionFactory.getCurrentSession().get(LessonTime.class, id);

    }

    @Override
    public void update(LessonTime lessonTime) {
        sessionFactory.getCurrentSession().update(lessonTime);
    }

    @Override
    public void delete(LessonTime lessonTime) {
        sessionFactory.getCurrentSession().delete(lessonTime);
    }
}