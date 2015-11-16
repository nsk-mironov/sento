package io.sento.annotations;

import android.graphics.drawable.Drawable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
@ResourceBindings({
    @ResourceBinding(
        type = Drawable.class,
        getter = "getDrawable",
        array = false
    )
})
public @interface BindDrawable {
  public int value();
}
