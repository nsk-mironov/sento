package io.sento.compiler

import java.text.MessageFormat

public class SentoException : RuntimeException {
  public constructor(message: String, vararg args: Any?) : super(MessageFormat.format(message, *args))
}
