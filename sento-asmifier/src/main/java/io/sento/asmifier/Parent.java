package io.sento.asmifier;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

public class Parent {
  public ImageView optional = null;
  public ImageView required = null;

  public Drawable background = null;
  public boolean enabled = false;

  public String title = null;
  public float padding = 0;

  public View container = null;

  public void onContainerClick(final View view) {

  }
}
