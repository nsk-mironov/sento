package com.github.vmironov.sento.compiler.specs

import com.github.vmironov.sento.compiler.common.AnnotationProxy
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

  public fun <A : Annotation> getAnnotation(annotation: Class<A>): A? {
    val type = Type.getType(annotation)
    val spec = annotations.firstOrNull {
      it.type == type
    } ?: return null

    return AnnotationProxy.create(annotation, spec.values)
  }
}
