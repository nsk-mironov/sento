package io.sento.compiler.bindings.fields

import io.sento.compiler.common.TypeFactory
import io.sento.compiler.model.AnnotationSpec
import io.sento.compiler.model.ClassSpec
import io.sento.compiler.model.FieldSpec
import org.objectweb.asm.commons.GeneratorAdapter
import java.util.NoSuchElementException

internal class FieldBindingContext(
    public val field: FieldSpec,
    public val clazz: ClassSpec,
    public val annotation: AnnotationSpec,
    public val adapter: GeneratorAdapter,
    public val variables: Map<String, Int>,
    public val factory: TypeFactory,
    public val optional: Boolean
) {
  public fun variable(name: String): Int {
    return variables[name] ?: throw NoSuchElementException("Unknown variable \"$name\"")
  }
}
