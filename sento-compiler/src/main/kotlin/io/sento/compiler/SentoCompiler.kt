package io.sento.compiler

import io.sento.compiler.bindings.SentoBindingContentGenerator
import io.sento.compiler.bindings.SentoContentGeneratorFactory
import io.sento.compiler.model.SentoBindingSpec
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import java.io.File
import java.util.ArrayList

public class SentoCompiler() {
  private val logger = LoggerFactory.getLogger(SentoCompiler::class.java)

  public fun compile(options: SentoOptions) {
    logger.info("Starting sento compiler:").apply {
      options.libs.forEach {
        logger.info("\tReferenced classes - $it")
      }

      options.libs.forEach {
        logger.info("\tInput classes - $it")
      }

      logger.info("\tOutput directory - ${options.output}")
    }

    val registry = ClassRegistryFactory.create(options)
    val environment = GenerationEnvironment(registry)

    logger.info("Successfully created class registry:")
    logger.info("\tReferenced classes count: ${registry.references.size}")
    logger.info("\tInput classes count: ${registry.inputs.size}")

    val factory = SentoContentGeneratorFactory.from(environment)
    val bindings = ArrayList<SentoBindingSpec>()

    options.inputs.forEach {
      logger.info("Copying files from ${it.absolutePath} to ${options.output.absolutePath}")
      FileUtils.copyDirectory(it, options.output)
    }

    registry.inputs.forEach {
      factory.createBinding(registry.resolve(it, false)).generate(environment).forEach {
        logger.info("Writing generated class ${File(options.output, it.path)}")

        if (it.has(SentoBindingContentGenerator.EXTRA_BINDING_SPEC)) {
          bindings.add(it.extra<SentoBindingSpec>(SentoBindingContentGenerator.EXTRA_BINDING_SPEC))
        }

        FileUtils.writeByteArrayToFile(File(options.output, it.path), it.content)
      }
    }

    factory.createFactory(bindings).generate(environment).forEach {
      logger.info("Writing generated class - ${File(options.output, it.path)}")
      FileUtils.writeByteArrayToFile(File(options.output, it.path), it.content)
    }

    logger.info("Successfully created bindings for ${bindings.size} classes")
  }
}
