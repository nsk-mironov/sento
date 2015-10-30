package com.github.vmironov.sento.compiler

import com.github.vmironov.sento.compiler.generators.BytecodeGenerator
import com.github.vmironov.sento.compiler.generators.BindingBytecodeGenerator
import com.github.vmironov.sento.compiler.visitors.ClassSpecVisitor
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

    val registry = createClassRegistry(options.input)
    val generator = createBytecodeGenerator()

    registry.classes.forEach {
      if (generator.shouldGenerateBytecode(it, registry)) {
        val bytecode = generator.onGenerateBytecode(it, registry)
        val file = File(options.output, "${it.type.internalName}\$\$SentoBinding.class")

        FileUtils.writeByteArrayToFile(file, bytecode)
      }
    }

    FileUtils.copyDirectory(options.input, options.output)
  }

  private fun createClassRegistry(directory: File): ClassRegistry {
    val builder = ClassRegistry.Builder()

    FileUtils.iterateFiles(directory, arrayOf("class"), true).forEach {
      val bytes = FileUtils.readFileToByteArray(it)
      val reader = ClassReader(bytes)

      val type = Type.getObjectType(reader.className)
      val parent = Type.getObjectType(reader.superName)

      reader.accept(ClassSpecVisitor(type, parent) {
        builder.spec(it)
      }, 0)
    }

    return builder.build()
  }

  private fun createBytecodeGenerator(): BytecodeGenerator {
    return BindingBytecodeGenerator()
  }
}
