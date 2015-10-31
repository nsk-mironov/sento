package io.sento.compiler

import io.sento.compiler.api.ClassRegistry
import io.sento.compiler.api.GenerationEnvironment
import io.sento.compiler.api.ContentGenerator
import io.sento.compiler.bindings.BindingContentGenerator
import io.sento.compiler.visitors.ClassSpecVisitor
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

    val environment = GenerationEnvironment()
    val registry = createClassRegistry(options.input)
    val generator = createBytecodeGenerator()

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

  private fun createClassRegistry(directory: File): ClassRegistry {
    val builder = ClassRegistry.Builder()

    FileUtils.iterateFiles(directory, arrayOf("class"), true).forEach {
      val bytes = FileUtils.readFileToByteArray(it)
      val reader = ClassReader(bytes)

      val type = Type.getObjectType(reader.className)
      val parent = Type.getObjectType(reader.superName)

      reader.accept(ClassSpecVisitor(it, type, parent) {
        builder.spec(it)
      }, 0)
    }

    return builder.build()
  }

  private fun createBytecodeGenerator(): ContentGenerator {
    return BindingContentGenerator()
  }
}
