package io.mironov.sento.compiler

import java.io.File
import java.util.ArrayList

class SentoOptions(
    val inputs: Collection<File>,
    val libs: Collection<File>,
    val output: File
) {
  class Builder(val output: File) {
    private val libs = ArrayList<File>()
    private val inputs = ArrayList<File>()

    fun inputs(files: List<File>) = apply {
      inputs.addAll(files)
    }

    fun libs(files: List<File>) = apply {
      libs.addAll(files)
    }

    fun build(): SentoOptions {
      return SentoOptions(inputs, libs, output)
    }
  }
}
