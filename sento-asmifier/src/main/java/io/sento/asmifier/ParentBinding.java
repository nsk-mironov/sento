package io.sento.asmifier;

import android.widget.ImageView;
import io.sento.Binding;
import io.sento.Finder;

public class ParentBinding<T extends Parent> implements Binding<T> {
  @Override
  public <S> void bind(T target, S source, Finder<? super S> finder) {
    target.required = (ImageView) finder.find(456789, source, false);
    target.optional = (ImageView) finder.find(456789, source, true);
    target.background = finder.resources(source).getDrawable(456789);
    target.padding = finder.resources(source).getDimension(456789);
    target.enabled = finder.resources(source).getBoolean(456789);
    target.title = finder.resources(source).getString(456789);
  }

  @Override
  public void unbind(T target) {

  }
}
