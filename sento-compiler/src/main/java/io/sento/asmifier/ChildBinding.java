package io.sento.asmifier;

import io.sento.Binding;
import io.sento.Finder;

public class ChildBinding<T extends Child> extends ParentBinding<T> implements Binding<T> {
  @Override
  public <S> void bind(T target, S source, Finder<? super S> finder) {
    super.bind(target, source, finder);

    target.sizes = finder.resources(source).getIntArray(456789);
    target.labels = finder.resources(source).getStringArray(456789);
  }

  @Override
  public void unbind(T target) {
    super.unbind(target);

    target.sizes = null;
    target.labels = null;
  }
}
