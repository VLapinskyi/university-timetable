package ua.com.foxminded.repositories;

import java.time.DayOfWeek;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import ua.com.foxminded.domain.Lesson;

@Repository
@Transactional
public class LessonRepository implements GenericRepository<Lesson> {
    private EntityManager entityManager;

    @Autowired
    public LessonRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void create(Lesson lesson) {
        entityManager.persist(lesson);
    }

    @Override
    public List<Lesson> findAll() {
        return entityManager.createQuery("FROM Lesson", Lesson.class).getResultList();
    }

    @Override
    public Lesson findById(int id) {
        return entityManager.find(Lesson.class, id);
    }

    @Override
    public void update(Lesson lesson) {
        entityManager.merge(lesson);
    }

    @Override
    public void delete(Lesson lesson) {
        Lesson deletedLesson = entityManager.merge(lesson);
        entityManager.remove(deletedLesson);
    }

    @SuppressWarnings("unchecked")
    public List<Lesson> getGroupDayLessons(int groupId, DayOfWeek weekDay) {
        Query query = entityManager.createQuery("from Lesson where group_id = :groupId" +
                " and week_day = :weekDay", Lesson.class);
        query.setParameter("groupId", groupId);
        query.setParameter("weekDay", weekDay.toString());
        
        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<Lesson> getLecturerDayLessons(int lecturerId, DayOfWeek weekDay) {
        Query query = entityManager.createQuery("from Lesson where lecturer_id = :lecturerId" + 
                " and week_day = :weekDay", Lesson.class);
        query.setParameter("lecturerId", lecturerId);
        query.setParameter("weekDay", weekDay.toString());
        return query.getResultList();
    }
}