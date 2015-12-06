package io.sento.annotations;

import android.widget.SeekBar;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
@ListenerClass(
    owner = SeekBar.class,
    listener = SeekBar.OnSeekBarChangeListener.class,
    setter = "setOnSeekBarChangeListener",
    callback = "onProgressChanged"
)
public @interface OnProgressChanged {
  public int[] value();
}
