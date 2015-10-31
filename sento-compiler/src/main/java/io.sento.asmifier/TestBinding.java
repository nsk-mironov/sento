package io.sento.asmifier;

import io.sento.Binding;
import io.sento.Finder;

public class TestBinding implements Binding<Test> {
  @Override
  public <S> void bind(Test target, S source, Finder<? super S> finder) {
    target.background = finder.resources(source).getDrawable(456789);
    target.padding = finder.resources(source).getDimension(456789);
    target.enabled = finder.resources(source).getBoolean(456789);
    target.title = finder.resources(source).getString(456789);
  }

  @Override
  public void unbind(Test target) {

  }
}
