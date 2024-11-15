package dao;

import records.GeekUserRole;

import java.util.List;
import java.util.Optional;

public interface DAO<T> {

    Optional<T> get(int id) throws DataAccessException;
    List<T> getAll() throws DataAccessException;
    int save(T t) throws DataAccessException;
    void update(T t, String[] params) throws DataAccessException;
    void delete(T t) throws DataAccessException;
}
