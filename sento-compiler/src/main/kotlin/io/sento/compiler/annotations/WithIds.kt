package io.sento.compiler.annotations

import io.sento.compiler.model.AnnotationSpec

internal interface WithIds {
  public companion object {
    public fun resolve(annotation: AnnotationSpec): WithIds {
      return object : WithIds {
        override fun value(): IntArray {
          val ids = annotation.value<IntArray>("value")
          val id = annotation.value<Int>("value")

          if (id != null) {
            return intArrayOf(id)
          }

          if (ids != null) {
            return ids
          }

          return IntArray(0)
        }
      }
    }
  }

  public fun value(): IntArray
}
