package io.sento.compiler

import io.sento.compiler.bindings.SentoBindingContentGenerator
import io.sento.compiler.bindings.SentoFactoryContentGenerator
import io.sento.compiler.model.SentoBindingSpec
import org.apache.commons.io.FileUtils
import java.io.File
import java.util.ArrayList

public class SentoCompiler() {
  public fun compile(options: SentoOptions) {
    println("input ${options.input}")
    println("output ${options.output}")
    println("incremental ${options.incremental}")
    println("libs ${options.libs}")
    println("dry ${options.dryRun}")

    val registry = ClassRegistryFactory.create(options)
    val environment = GenerationEnvironment(registry)

    val bindings = ArrayList<SentoBindingSpec>()

    FileUtils.copyDirectory(options.input, options.output).apply {
      registry.classes.forEach {
        SentoBindingContentGenerator(it).onGenerateContent(environment).forEach {
          if (it.containsExtra(SentoBindingContentGenerator.EXTRA_BINDING_SPEC)) {
            bindings.add(it.extra<SentoBindingSpec>(SentoBindingContentGenerator.EXTRA_BINDING_SPEC))
          }

          FileUtils.deleteQuietly(File(options.output, it.path)).apply {
            FileUtils.writeByteArrayToFile(File(options.output, it.path), it.content)
          }
        }
      }
    }

    SentoFactoryContentGenerator(bindings).onGenerateContent(environment).forEach {
      FileUtils.deleteQuietly(File(options.output, it.path)).apply {
        FileUtils.writeByteArrayToFile(File(options.output, it.path), it.content)
      }
    }
  }
}
