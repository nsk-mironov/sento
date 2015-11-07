package io.sento.asmifier;

import android.widget.ImageView;
import io.sento.Binding;
import io.sento.Finder;

public class ParentBinding implements Binding<Object> {
  @Override
  public <S> void bind(Object target, S source, Finder<? super S> finder) {
    ((Parent) target).required = (ImageView) finder.find(456789, source, false);
    ((Parent) target).optional = (ImageView) finder.find(456789, source, true);
    ((Parent) target).background = finder.resources(source).getDrawable(456789);
    ((Parent) target).padding = finder.resources(source).getDimension(456789);
    ((Parent) target).enabled = finder.resources(source).getBoolean(456789);
    ((Parent) target).title = finder.resources(source).getString(456789);

    finder.find(456789, source, false).setOnClickListener(new ParentBinding$$1(((Parent) target)));
    finder.find(456789, source, false).setOnClickListener(new ParentBinding$$1(((Parent) target)));
    finder.find(456789, source, false).setOnClickListener(new ParentBinding$$1(((Parent) target)));
    finder.find(456789, source, false).setOnClickListener(new ParentBinding$$1(((Parent) target)));
    finder.find(456789, source, false).setOnClickListener(new ParentBinding$$1(((Parent) target)));
  }

  @Override
  public void unbind(Object target) {

  }
}
