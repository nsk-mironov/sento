package io.mironov.sento.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
@ListenerClass(
    owner = "android.widget.TextView",
    listener = "android.text.TextWatcher",
    setter = "addTextChangedListener",
    unsetter = "removeTextChangedListener",
    callback = "onTextChanged"
)
public @interface OnTextChanged {
  public int[] value();
}
