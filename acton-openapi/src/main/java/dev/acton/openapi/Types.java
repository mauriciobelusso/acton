package dev.acton.openapi;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Optional;

final class Types {
    private Types() {}

    static boolean isOptional(Type t) {
        return erase(t) == Optional.class;
    }

    static boolean isCollection(Type t) {
        Class<?> raw = erase(t);
        return Collection.class.isAssignableFrom(raw);
    }

    static Class<?> erase(Type t) {
        if (t instanceof Class<?> c) return c;
        if (t instanceof ParameterizedType p) return (Class<?>) p.getRawType();
        if (t instanceof GenericArrayType) return Object[].class;
        throw new IllegalArgumentException("Unsupported Type: " + t);
    }

    static Optional<Type> firstTypeArg(Type t) {
        if (t instanceof ParameterizedType p && p.getActualTypeArguments().length >= 1) {
            return Optional.of(p.getActualTypeArguments()[0]);
        }
        return Optional.empty();
    }
}
