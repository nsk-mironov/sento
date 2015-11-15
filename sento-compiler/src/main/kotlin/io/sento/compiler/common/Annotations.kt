package io.sento.compiler.common

import io.sento.annotations.AnnotationProxy
import io.sento.compiler.model.AnnotationSpec

internal object Annotations {
  public fun <A> create(clazz: Class<A>, spec: AnnotationSpec): A {
    return AnnotationProxy.create(clazz, spec)
  }

  public fun ids(annotation: AnnotationSpec): IntArray {
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

  public fun id(annotation: AnnotationSpec): Int {
    return annotation.value<Int>("value") ?: throw NoSuchFieldException("value")
  }
}
