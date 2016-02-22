package io.mironov.sento;

public interface Binding {
  public void bind(final Object target, final Object source, final Finder finder);

  public void unbind(final Object target);
}
