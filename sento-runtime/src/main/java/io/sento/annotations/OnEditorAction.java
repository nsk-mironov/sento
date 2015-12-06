package io.sento.annotations;

import android.widget.TextView;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
@ListenerClass(
    owner = TextView.class,
    listener = TextView.OnEditorActionListener.class,
    setter = "setOnEditorActionListener",
    callback = "onEditorAction"
)
public @interface OnEditorAction {
  public int[] value();
}
