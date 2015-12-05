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
    listener = View.OnClickListener.class,
    setter = "setOnClickListener",
    callback = "onClick"
)
public @interface OnClick {
  public int[] value();
}
