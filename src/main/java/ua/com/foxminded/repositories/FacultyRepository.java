package ua.com.foxminded.repositories;

import java.util.List;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import ua.com.foxminded.domain.Faculty;

@Repository
@Transactional
public class FacultyRepository implements GenericRepository<Faculty> {
    
   private EntityManager entityManager;
    
    @Autowired
    public FacultyRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void create(Faculty faculty) {
        entityManager.persist(faculty);
    }

    @Override
    public List<Faculty> findAll() {
        return entityManager.createQuery("FROM Faculty", Faculty.class).getResultList();
    }

    @Override
    public Faculty findById(int id) {
        return entityManager.find(Faculty.class, id);
    }

    @Override
    public void update(Faculty faculty) {
        entityManager.merge(faculty);;
    }

    @Override
    public void delete(Faculty faculty) {
        entityManager.remove(faculty);
    }
}