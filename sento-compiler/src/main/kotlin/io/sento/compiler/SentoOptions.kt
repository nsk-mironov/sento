package io.sento.compiler

import java.io.File
import java.util.ArrayList

public class SentoOptions(
    public val input: File,
    public val output: File,
    public val libs: Collection<File>,
    public val incremental: Boolean,
    public val dryRun: Boolean
) {
  public class Builder(val input: File, val output: File) {
    private val libs = ArrayList<File>()

    private var incremental = false
    private var dryRun = false

    public fun incremental(enabled: Boolean) = apply {
      incremental = enabled
    }

    public fun dryRun(enabled: Boolean) = apply {
      dryRun = enabled
    }

    public fun libs(files: List<File>) = apply {
      libs.addAll(files)
    }

    public fun build(): SentoOptions {
      return SentoOptions(input, output, libs, incremental, dryRun)
    }
  }
}
