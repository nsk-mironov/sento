package io.sento.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
@ListenerClass(
    owner = "android.widget.RadioGroup",
    listener = "android.widget.RadioGroup$OnCheckedChangeListener",
    setter = "setOnCheckedChangeListener",
    callback = "onCheckedChanged"
)
public @interface OnRadioGroupCheckedChanged {
  public int[] value();
}
