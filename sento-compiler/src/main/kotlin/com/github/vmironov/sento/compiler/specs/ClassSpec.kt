package com.github.vmironov.sento.compiler.specs

import org.objectweb.asm.Type
import java.util.ArrayList

public data class ClassSpec(
    public val type: Type,
    public val parent: Type,
    public val annotations: List<AnnotationSpec>,
    public val fields: List<FieldSpec>,
    public val methods: List<MethodSpec>
) {
  public class Builder(val type: Type, val parent: Type) {
    private val annotations = ArrayList<AnnotationSpec>()
    private val fields = ArrayList<FieldSpec>()
    private val methods = ArrayList<MethodSpec>()

    public fun annotation(annotation: AnnotationSpec): Builder = apply {
      annotations.add(annotation)
    }

    public fun field(field: FieldSpec): Builder = apply {
      fields.add(field)
    }

    public fun method(method: MethodSpec): Builder = apply {
      methods.add(method)
    }

    public fun build(): ClassSpec {
      return ClassSpec(type, parent, annotations, fields, methods)
    }
  }
}
