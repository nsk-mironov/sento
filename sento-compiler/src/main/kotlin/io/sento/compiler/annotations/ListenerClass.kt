package io.sento.compiler.annotations

import org.objectweb.asm.Type

@AnnotationDelegate("io.sento.annotations.ListenerClass")
internal interface ListenerClass {
  public fun owner(): Type

  public fun listener(): Type

  public fun callback(): String

  public fun setter(): String

  public fun unsetter(): String?
}
