package dev.acton.router.util;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

/**
 * Utility reflection helpers used internally by router and stores.
 */
public final class ReflectionUtils {

    private ReflectionUtils() {}

    public static Optional<Method> findMethod(Class<?> type, String name, Class<?>... params) {
        return Arrays.stream(type.getDeclaredMethods())
                     .filter(m -> m.getName().equals(name)
                               && Arrays.equals(m.getParameterTypes(), params))
                     .findFirst();
    }
}
