package dev.acton.spring.store;

import dev.acton.core.store.StoreState;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;

public final class ActOnStoreProvider {
    private final ApplicationContext ctx;
    private final ObjectProvider<JpaStoreFactory> jpaFactory;
    private final ObjectProvider<InMemoryStoreFactory> memFactory;
    private final Map<Class<?>, StoreState<?>> cache = new ConcurrentHashMap<>();

    public ActOnStoreProvider(ApplicationContext ctx,
                              ObjectProvider<JpaStoreFactory> jpaFactory,
                              ObjectProvider<InMemoryStoreFactory> memFactory) {
        this.ctx = ctx;
        this.jpaFactory = jpaFactory;
        this.memFactory = memFactory;
    }

    @SuppressWarnings("unchecked")
    public <T> StoreState<T> get(Class<T> type) {
        return (StoreState<T>) cache.computeIfAbsent(type, this::create);
    }

    private StoreState<?> create(Class<?> type) {
        Map<String, StoreState> beans = (Map) ctx.getBeansOfType(StoreState.class);
        for (StoreState<?> s : beans.values()) {
            var gs = s.getClass().getGenericSuperclass();
            if (gs instanceof java.lang.reflect.ParameterizedType pt) {
                var arg = pt.getActualTypeArguments()[0];
                if (arg instanceof Class<?> c && c.equals(type)) return s;
            }
        }
        var jpa = jpaFactory.getIfAvailable();
        if (jpa != null && jpa.supports(type)) return jpa.create(type);
        var mem = memFactory.getIfAvailable();
        if (mem != null) return mem.create(type);

        throw new IllegalStateException("No StoreState available for type: " + type.getName());
    }
}
