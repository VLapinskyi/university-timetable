package ua.com.foxminded.repositories;

import java.util.List;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import ua.com.foxminded.domain.Role;
import ua.com.foxminded.domain.Student;

@Repository
@Transactional
public class StudentRepository implements GenericRepository<Student> {
    
    private SessionFactory sessionFactory;
       
    @Autowired
    public StudentRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void create(Student student) {
        sessionFactory.getCurrentSession().persist(student);
    }

    @Override
    public List<Student> findAll() {
        return sessionFactory.getCurrentSession().createQuery("from Student where role = '" + Role.STUDENT + "'", Student.class).getResultList();

    }

    @Override
    public Student findById(int id) {
        return sessionFactory.getCurrentSession().get(Student.class, id);
    }

    @Override
    public void update(Student student) {
        sessionFactory.getCurrentSession().update(student);
    }

    @Override
    public void delete(Student student) {
        sessionFactory.getCurrentSession().delete(student);
    }
}
