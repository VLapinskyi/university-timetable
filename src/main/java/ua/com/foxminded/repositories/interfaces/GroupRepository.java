package ua.com.foxminded.repositories.interfaces;

import org.springframework.data.jpa.repository.JpaRepository;

import ua.com.foxminded.domain.Group;

public interface GroupRepository extends JpaRepository<Group, Integer> {

}
