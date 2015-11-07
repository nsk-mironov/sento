package io.sento;

public @interface MethodBinding {
  public String target();

  public String listener();

  public String setter();
}
