package io.sento.asmifier;

import io.sento.Finder;

public class ChildBinding<T extends Child> extends ParentBinding<T> {
  @Override
  public <S> void bind(T target, S source, Finder<? super S> finder) {
    super.bind(target, source, finder);
  }

  @Override
  public void unbind(T target) {
    super.unbind(target);
  }
}
