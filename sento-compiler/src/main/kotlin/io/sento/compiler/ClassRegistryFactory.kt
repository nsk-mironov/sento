package io.sento.compiler

import io.sento.compiler.api.ClassRegistry
import io.sento.compiler.common.Types
import io.sento.compiler.model.ClassRef
import io.sento.compiler.visitors.ClassSpecVisitor
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.IOUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Type
import java.util.zip.ZipFile

internal object  ClassRegistryFactory {
  public fun from(options: SentoOptions): ClassRegistry {
    val builder = ClassRegistry.Builder()

    options.libs.forEach {
      if (it.isDirectory) {
        FileUtils.iterateFiles(it, arrayOf("class"), true).forEach {
          val bytes = FileUtils.readFileToByteArray(it)
          val reader = ClassReader(bytes)

          val type = Type.getObjectType(reader.className)
          val parent = Type.getObjectType(reader.superName)

          builder.reference(ClassRef(type, parent))
        }
      }

      if (it.isFile && FilenameUtils.getExtension(it.absolutePath) == "jar") {
        ZipFile(it).apply {
          for (entry in entries()) {
            if (FilenameUtils.getExtension(entry.name) == "class") {
              val bytes = IOUtils.toByteArray(getInputStream(entry))
              val reader = ClassReader(bytes)

              val parent = Type.getObjectType(reader.superName ?: Types.TYPE_OBJECT.internalName)
              val type = Type.getObjectType(reader.className)

              builder.reference(ClassRef(type, parent))
            }
          }
        }
      }
    }

    FileUtils.iterateFiles(options.input, arrayOf("class"), true).forEach {
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
}
