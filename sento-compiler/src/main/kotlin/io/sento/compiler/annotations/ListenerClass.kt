package io.sento.compiler.annotations

@AnnotationDelegate("io.sento.annotations.ListenerClass")
internal interface ListenerClass {
  public fun owner(): String

  public fun listener(): String

  public fun callback(): String

  public fun setter(): String

  public fun unsetter(): String?
}
