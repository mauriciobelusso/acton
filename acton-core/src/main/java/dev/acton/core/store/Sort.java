package dev.acton.core.store;

/**
 * Sorting descriptor for queries in StoreState.
 */
public record Sort(String field, Direction direction) {
    public enum Direction { ASC, DESC }

    public static Sort asc(String field) { return new Sort(field, Direction.ASC); }
    public static Sort desc(String field) { return new Sort(field, Direction.DESC); }
}
