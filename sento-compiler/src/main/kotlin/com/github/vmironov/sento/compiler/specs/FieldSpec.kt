package com.github.vmironov.sento.compiler.specs

import org.objectweb.asm.Type
import java.util.ArrayList

public data class FieldSpec(
    public val name: String,
    public val type: Type,
    public val annotations: List<AnnotationSpec>
) {
  public class Builder(val name: String, val type: Type) {
    private val annotations = ArrayList<AnnotationSpec>()

    public fun annotation(annotation: AnnotationSpec): Builder = apply {
      annotations.add(annotation)
    }

    public fun build(): FieldSpec {
      return FieldSpec(name, type, annotations)
    }
  }
}
