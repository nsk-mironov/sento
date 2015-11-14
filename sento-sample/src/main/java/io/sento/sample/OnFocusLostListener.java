package io.sento.sample;

import android.view.View;

public abstract class OnFocusLostListener implements View.OnFocusChangeListener {
  @Override
  public void onFocusChange(final View view, final boolean focused) {
    if (!focused) {
      onFocusLost(view);
    }
  }

  public abstract void onFocusLost(final View view);
}
