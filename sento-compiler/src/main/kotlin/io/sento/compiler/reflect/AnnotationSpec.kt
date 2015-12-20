package io.sento.compiler.reflect

import org.objectweb.asm.Type
import java.util.LinkedHashMap

internal data class AnnotationSpec(
    public val type: Type,
    public val values: Map<String, Any?>
) {
  internal class Builder(val type: Type) {
    private val values = LinkedHashMap<String, Any?>()

    public fun value(name: String, value: Any?): Builder = apply {
      values.put(name, value)
    }

    public fun build(): AnnotationSpec {
      return AnnotationSpec(type, values)
    }
  }

  public inline fun <reified V : Any> value(name: String): V {
    return values[name] as V
  }
}
