package io.sento.compiler.model

import io.sento.compiler.reflect.FieldSpec
import io.sento.compiler.reflect.MethodSpec

internal sealed class ViewOwner(val name: String) {
  companion object {
    fun from(field: FieldSpec): ViewOwner {
      return ViewOwner.Field(field)
    }

    fun from(method: MethodSpec): ViewOwner {
      return ViewOwner.Method(method)
    }
  }

  internal class Field(field: FieldSpec) : ViewOwner("field '${field.name}'")
  internal class Method(method: MethodSpec) : ViewOwner("method '${method.name}'")
}
