package io.sento.compiler.annotations

@AnnotationDelegate("kotlin.Metadata")
interface Metadata {
  fun d1(): Array<String>

  fun d2(): Array<String>
}

val Metadata.strings: Array<String>
  get() = d1()

val Metadata.data: Array<String>
  get() = d2()
