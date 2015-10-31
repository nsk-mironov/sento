package io.sento.compiler

import java.io.File

public class SentoOptions(
    val input: File,
    val output: File,
    val incremental: Boolean,
    val dryRun: Boolean
) {
  public class Builder(val input: File, val output: File) {
    private var incremental = false
    private var dryRun = false

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
