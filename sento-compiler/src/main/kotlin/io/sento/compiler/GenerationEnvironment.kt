package io.sento.compiler

import org.objectweb.asm.ClassWriter

internal class GenerationEnvironment(public val registry: ClassRegistry) {
  public fun createClass(visitor: ClassWriter.() -> Unit): ByteArray {
    return ClassWriter(ClassWriter.COMPUTE_MAXS).apply {
      visitor()
      visitEnd()
    }.toByteArray()
  }
}
