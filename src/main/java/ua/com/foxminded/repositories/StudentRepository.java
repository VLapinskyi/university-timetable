package ua.com.foxminded.repositories;

import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import ua.com.foxminded.domain.Role;
import ua.com.foxminded.domain.Student;

@Repository
@Transactional
public class StudentRepository implements GenericRepository<Student> {
    
    private EntityManager entityManager;
       
    @Autowired
    public StudentRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void create(Student student) {
        entityManager.persist(student);
    }

    @Override
    public List<Student> findAll() {
        return entityManager.createQuery("from Student where role = '" + Role.STUDENT + "'", Student.class).getResultList();

    }

    @Override
    public Student findById(int id) {
        return entityManager.find(Student.class, id);
    }

    @Override
    public void update(Student student) {
        entityManager.merge(student);
    }

    @Override
    public void delete(Student student) {
        Student deletedStudent = entityManager.merge(student);
        entityManager.remove(deletedStudent);
    }
}
