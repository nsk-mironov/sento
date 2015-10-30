package com.github.vmironov.sento.compiler.specs

import com.github.vmironov.sento.compiler.common.AnnotationProxy
import org.objectweb.asm.Type
import java.util.LinkedHashMap

internal data class AnnotationSpec(
    public val type: Type,
    public val values: Map<String, Any?>
) {
  public class Builder(val type: Type) {
    private val values = LinkedHashMap<String, Any?>()

    public fun value(name: String, value: Any?): Builder = apply {
      values.put(name, value)
    }

    public fun build(): AnnotationSpec {
      return AnnotationSpec(type, values)
    }
  }

  public inline fun <reified A : Annotation> resolve(): A {
    return AnnotationProxy.create(Class.forName(type.className).asSubclass(A::class.java), values)
  }
}
