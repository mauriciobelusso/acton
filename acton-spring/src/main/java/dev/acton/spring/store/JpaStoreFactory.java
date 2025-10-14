package dev.acton.spring.store;

import dev.acton.core.store.Filter;
import dev.acton.core.store.Sort;
import dev.acton.core.store.StoreState;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

public final class JpaStoreFactory {

    private final EntityManager em;

    public JpaStoreFactory(EntityManager em) { this.em = em; }

    public boolean supports(Class<?> type) {
        return type.isAnnotationPresent(Entity.class);
    }

    public <T> StoreState<T> create(Class<T> type) {
        return new JpaStoreState<>(em, type);
    }

    static final class JpaStoreState<T> implements StoreState<T> {
        private final EntityManager em;
        private final Class<T> type;

        JpaStoreState(EntityManager em, Class<T> type) {
            this.em = em; this.type = type;
        }

        @Override public T save(T entity) {
            if (em.contains(entity) || idOf(entity) != null) return em.merge(entity);
            em.persist(entity);
            return entity;
        }

        @Override public Optional<T> find(Object id) {
            return Optional.ofNullable(em.find(type, id));
        }

        @Override public List<T> query(Filter<T> filter, int page, int size, List<Sort> sort) {
            String ql = "select e from " + type.getSimpleName() + " e";
            TypedQuery<T> q = em.createQuery(ql, type);
            if (page >= 0 && size > 0) { q.setFirstResult(page * size); q.setMaxResults(size); }
            return q.getResultList();
        }

        @Override public long count(Filter<T> filter) {
            String ql = "select count(e) from " + type.getSimpleName() + " e";
            return em.createQuery(ql, Long.class).getSingleResult();
        }

        @Override public void delete(T entity) { em.remove(em.contains(entity) ? entity : em.merge(entity)); }

        private Object idOf(T entity) {
            return null;
        }
    }
}
