package com.github.vmironov.sento;

import android.view.View;

public interface Finder<T> {
  public <V extends View> V find(final int id, final T source);
}
