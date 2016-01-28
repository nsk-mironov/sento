package io.sento.compiler

import java.text.MessageFormat

class SentoException : RuntimeException {
  constructor(message: String, vararg args: Any?) : super(MessageFormat.format(message, *args))
}
