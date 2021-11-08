package ua.com.foxminded.repositories.interfaces;

import org.springframework.data.jpa.repository.JpaRepository;

import ua.com.foxminded.domain.Faculty;

public interface FacultyRepository extends JpaRepository<Faculty, Integer> {

}
