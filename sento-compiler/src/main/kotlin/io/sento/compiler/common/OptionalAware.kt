package io.sento.compiler.common

import io.sento.compiler.model.ClassSpec
import io.sento.compiler.model.FieldSpec
import io.sento.compiler.model.MethodSpec

internal class OptionalAware(private val spec: ClassSpec) {
  public fun isOptional(field: FieldSpec): Boolean {
    return false
  }

  public fun isOptional(method: MethodSpec): Boolean {
    return false
  }
}
