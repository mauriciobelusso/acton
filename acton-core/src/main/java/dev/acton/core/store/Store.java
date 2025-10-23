package dev.acton.core.store;

import java.util.List;
import java.util.Optional;

public interface Store<T> {

    T save(T entity);

    List<T> save(Iterable<T> entity);

    Optional<T> findById(Object id);

    void delete(T entity);
}
