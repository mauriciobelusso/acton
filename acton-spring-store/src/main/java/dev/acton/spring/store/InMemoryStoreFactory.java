package dev.acton.spring.store;

import dev.acton.core.store.Store;
import dev.acton.core.store.StoreFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory StoreFactory adapted to the simplified Store<T> API.
 */
public final class InMemoryStoreFactory implements StoreFactory {

    @Override
    public <T> Store<T> create(Class<T> type) { return new MemStore<>(); }

    static final class MemStore<T> implements Store<T> {
        private final Map<Object, T> map = new ConcurrentHashMap<>();
        private final AtomicLong seq = new AtomicLong(1);

        @Override
        public T save(T entity) {
            Object id = seq.getAndIncrement();
            map.put(id, entity);
            return entity;
        }

        @Override
        public List<T> save(Iterable<T> entities) {
            List<T> out = new ArrayList<>();
            for (T e : entities) out.add(save(e));
            return out;
        }

        @Override
        public Optional<T> findById(Object id) {
            return Optional.ofNullable(map.get(id));
        }

        @Override
        public void delete(T entity) {
            map.values().removeIf(v -> v == entity);
        }
    }
}
