package io.sento.asmifier;

import android.view.View;

public class ParentBinding$$1 implements View.OnClickListener {
  private final Parent target;

  public ParentBinding$$1(final Parent target) {
    this.target = target;
  }

  @Override
  public void onClick(final View view) {
    target.onContainerClick(view);
  }
}
