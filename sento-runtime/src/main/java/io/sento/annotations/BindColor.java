package io.sento.annotations;

import android.content.res.ColorStateList;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
@ResourceBindings({
    @ResourceBinding(
        type = int.class,
        getter = "getColor",
        array = false
    ),
    @ResourceBinding(
        type = ColorStateList.class,
        getter = "getColorStateList",
        array = false
    )
})
public @interface BindColor {
  public int value();
}
