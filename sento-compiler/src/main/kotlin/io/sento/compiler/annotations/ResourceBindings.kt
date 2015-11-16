package io.sento.compiler.annotations

@AnnotationDelegate("io.sento.annotations.ResourceBindings")
internal interface ResourceBindings {
  public fun value(): Array<ResourceBinding>
}
