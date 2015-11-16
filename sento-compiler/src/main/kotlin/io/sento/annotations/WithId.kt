package io.sento.annotations

import io.sento.compiler.model.AnnotationSpec

internal interface WithId {
  public companion object {
    public fun resolve(annotation: AnnotationSpec): WithId {
      return object : WithId {
        override fun value(): Int {
          return annotation.value("value") ?: throw NoSuchFieldException("value")
        }
      }
    }
  }

  public fun value(): Int
}