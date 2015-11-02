package io.sento.asmifier;

import io.sento.Binding;
import io.sento.Finder;

public class GrandChildBinding<T extends GrandChild> extends ChildBinding<T> implements Binding<T> {
  @Override
  public <S> void bind(T target, S source, Finder<? super S> finder) {
    super.bind(target, source, finder);
    target.magic = finder.resources(source).getBoolean(456789);
  }

  @Override
  public void unbind(T target) {
    super.unbind(target);
    target.magic = false;
  }
}
