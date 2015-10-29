package com.github.vmironov.sento.compiler

import com.github.vmironov.sento.compiler.generators.BindingGenerator
import com.github.vmironov.sento.compiler.generators.DefaultBindingGenerator
import org.apache.commons.io.FileUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Type
import java.io.File

public class SentoCompiler() {
  public fun compile(options: SentoOptions) {
    println("input ${options.input}")
    println("output ${options.output}")
    println("incremental ${options.incremental}")
    println("dry ${options.dryRun}")

    val generator = createBindingGenerator()
    val registry = createRegistry(options.input, generator)

    registry.classes.forEach {
      if (generator.shouldGenerateBinding(it, registry)) {
        val bytecode = generator.onGenerate(it, registry)
        val file = File(options.output, "${it.type.internalName}\$\$SentoBinding.class")

        FileUtils.writeByteArrayToFile(file, bytecode)
      }
    }

    FileUtils.copyDirectory(options.input, options.output)
  }

  private fun createRegistry(directory: File, generator: BindingGenerator): SentoRegistry {
    val builder = SentoRegistry.Builder()

    FileUtils.iterateFiles(directory, arrayOf("class"), true).forEach {
      val bytes = FileUtils.readFileToByteArray(it)
      val reader = ClassReader(bytes)

      val type = Type.getObjectType(reader.className)
      val parent = Type.getObjectType(reader.superName)

      reader.accept(SentoClassVisitor(type, parent, generator) {
        builder.spec(it)
      }, 0)
    }

    return builder.build()
  }

  private fun createBindingGenerator(): BindingGenerator {
    return DefaultBindingGenerator()
  }
}
