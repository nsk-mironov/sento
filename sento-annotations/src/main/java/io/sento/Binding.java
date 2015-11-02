package io.sento;

public interface Binding<T> {
  public <S> void bind(final T target, final S source, final Finder<? super S> finder);

  public void unbind(final T target);
}
