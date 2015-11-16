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
        getter = "getString",
        array = false
    ),
    @ResourceBinding(
        type = CharSequence.class,
        getter = "getText",
        array = false
    )
})
public @interface BindString {
  public int value();
}
