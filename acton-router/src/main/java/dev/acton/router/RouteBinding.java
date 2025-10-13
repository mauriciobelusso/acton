package dev.acton.router;

import dev.acton.core.actor.Actor;
import java.lang.reflect.Method;

/**
 * Represents a single route between a contract and its actor method.
 */
public record RouteBinding(
        String name,
        Actor actor,
        Method method,
        boolean paramIsCollection,
        Class<?> returnRawType
) {}
