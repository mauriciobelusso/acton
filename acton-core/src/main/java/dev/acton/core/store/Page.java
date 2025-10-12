package dev.acton.core.store;

import java.util.List;

/**
 * Simple pagination envelope for queries.
 */
public record Page<T>(List<T> items, long total, int page, int size) {
    public static <T> Page<T> of(List<T> items, long total, int page, int size) {
        return new Page<>(items, total, page, size);
    }
}
