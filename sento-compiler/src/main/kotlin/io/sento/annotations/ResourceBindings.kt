package io.sento.annotations

internal @AnnotationDelegate interface ResourceBindings {
  public fun value(): Array<ResourceBinding>
}
