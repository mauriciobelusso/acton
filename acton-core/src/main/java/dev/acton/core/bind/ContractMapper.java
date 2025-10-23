package dev.acton.core.bind;

public interface ContractMapper<Q, D> {
    final class Auto implements ContractMapper<Object, Object> {
        @Override
        public Object toDomain(Object q) {
            throw new UnsupportedOperationException();
        }
    }

    D toDomain(Q contract);
}
