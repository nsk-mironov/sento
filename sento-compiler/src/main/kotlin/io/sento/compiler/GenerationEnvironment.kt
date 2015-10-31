package io.sento.compiler

public class GenerationEnvironment {
  public fun debug(message: String) {
    println("[DEBUG] $message")
  }

  public fun error(message: String) {
    println("[ERROR] $message")
  }

  public fun info(message: String) {
    println("[INFO] $message")
  }
}
