package com.github.vmironov.sento.compiler

public class SentoCompiler() {
  public fun compile(options: SentoOptions) {
    println("input ${options.input}")
    println("output ${options.output}")
    println("incremental ${options.incremental}")
    println("dry ${options.dryRun}")
  }
}
