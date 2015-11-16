package io.sento.compiler.annotations

import org.objectweb.asm.Type

@AnnotationDelegate("io.sento.annotations.ResourceBinding")
internal interface ResourceBinding {
  public fun type(): Type

  public fun array(): Boolean

  public fun getter(): String
}
