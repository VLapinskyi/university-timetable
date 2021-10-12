package ua.com.foxminded.repositories;

import java.util.List;

public interface GenericRepository<T> {
    void create(T t);

    List<T> findAll();

    T findById(int id);

    void update(T t);

    void delete(T t);
}
