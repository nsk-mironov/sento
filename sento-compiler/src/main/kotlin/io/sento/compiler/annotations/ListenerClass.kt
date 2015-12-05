package io.sento.compiler.annotations

import org.objectweb.asm.Type

@AnnotationDelegate("io.sento.annotations.ListenerClass")
internal interface ListenerClass {
  public fun owner(): Type

  public fun listener(): Type

  public fun setter(): String

  public fun callback(): String
}
