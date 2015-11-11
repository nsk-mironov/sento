package io.sento;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
@ResourceBindings({
    @ResourceBinding(
        type = "java.lang.String[]",
        getter = "getStringArray"
    ),
    @ResourceBinding(
        type = "java.lang.CharSequence[]",
        getter = "getTextArray"
    ),
    @ResourceBinding(
        type = "int[]",
        getter = "getIntArray"
    )
})
public @interface BindArray {
  public int value();
}
