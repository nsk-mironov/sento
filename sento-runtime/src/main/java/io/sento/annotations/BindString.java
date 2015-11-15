package io.sento.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
@ResourceBindings({
    @ResourceBinding(
        type = "java.lang.String",
        getter = "getString"
    ),
    @ResourceBinding(
        type = "java.lang.CharSequence",
        getter = "getText"
    )
})
public @interface BindString {
  public int value();
}
