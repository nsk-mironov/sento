package io.sento.compiler.bindings

import io.sento.compiler.reflection.AnnotationSpec
import io.sento.compiler.reflection.ClassSpec
import io.sento.compiler.reflection.FieldSpec
import org.objectweb.asm.commons.GeneratorAdapter
import java.util.NoSuchElementException

internal class ViewBindingContext(
    public val field: FieldSpec,
    public val clazz: ClassSpec,
    public val annotation: AnnotationSpec,
    public val adapter: GeneratorAdapter,
    public val variables: Map<String, Int>,
    public val arguments: Map<String, Int>,
    public val optional: Boolean
) {
  public fun argument(name: String): Int {
    return arguments[name] ?: throw NoSuchElementException("Unknown argument \"$name\"")
  }

  public fun variable(name: String): Int {
    return variables[name] ?: throw NoSuchElementException("Unknown variable \"$name\"")
  }
}
