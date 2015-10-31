package io.sento.compiler

internal class GenerationEnvironment {
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
}
