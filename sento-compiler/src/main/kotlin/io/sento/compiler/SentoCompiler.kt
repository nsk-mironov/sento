package io.sento.compiler

import io.sento.compiler.bindings.SentoBindingContentGenerator
import io.sento.compiler.bindings.SentoContentGeneratorFactory
import io.sento.compiler.model.SentoBindingSpec
import org.apache.commons.io.FileUtils
import java.io.File
import java.util.ArrayList

public class SentoCompiler() {
  public fun compile(options: SentoOptions) {
    println("inputs ${options.inputs}")
    println("libs ${options.libs}")
    println("output ${options.output}")
    println("incremental ${options.incremental}")
    println("dry ${options.dryRun}")

    val registry = ClassRegistryFactory.create(options)
    val environment = GenerationEnvironment(registry)

    val factory = SentoContentGeneratorFactory.from(environment)
    val bindings = ArrayList<SentoBindingSpec>()

    options.inputs.forEach {
      FileUtils.copyDirectory(it, options.output)
    }

    registry.inputs.forEach {
      factory.createBinding(registry.resolve(it, false)).onGenerateContent(environment).forEach {
        FileUtils.writeByteArrayToFile(File(options.output, it.path), it.content).apply {
          if (it.has(SentoBindingContentGenerator.EXTRA_BINDING_SPEC)) {
            bindings.add(it.extra<SentoBindingSpec>(SentoBindingContentGenerator.EXTRA_BINDING_SPEC))
          }
        }
      }
    }

    factory.createFactory(bindings).onGenerateContent(environment).forEach {
      FileUtils.writeByteArrayToFile(File(options.output, it.path), it.content)
    }
  }
}
