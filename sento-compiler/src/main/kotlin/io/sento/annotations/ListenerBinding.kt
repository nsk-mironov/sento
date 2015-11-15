package io.sento.annotations

import org.objectweb.asm.Type

internal @AnnotationDelegate interface ListenerBinding {
  public fun owner(): Type

  public fun listener(): Type

  public fun setter(): String
}
