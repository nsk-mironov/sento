package com.github.vmironov.sento.compiler

import com.google.common.io.Files
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Type

public class SentoCompiler() {
  public fun compile(options: SentoOptions) {
    println("input ${options.input}")
    println("output ${options.output}")
    println("incremental ${options.incremental}")
    println("dry ${options.dryRun}")

    Files.fileTreeTraverser().preOrderTraversal(options.input).forEach {
      if (it.isFile && Files.getFileExtension(it.absolutePath) == "class") {
        val bytes = Files.toByteArray(it)
        val reader = ClassReader(bytes)

        println("file ${it.absolutePath}")
        println("    ${reader.className}")
        println("    ${reader.superName}")

        val type = Type.getObjectType(reader.className)
        val parent = Type.getObjectType(reader.superName)

        reader.accept(SentoClassSpecVisitor(type, parent) {
          it.annotations.forEach {
            println("    annotation $it")
          }

          it.fields.forEach {
            println("    field $it")
          }

          it.methods.forEach {
            println("    method $it")
          }
        }, 0)
      }
    }
  }
}
