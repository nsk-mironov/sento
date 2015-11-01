package io.sento;

import android.content.res.Resources;
import android.view.View;

public interface Finder<T> {
  public <V extends View> V find(final int id, final T source, final boolean optional);

  public Resources resources(final T source);
}
