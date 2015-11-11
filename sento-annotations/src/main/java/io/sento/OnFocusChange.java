package io.sento;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
@MethodBinding(
  owner = "android.view.View",
  listener = "android.view.View$OnFocusChangeListener",
  setter = "setOnFocusChangeListener"
)
public @interface OnFocusChange {
  public int[] value();
}
