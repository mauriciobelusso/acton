package dev.acton.spring.bind;

import dev.acton.core.bind.ActOnBinder;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class DefaultActOnBinder implements ActOnBinder {
    private final Map<Class<?>, Handler<?>> handlers = new ConcurrentHashMap<>();

    @Override
    public <Q> void handle(Class<Q> contractType, Handler<Q> handler) {
        if (handlers.putIfAbsent(contractType, handler) != null) {
            throw new IllegalStateException("Handler already registered for: " + contractType.getName());
        }
    }

    @SuppressWarnings("unchecked")
    public <Q> Handler<Q> getHandler(Class<Q> contractType) {
        var h = handlers.get(contractType);
        if (h == null) throw new IllegalStateException("No handler for: " + contractType.getName());
        return (Handler<Q>) h;
    }

    public Map<Class<?>, Handler<?>> snapshot() { return Map.copyOf(handlers); }
}
