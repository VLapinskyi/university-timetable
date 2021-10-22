package ua.com.foxminded.repositories;

import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import ua.com.foxminded.domain.Lecturer;
import ua.com.foxminded.domain.Role;

@Repository
@Transactional
public class LecturerRepository implements GenericRepository<Lecturer> {
    
    private EntityManager entityManager;

    @Autowired
    public LecturerRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void create(Lecturer lecturer) {
        entityManager.persist(lecturer);
    }

    @Override
    public List<Lecturer> findAll() {
        return entityManager.createQuery("from Lecturer where role = '" + Role.LECTURER + "'", Lecturer.class).getResultList();
    }

    @Override
    public Lecturer findById(int id) {
        return entityManager.find(Lecturer.class, id);
    }

    @Override
    public void update(Lecturer lecturer) {
        entityManager.merge(lecturer);
    }

    @Override
    public void delete(Lecturer lecturer) {
        entityManager.remove(lecturer);
    }
}
