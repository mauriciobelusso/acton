package dev.acton.spring.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import org.springframework.http.ResponseEntity;

public final class TypeUtils {

    private TypeUtils() {}

    /**
     * Desembrulha tipos genéricos como ResponseEntity<T>, Optional<T>, CompletionStage<T>.
     * Retorna o tipo real interno (T), ou o próprio tipo se não estiver embrulhado.
     */
    public static Type unwrap(Type type) {
        if (type instanceof ParameterizedType pt) {
            Class<?> raw = (Class<?>) pt.getRawType();

            // unwrap para ResponseEntity<T>
            if (raw == ResponseEntity.class)
                return pt.getActualTypeArguments()[0];

            // unwrap para Optional<T>
            if (raw == Optional.class)
                return pt.getActualTypeArguments()[0];

            // unwrap para CompletableFuture<T> / CompletionStage<T>
            if (CompletionStage.class.isAssignableFrom(raw))
                return pt.getActualTypeArguments()[0];
        }
        return type;
    }

    /**
     * Desembrulha valores em runtime: ResponseEntity, Optional, CompletionStage.
     */
    public static Object unwrapValue(Object result) {
        return switch (result) {
            case null -> null;
            case ResponseEntity<?> re -> re.getBody();
            case Optional<?> opt -> opt.orElse(null);
            case CompletionStage<?> cs -> cs.toCompletableFuture().join();
            default -> result;
        };

    }
}
