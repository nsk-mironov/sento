package io.sento;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
@MethodBinding(
    target = "android.view.View",
    listener = "android.view.View$OnHoverListener",
    setter = "setOnHoverListener"
)
public @interface OnHover {
  public int[] value();
}
