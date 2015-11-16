package io.sento.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
@ResourceBindings({
    @ResourceBinding(
        type = float.class,
        getter = "getDimension",
        array = false
    ),
    @ResourceBinding(
        type = int.class,
        getter = "getDimensionPixelSize",
        array = false
    )
})
public @interface BindDimen {
  public int value();
}
