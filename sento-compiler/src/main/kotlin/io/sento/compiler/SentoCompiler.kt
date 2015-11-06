package io.sento.compiler

import io.sento.compiler.api.GenerationEnvironment
import io.sento.compiler.bindings.BindingContentGenerator
import org.apache.commons.io.FileUtils
import java.io.File

public class SentoCompiler() {
  public fun compile(options: SentoOptions) {
    println("input ${options.input}")
    println("output ${options.output}")
    println("incremental ${options.incremental}")
    println("libs ${options.libs}")
    println("dry ${options.dryRun}")

    val registry = ClassRegistryFactory.from(options)
    val environment = GenerationEnvironment(registry)
    val generator = BindingContentGenerator()

    FileUtils.copyDirectory(options.input, options.output).apply {
      registry.classes.forEach {
        generator.onGenerateContent(it, environment).forEach {
          FileUtils.deleteQuietly(File(options.output, it.path)).apply {
            FileUtils.writeByteArrayToFile(File(options.output, it.path), it.content)
          }
        }
      }
    }
  }
}
