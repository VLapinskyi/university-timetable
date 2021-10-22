package ua.com.foxminded.repositories;

import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import ua.com.foxminded.domain.LessonTime;

@Repository
@Transactional
public class LessonTimeRepository implements GenericRepository<LessonTime> {
    private EntityManager entityManager;

    @Autowired
    public LessonTimeRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void create(LessonTime lessonTime) {
        entityManager.persist(lessonTime);
    }

    @Override
    public List<LessonTime> findAll() {
        return entityManager.createQuery("from LessonTime", LessonTime.class).getResultList();
    }

    @Override
    public LessonTime findById(int id) {
        return entityManager.find(LessonTime.class, id);

    }

    @Override
    public void update(LessonTime lessonTime) {
        entityManager.merge(lessonTime);
    }

    @Override
    public void delete(LessonTime lessonTime) {
        entityManager.remove(lessonTime);
    }
}