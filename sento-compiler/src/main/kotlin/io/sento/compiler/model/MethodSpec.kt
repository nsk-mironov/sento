package io.sento.compiler.model

import io.sento.compiler.common.Annotations
import org.objectweb.asm.Type
import java.util.ArrayList

internal data class MethodSpec(
    public val access: Int,
    public val name: String,
    public val type: Type,
    public val signature: String?,
    public val annotations: Collection<AnnotationSpec>
) {
  internal class Builder(val access: Int, val name: String, val type: Type, val signature: String?) {
    private val annotations = ArrayList<AnnotationSpec>()

    public fun annotation(annotation: AnnotationSpec): Builder = apply {
      annotations.add(annotation)
    }

    public fun build(): MethodSpec {
      return MethodSpec(access, name, type, signature, annotations)
    }
  }

  public inline fun <reified A : Annotation> getAnnotation(): A? {
    return getAnnotation(A::class.java)
  }

  public fun <A : Annotation> getAnnotation(annotation: Class<A>): A? {
    return Annotations.create(annotation, annotations.firstOrNull {
      it.type == Type.getType(annotation)
    } ?: return null)
  }
}
