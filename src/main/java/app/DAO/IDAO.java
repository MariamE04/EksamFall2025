package app.DAO;

import java.util.List;

public interface IDAO<T,I> {
    T create(T t);
    T getById(I i);
    T update(T t);
    List<T> getAll();
    boolean delete(I i);
    }
