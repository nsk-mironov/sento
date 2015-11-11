package io.sento.compiler

import org.objectweb.asm.ClassWriter

internal class GenerationEnvironment(public val registry: ClassRegistry) {
  public fun info(message: String) {
    println("[INFO] $message")
  }

  public fun debug(message: String) {
    println("[DEBUG] $message")
  }

  public fun error(message: String) {
    println("[ERROR] $message")
  }

  public fun fatal(message: String) {
    throw RuntimeException(message)
  }

  public fun createClass(visitor: ClassWriter.() -> Unit): ByteArray {
    return ClassWriter(ClassWriter.COMPUTE_MAXS).apply {
      visitor()
      visitEnd()
    }.toByteArray()
  }
}
