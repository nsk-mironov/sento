package io.sento.sample;

import android.view.View;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class DebouncingOnClickListener implements View.OnClickListener {
  private static final AtomicBoolean ENABLED = new AtomicBoolean(true);

  private static final Runnable ENABLE_AGAIN = new Runnable() {
    @Override
    public void run() {
      ENABLED.set(true);
    }
  };

  @Override
  public final void onClick(final View view) {
    if (ENABLED.compareAndSet(true, false)) {
      view.post(ENABLE_AGAIN);
      onDebouncedClick(view);
    }
  }

  public abstract void onDebouncedClick(final View view);
}