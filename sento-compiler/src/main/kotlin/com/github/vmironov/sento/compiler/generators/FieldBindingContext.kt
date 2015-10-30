package com.github.vmironov.sento.compiler.generators

import com.github.vmironov.sento.compiler.specs.ClassSpec
import com.github.vmironov.sento.compiler.specs.FieldSpec
import org.objectweb.asm.MethodVisitor

public class FieldBindingContext<A : Annotation>(
    public val field: FieldSpec,
    public val clazz: ClassSpec,
    public val annotation: A,
    public val visitor: MethodVisitor,
    public val variables: Map<String, Int>
) {
  public fun variable(name: String): Int {
    return variables.get(name) ?: throw UnsupportedOperationException("Unknown variable \"$name\"")
  }
}
