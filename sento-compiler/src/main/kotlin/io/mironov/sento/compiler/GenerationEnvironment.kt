package io.mironov.sento.compiler

import io.mironov.sento.compiler.common.Naming

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
