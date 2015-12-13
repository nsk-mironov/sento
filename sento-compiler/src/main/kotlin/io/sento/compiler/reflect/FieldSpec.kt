package io.sento.compiler.reflect

import io.sento.compiler.annotations.AnnotationProxy
import io.sento.compiler.common.Types
import org.objectweb.asm.Type
import java.util.ArrayList

internal data class FieldSpec(
    public val access: Int,
    public val name: String,
    public val type: Type,
    public val annotations: Collection<AnnotationSpec>
) {
  internal class Builder(val access: Int, val name: String, val type: Type) {
    private val annotations = ArrayList<AnnotationSpec>()

    public fun annotation(annotation: AnnotationSpec): Builder = apply {
      annotations.add(annotation)
    }

    public fun build(): FieldSpec {
      return FieldSpec(access, name, type, annotations)
    }
  }

  public inline fun <reified A : Any> getAnnotation(): A? {
    return getAnnotation(A::class.java)
  }

  public fun <A> getAnnotation(annotation: Class<A>): A? {
    return AnnotationProxy.create(annotation, annotations.firstOrNull {
      it.type == Types.getAnnotationType(annotation)
    } ?: return null)
  }
}
