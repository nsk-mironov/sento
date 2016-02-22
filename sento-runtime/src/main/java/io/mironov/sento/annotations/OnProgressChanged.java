package io.mironov.sento.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
@ListenerClass(
    owner = "android.widget.SeekBar",
    listener = "android.widget.SeekBar$OnSeekBarChangeListener",
    setter = "setOnSeekBarChangeListener",
    callback = "onProgressChanged"
)
public @interface OnProgressChanged {
  public int[] value();
}
