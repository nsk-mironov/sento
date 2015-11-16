package io.sento.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
@ResourceBindings({
    @ResourceBinding(
        type = String.class,
        getter = "getStringArray",
        array = true
    ),
    @ResourceBinding(
        type = CharSequence.class,
        getter = "getTextArray",
        array = true
    ),
    @ResourceBinding(
        type = int.class,
        getter = "getIntArray",
        array = true
    )
})
public @interface BindArray {
  public int value();
}
