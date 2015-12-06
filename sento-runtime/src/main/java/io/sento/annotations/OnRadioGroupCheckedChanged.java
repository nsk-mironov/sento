package io.sento.annotations;

import android.widget.RadioGroup;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
@ListenerClass(
    owner = RadioGroup.class,
    listener = RadioGroup.OnCheckedChangeListener.class,
    setter = "setOnCheckedChangeListener",
    callback = "onCheckedChanged"
)
public @interface OnRadioGroupCheckedChanged {
  public int[] value();
}
