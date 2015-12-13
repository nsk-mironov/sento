package io.sento.compiler

import io.sento.compiler.common.Naming
import io.sento.compiler.generator.ContentGeneratorFactory
import io.sento.compiler.model.BindingSpec
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import java.io.File

public class SentoCompiler() {
  private val logger = LoggerFactory.getLogger(SentoCompiler::class.java)

  public fun compile(options: SentoOptions) {
    logger.info("Starting sento compiler:").apply {
      options.libs.forEach {
        logger.info("Referenced classes - {}", it)
      }

      options.libs.forEach {
        logger.info("Input classes - {}", it)
      }

      logger.info("Output directory - {}", options.output)
    }

    val registry = ClassRegistryFactory.create(options)
    val environment = GenerationEnvironment(registry, Naming())

    logger.info("Successfully created class registry:")
    logger.info("Referenced classes count: {}", registry.references.size)
    logger.info("Input classes count: {}", registry.inputs.size)

    options.inputs.forEach {
      logger.info("Copying files from {} to {}", it.absolutePath, options.output.absolutePath)
      FileUtils.copyDirectory(it, options.output)
    }

    val factory = ContentGeneratorFactory.from(environment)
    val bindings = registry.inputs.map { BindingSpec.from(registry.resolve(it, false), environment) }.filter {
      !it.bindings.isEmpty() || !it.listeners.isEmpty() || !it.views.isEmpty()
    }

    bindings.forEach {
      factory.createBinding(it).generate(environment).forEach {
        logger.info("Writing generated class {}", File(options.output, it.path))
        FileUtils.writeByteArrayToFile(File(options.output, it.path), it.content)
      }
    }

    factory.createFactory(bindings).generate(environment).forEach {
      logger.info("Writing generated class - {}", File(options.output, it.path))
      FileUtils.writeByteArrayToFile(File(options.output, it.path), it.content)
    }

    logger.info("Successfully created bindings for {} classes", bindings.size)
  }
}
