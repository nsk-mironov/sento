package io.sento.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
@ResourceBindings(
    @ResourceBinding(
        type = int.class,
        getter = "getInteger",
        array = false
    )
)
public @interface BindInteger {
  public int value();
}
