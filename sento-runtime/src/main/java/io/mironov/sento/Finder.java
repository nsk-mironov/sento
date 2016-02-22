package io.mironov.sento;

import android.content.res.Resources;
import android.view.View;

public interface Finder {
  public View find(final int id, final Object source);

  public void require(final int id, final View view, final Object source, final String message);

  public Resources resources(final Object source);
}
