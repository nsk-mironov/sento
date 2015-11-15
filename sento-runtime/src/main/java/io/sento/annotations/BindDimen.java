package io.sento.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
@ResourceBindings({
    @ResourceBinding(
        type = "float",
        getter = "getDimension"
    ),
    @ResourceBinding(
        type = "int",
        getter = "getDimensionPixelSize"
    )
})
public @interface BindDimen {
  public int value();
}
