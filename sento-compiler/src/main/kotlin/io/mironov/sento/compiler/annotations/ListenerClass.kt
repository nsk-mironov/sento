package io.mironov.sento.compiler.annotations

@AnnotationDelegate("io.sento.annotations.ListenerClass")
internal interface ListenerClass {
  fun owner(): String

  fun listener(): String

  fun callback(): String

  fun setter(): String

  fun unsetter(): String?
}
