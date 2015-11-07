package io.sento.compiler.api

import java.util.NoSuchElementException

internal class GeneratedContent(
    public val path: String,
    public val content: ByteArray,
    public val extras: Map<String, Any> = mapOf()
) {
  public inline fun <reified T> extra(name: String): T {
    return extras[name] as T ?: throw NoSuchElementException(name)
  }

  public fun containsExtra(name: String): Boolean {
    return extras.containsKey(name)
  }
}
