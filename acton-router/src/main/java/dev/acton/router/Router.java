package dev.acton.router;

import dev.acton.core.actor.Actor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * The Router is responsible for mapping @Contract types to Actor methods.
 */
public class Router {

    private final Map<Class<?>, RouteBinding> byType = new HashMap<>();
    private final Map<String, RouteBinding> byName = new HashMap<>();

    public void registerActor(Actor actor) {
        for (Method m : actor.getClass().getDeclaredMethods()) {
            if (!m.getName().equals("on") || m.getParameterCount() != 1) continue;

            Class<?> paramType = m.getParameterTypes()[0];
            String name = getName(paramType);

            boolean isCollection = java.util.Collection.class.isAssignableFrom(paramType);
            Class<?> ret = m.getReturnType();

            var binding = new RouteBinding(name, actor, m, isCollection, ret);
            byType.put(paramType, binding);
            byName.put(name, binding);
        }
    }

    private String getName(Class<?> paramType) {
        var contract = paramType.getAnnotation(dev.acton.core.annotation.Contract.class);
        if (contract == null) {
            throw new RouterException("Method on(" + paramType.getName() + ") is missing @Contract");
        }
        String name = contract.value();

        if (byType.containsKey(paramType))
            throw new RouterException("Duplicate handler for contract type " + paramType.getName());
        if (byName.containsKey(name))
            throw new RouterException("Duplicate handler for contract name " + name);
        return name;
    }

    /**
     * Executes the matching Actor.on(contract) for a given contract instance.
     */
    public Object execute(Object contractInstance) {
        var b = byType.get(contractInstance.getClass());
        if (b == null) throw new RouterException("No actor registered for " + contractInstance.getClass().getName());
        try { return b.method().invoke(b.actor(), contractInstance); }
        catch (ReflectiveOperationException e) { throw new RouterException("Failed to execute: " + b.name(), e); }
    }

    public Optional<RouteBinding> lookupByName(String name) { return Optional.ofNullable(byName.get(name)); }
    public Collection<RouteBinding> routes() { return byName.values(); }
}
