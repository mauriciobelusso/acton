package dev.acton.core.bind;

public interface ResultMapper<S, R> {
    final class Auto implements ResultMapper<Object, Object> {
        @Override
        public Object map(Object src) {
            throw new UnsupportedOperationException();
        }
    }

    R map(S source);
}
