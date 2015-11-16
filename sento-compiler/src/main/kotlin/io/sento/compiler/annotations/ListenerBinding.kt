package io.sento.compiler.annotations

import org.objectweb.asm.Type

@AnnotationDelegate("io.sento.annotations.ListenerBinding")
internal interface ListenerBinding {
  public fun owner(): Type

  public fun listener(): Type

  public fun setter(): String
}
