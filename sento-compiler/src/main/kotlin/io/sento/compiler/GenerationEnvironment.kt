package io.sento.compiler

import org.objectweb.asm.ClassWriter
import java.text.MessageFormat

internal class GenerationEnvironment(public val registry: ClassRegistry) {
  public fun info(message: String, vararg args: Any?) {
    println("[INFO] ${MessageFormat.format(message, *args)}")
  }

  public fun debug(message: String, vararg args: Any?) {
    println("[DEBUG] ${MessageFormat.format(message, *args)}")
  }

  public fun error(message: String, vararg args: Any?) {
    println("[ERROR] ${MessageFormat.format(message, *args)}}")
  }

  public fun fatal(message: String, vararg args: Any?) {
    throw SentoException(MessageFormat.format(message, *args))
  }

  public fun createClass(visitor: ClassWriter.() -> Unit): ByteArray {
    return ClassWriter(ClassWriter.COMPUTE_MAXS).apply {
      visitor()
      visitEnd()
    }.toByteArray()
  }
}
