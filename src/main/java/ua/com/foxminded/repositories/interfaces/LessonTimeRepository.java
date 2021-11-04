package ua.com.foxminded.repositories.interfaces;

import org.springframework.data.jpa.repository.JpaRepository;

import ua.com.foxminded.domain.LessonTime;

public interface LessonTimeRepository extends JpaRepository<LessonTime, Integer> {

}
