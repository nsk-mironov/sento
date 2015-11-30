package io.sento.compiler.bindings.methods

import io.sento.compiler.common.TypeFactory
import io.sento.compiler.model.AnnotationSpec
import io.sento.compiler.model.ClassSpec
import io.sento.compiler.model.MethodSpec
import org.objectweb.asm.commons.GeneratorAdapter
import java.util.NoSuchElementException

internal class MethodBindingContext(
    public val method: MethodSpec,
    public val clazz: ClassSpec,
    public val annotation: AnnotationSpec,
    public val adapter: GeneratorAdapter,
    public val variables: Map<String, Int>,
    public val arguments: Map<String, Int>,
    public val factory: TypeFactory,
    public val optional: Boolean
) {
  public fun argument(name: String): Int {
    return arguments[name] ?: throw NoSuchElementException("Unknown argument \"$name\"")
  }

  public fun variable(name: String): Int {
    return variables[name] ?: throw NoSuchElementException("Unknown variable \"$name\"")
  }
}
