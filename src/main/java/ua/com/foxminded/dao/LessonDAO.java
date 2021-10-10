package ua.com.foxminded.dao;

import java.time.DayOfWeek;
import java.util.List;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import ua.com.foxminded.domain.Lesson;

@Repository
@Transactional
public class LessonDAO implements GenericDAO<Lesson> {
    private SessionFactory sessionFactory;

    @Autowired
    public LessonDAO(SessionFactory sessoinFactory) {
        this.sessionFactory = sessoinFactory;
    }

    @Override
    public void create(Lesson lesson) {
        sessionFactory.getCurrentSession().persist(lesson);
    }

    @Override
    public List<Lesson> findAll() {
        return sessionFactory.getCurrentSession().createQuery("from Lesson", Lesson.class).getResultList();
    }

    @Override
    public Lesson findById(int id) {
        return sessionFactory.getCurrentSession().get(Lesson.class, id);
    }

    @Override
    public void update(Lesson lesson) {
        sessionFactory.getCurrentSession().update(lesson);
    }

    @Override
    public void delete(Lesson lesson) {
        sessionFactory.getCurrentSession().delete(lesson);
    }

    public List<Lesson> getGroupDayLessons(int groupId, DayOfWeek weekDay) {
        return sessionFactory.getCurrentSession().createQuery("from Lesson where group_id = '" + groupId + 
                "' and week_day = '" + weekDay + "'", Lesson.class).getResultList();
    }

    public List<Lesson> getLecturerDayLessons(int lecturerId, DayOfWeek weekDay) {
        return sessionFactory.getCurrentSession().createQuery("from Lesson where lecturer_id = '" + lecturerId + 
                "' and week_day = '" + weekDay + "'", Lesson.class).getResultList();
    }
}