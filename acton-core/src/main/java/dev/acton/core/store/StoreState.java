package dev.acton.core.store;

import java.util.*;
import java.util.function.Predicate;

/**
 * Generic persistence abstraction for ActOn.
 * Eliminates the need for repositories and provides a minimal query API.
 */
public interface StoreState<T> {

    T save(T entity);

    Optional<T> find(Object id);

    List<T> query(Filter<T> filter, int page, int size, List<Sort> sort);

    default void delete(T entity) {}

    default long count(Filter<T> filter) { return 0L; }

    /**
     * Default "true" filter that matches all entities.
     */
    static <T> Filter<T> all() {
        return entity -> true;
    }
}
