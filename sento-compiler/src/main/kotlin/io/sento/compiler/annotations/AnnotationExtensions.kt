package io.sento.compiler.annotations

import io.sento.compiler.reflect.AnnotationSpec

internal val AnnotationSpec.ids: IntArray
  get() {
    val ids = value<IntArray>("value")
    val id = value<Int>("value")

    return when {
      ids != null -> ids
      id != null -> intArrayOf(id)
      else -> IntArray(0)
    }
  }
