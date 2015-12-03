package io.sento;

import android.content.res.Resources;
import android.view.View;

public interface Finder<T> {
  public View find(final int id, final T source);

  public void require(final int id, final View view, final T source, final String message);

  public Resources resources(final T source);
}
