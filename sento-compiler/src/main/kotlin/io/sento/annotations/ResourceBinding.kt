package io.sento.annotations

internal @AnnotationDelegate interface ResourceBinding {
  public fun type(): String

  public fun getter(): String
}
