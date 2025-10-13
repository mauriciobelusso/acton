package dev.acton.router;

import dev.acton.core.annotation.Contract;
import dev.acton.core.actor.Actor;
import java.lang.reflect.Method;
import java.util.*;

/**
 * The Router is responsible for mapping @Contract types to Actor methods.
 */
public class Router {

    private final Map<Class<?>, RouteBinding> bindings = new HashMap<>();

    public void registerActor(Actor actor) {
        for (Method m : actor.getClass().getDeclaredMethods()) {
            if (m.getName().equals("on") && m.getParameterCount() == 1) {
                var paramType = m.getParameterTypes()[0];
                var contract = paramType.getAnnotation(Contract.class);
                if (contract == null) continue;
                bindings.put(paramType, new RouteBinding(contract.value(), actor, m));
            }
        }
    }

    /**
     * Executes the matching Actor.on(contract) for a given contract instance.
     */
    @SuppressWarnings("unchecked")
    public <T> Object execute(T contractInstance) {
        var binding = bindings.get(contractInstance.getClass());
        if (binding == null) {
            throw new RouterException("No actor registered for contract: " + contractInstance.getClass().getName());
        }
        try {
            return binding.method().invoke(binding.actor(), contractInstance);
        } catch (ReflectiveOperationException e) {
            throw new RouterException("Failed to execute contract: " + binding.name(), e);
        }
    }

    public Collection<RouteBinding> routes() {
        return bindings.values();
    }
}
