package dev.acton.store.memory;

import dev.acton.core.store.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryStoreState<T> implements StoreState<T> {
  private final Map<Object,T> data = new ConcurrentHashMap<>();
  private final AtomicLong seq = new AtomicLong();
  private final java.util.function.Function<T,Object> idGetter;
  private final java.util.function.BiConsumer<T,Object> idSetter;

  public InMemoryStoreState(java.util.function.Function<T,Object> idGetter,
                            java.util.function.BiConsumer<T,Object> idSetter) {
    this.idGetter = idGetter; this.idSetter = idSetter;
  }

  @Override public T save(T e) {
    Object id = idGetter.apply(e);
    if (id == null) { id = seq.incrementAndGet(); idSetter.accept(e, id); }
    data.put(id, e); return e;
  }
  @Override public Optional<T> find(Object id) { return Optional.ofNullable(data.get(id)); }
  @Override public List<T> query(Filter<T> f, int page, int size, List<Sort> sort) {
    return data.values().stream().filter(f).skip((long)page*size).limit(size).toList();
  }
  @Override public long count(Filter<T> f) { return data.values().stream().filter(f).count(); }
  @Override public void delete(T e) { data.values().removeIf(v -> v==e); }
}
