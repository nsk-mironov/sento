package io.mironov.sento.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.ANNOTATION_TYPE)
public @interface ListenerClass {
  public String owner();

  public String listener();

  public String callback();

  public String setter();

  public String unsetter() default "";
}
