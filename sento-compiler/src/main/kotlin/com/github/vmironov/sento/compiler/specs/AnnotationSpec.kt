package com.github.vmironov.sento.compiler.specs

import org.objectweb.asm.Type
import java.util.LinkedHashMap

public data class AnnotationSpec(
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
}
