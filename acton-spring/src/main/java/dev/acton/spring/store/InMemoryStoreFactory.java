package dev.acton.spring.store;

import dev.acton.core.store.Filter;
import dev.acton.core.store.Sort;
import dev.acton.core.store.StoreState;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public final class InMemoryStoreFactory {

    public <T> StoreState<T> create(Class<T> type) { return new MemStoreState<>(); }

    static final class MemStoreState<T> implements StoreState<T> {
        private final Map<Object, T> map = new ConcurrentHashMap<>();
        private final AtomicLong seq = new AtomicLong(1);

        @Override public T save(T entity) {
            Object id = seq.getAndIncrement();
            map.put(id, entity);
            return entity;
        }

        @Override public Optional<T> find(Object id) { return Optional.ofNullable(map.get(id)); }

        @Override public List<T> query(Filter<T> filter, int page, int size, List<Sort> sort) {
            List<T> all = new ArrayList<>(map.values());
            if (filter != null) all.removeIf(t -> !filter.test(t));
            if (page >= 0 && size > 0) {
                int from = Math.min(page * size, all.size());
                int to = Math.min(from + size, all.size());
                return all.subList(from, to);
            }
            return all;
        }

        @Override public long count(Filter<T> filter) {
            return filter == null ? map.size() : map.values().stream().filter(filter).count();
        }

        @Override public void delete(T entity) { map.values().removeIf(v -> v == entity); }
    }
}
