package io.sento;

public @interface MethodBinding {
  public String owner();

  public String listener();

  public String setter();
}
