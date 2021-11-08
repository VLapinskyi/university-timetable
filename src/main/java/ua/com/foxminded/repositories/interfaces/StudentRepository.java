package ua.com.foxminded.repositories.interfaces;

import org.springframework.data.jpa.repository.JpaRepository;

import ua.com.foxminded.domain.Student;

public interface StudentRepository extends JpaRepository<Student, Integer> {

}
