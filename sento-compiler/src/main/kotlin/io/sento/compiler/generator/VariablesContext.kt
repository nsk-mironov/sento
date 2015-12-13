package io.sento.compiler.generator

import java.util.HashMap
import java.util.NoSuchElementException

internal class VariablesContext() {
  private val names = HashMap<String, Int>()

  public fun variable(name: String, index: Int) {
    names[name] = index
  }

  public fun variable(name: String): Int {
    return names[name] ?: throw NoSuchElementException("Unknown variable \"$name\"")
  }

  public fun target(index: Int) {
    variable("target", index)
  }

  public fun target(): Int {
    return variable("target")
  }

  public fun view(id: Int, index: Int) {
    variable("view$id", index)
  }

  public fun view(id: Int): Int {
    return variable("view$id")
  }
}
