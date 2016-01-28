package io.sento.compiler

import io.sento.compiler.common.Naming

internal class GenerationEnvironment(
    val registry: ClassRegistry,
    val naming: Naming
) {
  fun newClassWriter(): ClassWriter {
    return ClassWriter(this)
  }

  fun newClass(visitor: ClassWriter.() -> Unit): ByteArray {
    return newClassWriter().apply(visitor).toByteArray()
  }
}
