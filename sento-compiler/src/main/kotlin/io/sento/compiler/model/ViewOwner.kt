package io.sento.compiler.model

import io.sento.compiler.reflect.FieldSpec
import io.sento.compiler.reflect.MethodSpec

internal sealed class ViewOwner(val name: String) {
  class Field(field: FieldSpec) : ViewOwner("field '${field.name}'")
  class Method(method: MethodSpec) : ViewOwner("method '${method.name}'")
}
