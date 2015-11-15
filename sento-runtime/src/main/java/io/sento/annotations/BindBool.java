package io.sento.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
@ResourceBindings(
    @ResourceBinding(
        type = "boolean",
        getter = "getBoolean"
    )
)
public @interface BindBool {
  public int value();
}