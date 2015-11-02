package io.sento.compiler.bindings

import io.sento.compiler.common.TypeFactory
import io.sento.compiler.specs.ClassSpec
import io.sento.compiler.specs.FieldSpec
import org.objectweb.asm.MethodVisitor
import java.util.NoSuchElementException

internal class FieldBindingContext<A : Annotation>(
    public val field: FieldSpec,
    public val clazz: ClassSpec,
    public val annotation: A,
    public val visitor: MethodVisitor,
    public val variables: Map<String, Int>,
    public val factory: TypeFactory
) {
  public fun variable(name: String): Int {
    return variables.get(name) ?: throw NoSuchElementException("Unknown variable \"$name\"")
  }
}
