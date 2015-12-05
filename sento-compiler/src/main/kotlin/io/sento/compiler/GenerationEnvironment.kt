package io.sento.compiler

import org.objectweb.asm.ClassWriter

internal class GenerationEnvironment(public val registry: ClassRegistry) {
  public fun newClassWriter(): ClassWriter {
    return ClassRegistryAwareClassWriter(registry)
  }

  public fun newClass(visitor: ClassWriter.() -> Unit): ByteArray {
    return newClassWriter().apply(visitor).toByteArray()
  }
}
