package com.github.vmironov.sento.compiler

import com.google.common.io.Files
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Type
import java.io.File

public class SentoCompiler() {
  public fun compile(options: SentoOptions) {
    println("input ${options.input}")
    println("output ${options.output}")
    println("incremental ${options.incremental}")
    println("dry ${options.dryRun}")

    createSentoRegistry(options.input).classes.forEach {
      println("class ${it.type}")

      it.fields.forEach {
        println("    field $it")
      }

      it.methods.forEach {
        println("    method $it")
      }
    }
  }

  private fun createSentoRegistry(directory: File): SentoRegistry {
    val builder = SentoRegistry.Builder()

    Files.fileTreeTraverser().preOrderTraversal(directory).forEach {
      if (it.isFile && Files.getFileExtension(it.absolutePath) == "class") {
        val bytes = Files.toByteArray(it)
        val reader = ClassReader(bytes)

        val type = Type.getObjectType(reader.className)
        val parent = Type.getObjectType(reader.superName)

        reader.accept(SentoClassSpecVisitor(type, parent) {
          builder.spec(it)
        }, 0)
      }
    }

    return builder.build()
  }
}
