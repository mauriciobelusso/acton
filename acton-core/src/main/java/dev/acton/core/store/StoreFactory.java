package dev.acton.core.store;

public interface StoreFactory {
  <T> Store<T> create(Class<T> type);
}
