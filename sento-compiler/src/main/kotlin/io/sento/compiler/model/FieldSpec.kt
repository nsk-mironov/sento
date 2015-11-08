package io.sento.compiler.model

import io.sento.compiler.common.AnnotationProxy
import org.objectweb.asm.Type
import java.util.ArrayList

internal data class FieldSpec(
    public val name: String,
    public val type: Type,
    public val annotations: Collection<AnnotationSpec>
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

  public inline fun <reified A : Annotation> getAnnotation(): A? {
    return getAnnotation(A::class.java)
  }

  public fun <A : Annotation> getAnnotation(annotation: Class<A>): A? {
    val type = Type.getType(annotation)
    val spec = annotations.firstOrNull {
      it.type == type
    } ?: return null

    return AnnotationProxy.create(annotation, spec.values)
  }
}
