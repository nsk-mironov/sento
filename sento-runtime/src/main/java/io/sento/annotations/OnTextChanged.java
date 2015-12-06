package io.sento.annotations;

import android.text.TextWatcher;
import android.widget.TextView;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
@ListenerClass(
    owner = TextView.class,
    listener = TextWatcher.class,
    setter = "addTextChangedListener",
    unsetter = "removeTextChangedListener",
    callback = "onTextChanged"
)
public @interface OnTextChanged {
  public int[] value();
}
