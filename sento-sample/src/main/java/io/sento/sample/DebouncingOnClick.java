package io.sento.sample;

import io.sento.ListenerBinding;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
@ListenerBinding(
    owner = "android.view.View",
    listener = "io.sento.sample.DebouncingOnClickListener",
    setter = "setOnClickListener"
)
public @interface DebouncingOnClick {
  public int[] value();
}
