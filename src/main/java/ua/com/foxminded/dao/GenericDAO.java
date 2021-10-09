package ua.com.foxminded.dao;

import java.util.List;

public interface GenericDAO<T> {
    void create(T t);

    List<T> findAll();

    T findById(int id);

    void update(int id, T t);

    void delete(T t);
}
