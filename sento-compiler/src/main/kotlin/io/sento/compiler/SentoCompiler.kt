package io.sento.compiler

import io.sento.compiler.bindings.ContentGeneratorFactory
import io.sento.compiler.bindings.SentoBindingContentGenerator
import io.sento.compiler.common.Naming
import io.sento.compiler.reflection.ClassSpec
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import java.io.File
import java.util.ArrayList

public class SentoCompiler() {
  private val logger = LoggerFactory.getLogger(SentoCompiler::class.java)

  init {
    // TODO: fix me https://github.com/nsk-mironov/sento/issues/39
    Naming.initialize()
  }

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
    val environment = GenerationEnvironment(registry)

    logger.info("Successfully created class registry:")
    logger.info("Referenced classes count: {}", registry.references.size)
    logger.info("Input classes count: {}", registry.inputs.size)

    val factory = ContentGeneratorFactory.from(environment)
    val bindings = ArrayList<ClassSpec>()

    options.inputs.forEach {
      logger.info("Copying files from {} to {}", it.absolutePath, options.output.absolutePath)
      FileUtils.copyDirectory(it, options.output)
    }

    registry.inputs.forEach {
      factory.createBinding(registry.resolve(it, false)).generate(environment).forEach {
        logger.info("Writing generated class {}", File(options.output, it.path))

        if (it.has(SentoBindingContentGenerator.EXTRA_BINDING_SPEC)) {
          bindings.add(it.extra<ClassSpec>(SentoBindingContentGenerator.EXTRA_BINDING_SPEC))
        }

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
