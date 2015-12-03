package io.sento.compiler.common

import org.objectweb.asm.commons.GeneratorAdapter

internal inline fun GeneratorAdapter.body(action: GeneratorAdapter.() -> Unit) {
  action()
  returnValue()
  endMethod()
}
