package io.sento.annotations;

import android.view.View;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
@ListenerBinding(
    owner = View.class,
    listener = View.OnClickListener.class,
    setter = "setOnClickListener"
)
public @interface OnClick {
  public int[] value();
}
