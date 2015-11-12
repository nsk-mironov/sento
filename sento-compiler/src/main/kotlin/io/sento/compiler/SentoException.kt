package io.sento.compiler

public class SentoException : RuntimeException {
  public constructor() : super()
  public constructor(message: String) : super(message)
  public constructor(message: String, cause: Throwable) : super(message, cause)
  public constructor(cause: Throwable) : super(cause)
}
