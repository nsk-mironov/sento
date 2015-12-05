package io.sento.annotations;

import android.view.View;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
@ListenerClass(
    owner = View.class,
    listener = View.OnHoverListener.class,
    setter = "setOnHoverListener",
    callback = "onHover"
)
public @interface OnHover {
  public int[] value();
}
