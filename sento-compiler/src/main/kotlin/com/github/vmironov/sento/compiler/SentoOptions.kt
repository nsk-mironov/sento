package com.github.vmironov.sento.compiler

import java.io.File

public class SentoOptions(
    val input: File,
    val output: File,
    val incremental: Boolean,
    val dryRun: Boolean
) {
  public class Builder() {
    private lateinit var input: File
    private lateinit var output: File

    private var incremental = false
    private var dryRun = false

    public fun input(input: File) = apply {
      this.input = input
    }

    public fun output(output: File) = apply {
      this.output = output
    }

    public fun incremental(enabled: Boolean) = apply {
      incremental = enabled
    }

    public fun dryRun(enabled: Boolean) = apply {
      dryRun = enabled
    }

    public fun build(): SentoOptions {
      return SentoOptions(input, output, incremental, dryRun)
    }
  }
}