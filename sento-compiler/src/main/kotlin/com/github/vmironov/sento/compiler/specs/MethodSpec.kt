package com.github.vmironov.sento.compiler.specs

import org.objectweb.asm.Type
import java.util.ArrayList

public data class MethodSpec(
    public val name: String,
    public val type: Type,
    public val annotations: List<AnnotationSpec>
) {
  public class Builder(val name: String, val type: Type) {
    private val annotations = ArrayList<AnnotationSpec>()

    public fun annotation(annotation: AnnotationSpec): Builder = apply {
      annotations.add(annotation)
    }

    public fun build(): MethodSpec {
      return MethodSpec(name, type, annotations)
    }
  }
}
