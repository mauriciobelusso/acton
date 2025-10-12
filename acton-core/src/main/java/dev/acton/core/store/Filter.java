package dev.acton.core.store;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Functional filter for querying entities in a StoreState.
 */
@FunctionalInterface
public interface Filter<T> extends Predicate<T> {

    static <T> Filter<T> and(Filter<T> a, Filter<T> b) {
        return entity -> a.test(entity) && b.test(entity);
    }

    static <T> Filter<T> or(Filter<T> a, Filter<T> b) {
        return entity -> a.test(entity) || b.test(entity);
    }

    static <T> Filter<T> not(Filter<T> f) {
        return entity -> !f.test(entity);
    }

    static <T> Filter<T> eq(String field, Object value) {
        return entity -> {
            try {
                var fieldRef = entity.getClass().getDeclaredField(field);
                fieldRef.setAccessible(true);
                Object val = fieldRef.get(entity);
                return Objects.equals(val, value);
            } catch (ReflectiveOperationException e) {
                return false;
            }
        };
    }
}
