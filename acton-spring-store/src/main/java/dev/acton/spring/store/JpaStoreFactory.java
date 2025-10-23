package dev.acton.spring.store;

import dev.acton.core.store.Store;
import dev.acton.core.store.StoreFactory;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Primary;

/**
 * JPA-backed StoreFactory adapted to the simplified Store<T> API.
 */
@Primary
public final class JpaStoreFactory implements StoreFactory {

    private final EntityManager em;

    public JpaStoreFactory(EntityManager em) { this.em = em; }

    public <T> Store<T> create(Class<T> type) {
        return new JpaStore<>(em, type);
    }

    static final class JpaStore<T> implements Store<T> {
        private final EntityManager em;
        private final Class<T> type;

        JpaStore(EntityManager em, Class<T> type) {
            this.em = em; this.type = type;
        }

        @Override
        public T save(T entity) {
            if (em.contains(entity) || idOf(entity) != null) return em.merge(entity);
            em.persist(entity);
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
            return Optional.ofNullable(em.find(type, id));
        }

        @Override
        public void delete(T entity) {
            em.remove(em.contains(entity) ? entity : em.merge(entity));
        }

        private Object idOf(T entity) {
            try {
                return em.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(entity);
            } catch (Exception e) {
                return null;
            }
        }
    }
}
