package io.sento.compiler.model

import io.sento.compiler.reflection.FieldSpec
import io.sento.compiler.reflection.MethodSpec

internal sealed class ViewOwner(val name: String) {
  class Field(field: FieldSpec) : ViewOwner("field '${field.name}'")
  class Method(method: MethodSpec) : ViewOwner("method '${method.name}'")
}
