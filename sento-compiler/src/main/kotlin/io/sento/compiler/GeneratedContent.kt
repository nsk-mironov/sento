package io.sento.compiler

import io.sento.compiler.common.Types
import org.objectweb.asm.Type
import java.util.NoSuchElementException

internal class GeneratedContent(
    public val path: String,
    public val extras: Map<String, Any>,
    public val content: ByteArray
) {
  public companion object {
    public fun from(type: Type, extras: Map<String, Any>, content: ByteArray): GeneratedContent {
      return GeneratedContent(Types.getClassFilePath(type), extras, content)
    }
  }

  public inline fun <reified T> extra(name: String): T {
    return extras[name] as T ?: throw NoSuchElementException(name)
  }

  public fun has(name: String): Boolean {
    return extras.containsKey(name)
  }
}
