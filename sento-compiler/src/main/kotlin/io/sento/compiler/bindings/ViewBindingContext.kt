package io.sento.compiler.bindings

import io.sento.compiler.common.GeneratorAdapter
import io.sento.compiler.model.BindTargetSpec
import java.util.NoSuchElementException

internal class ViewBindingContext(
    public val target: BindTargetSpec,
    public val adapter: GeneratorAdapter,
    public val variables: Map<String, Int>
) {
  public fun variable(name: String): Int {
    return variables[name] ?: throw NoSuchElementException("Unknown variable \"$name\"")
  }
}
