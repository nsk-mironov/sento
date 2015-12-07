package io.sento.compiler.bindings

import java.util.NoSuchElementException

internal class VariablesContext(private val names: Map<String, Int>) {
  public fun variable(name: String): Int {
    return names[name] ?: throw NoSuchElementException("Unknown variable \"$name\"")
  }

  public fun target(): Int {
    return variable("target")
  }

  public fun view(id: Int): Int {
    return variable("view$id")
  }
}
