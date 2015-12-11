package io.sento.compiler

import io.sento.compiler.common.Naming

internal class GenerationEnvironment(
    public val registry: ClassRegistry,
    public val naming: Naming
) {
  public fun newClassWriter(): ClassWriter {
    return ClassWriter(registry)
  }

  public fun newClass(visitor: ClassWriter.() -> Unit): ByteArray {
    return newClassWriter().apply(visitor).toByteArray()
  }
}
