package io.sento.compiler.model

import io.sento.compiler.common.AnnotationProxy
import org.objectweb.asm.Type
import java.util.ArrayList

internal data class MethodSpec(
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

  public fun <A : Annotation> getAnnotation(annotation: Class<A>): A? {
    val type = Type.getType(annotation)
    val spec = annotations.firstOrNull {
      it.type == type
    } ?: return null

    return AnnotationProxy.create(annotation, spec.values)
  }
}
