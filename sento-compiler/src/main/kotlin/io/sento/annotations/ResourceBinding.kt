package io.sento.annotations

import org.objectweb.asm.Type

internal @AnnotationDelegate interface ResourceBinding {
  public fun type(): Type

  public fun array(): Boolean

  public fun getter(): String
}
