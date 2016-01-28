package io.sento.compiler.generator

import java.util.HashMap
import java.util.NoSuchElementException

internal class VariablesContext() {
  private val names = HashMap<String, Int>()

  fun variable(name: String, index: Int) {
    names[name] = index
  }

  fun variable(name: String): Int {
    return names[name] ?: throw NoSuchElementException("Unknown variable \"$name\"")
  }

  fun target(index: Int) {
    variable("target", index)
  }

  fun target(): Int {
    return variable("target")
  }

  fun view(id: Int, index: Int) {
    variable("view$id", index)
  }

  fun view(id: Int): Int {
    return variable("view$id")
  }
}
