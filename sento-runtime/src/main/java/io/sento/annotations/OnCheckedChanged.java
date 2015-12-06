package io.sento.annotations;

import android.widget.CompoundButton;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
@ListenerClass(
    owner = CompoundButton.class,
    listener = CompoundButton.OnCheckedChangeListener.class,
    setter = "setOnCheckedChangeListener",
    callback = "onCheckedChanged"
)
public @interface OnCheckedChanged {
  public int[] value();
}
