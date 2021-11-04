package ua.com.foxminded.repositories.interfaces;

import org.springframework.data.jpa.repository.JpaRepository;

import ua.com.foxminded.domain.Lecturer;

public interface LecturerRepository extends JpaRepository<Lecturer, Integer> {

}
