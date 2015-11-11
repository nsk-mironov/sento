package io.sento.asmifier;

import android.view.View;
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

    final View view1 = finder.find(456789, source, false);

    if (view1 != null) {
      view1.setOnClickListener(new ParentBinding$$1(((Parent) target)));
    }

    final View view2 = finder.find(456789, source, false);

    if (view2 != null) {
      view2.setOnClickListener(new ParentBinding$$1(((Parent) target)));
    }
  }

  @Override
  public void unbind(Object target) {

  }
}
