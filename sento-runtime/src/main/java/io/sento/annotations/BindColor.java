package io.sento.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
@ResourceBindings({
    @ResourceBinding(
        type = "int",
        getter = "getColor"
    ),
    @ResourceBinding(
        type = "android.content.res.ColorStateList",
        getter = "getColorStateList"
    )
})
public @interface BindColor {
  public int value();
}
