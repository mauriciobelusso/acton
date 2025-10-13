package dev.acton.spring.util;

import dev.acton.core.annotation.Contract;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Optional;

public class MethodUtils {

    private MethodUtils() {
    }

    public static Optional<Parameter> getPayload(Method method) {
        if (method.getParameterCount() == 0) return Optional.empty();

        var parameter = method.getParameters()[0];

        if (parameter == null || !parameter.getType().isAnnotationPresent(Contract.class)) return Optional.empty();

        return Optional.of(parameter);
    }

    public static Contract getContract(Parameter payload) {
        var contract = payload.getType().getAnnotation(Contract.class);
        if (contract == null) throw new IllegalStateException("Parameter " + payload + " must be annotated with @Contract");
        return contract;
    }
}
