package io.sento;

public interface Binding {
  public <S> void bind(final Object target, final S source, final Finder<? super S> finder);

  public void unbind(final Object target);
}
