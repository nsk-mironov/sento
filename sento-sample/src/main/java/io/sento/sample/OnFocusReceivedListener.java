package io.sento.sample;

import android.view.View;

public abstract class OnFocusReceivedListener implements View.OnFocusChangeListener {
  @Override
  public void onFocusChange(final View view, final boolean focused) {
    if (focused) {
      onFocusReceived(view);
    }
  }

  public abstract void onFocusReceived(final View view);
}
