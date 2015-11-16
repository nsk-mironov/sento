package io.sento.compiler.annotations

@AnnotationDelegate("io.sento.annotations.Bind")
internal interface Bind {
  public fun value(): Int
}
