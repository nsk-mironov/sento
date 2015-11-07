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

internal object ClassRegistryFactory {
  private const val EXTENSION_CLASS = "class"
  private const val EXTENSION_JAR = "jar"

  public fun create(options: SentoOptions): ClassRegistry {
    val builder = ClassRegistry.Builder()
    val references = options.libs + options.input

    references.forEach {
      if (it.isDirectory) {
        FileUtils.iterateFiles(it, arrayOf(EXTENSION_CLASS), true).forEach {
          builder.reference(asClassRef(FileUtils.readFileToByteArray(it)))
        }
      }

      if (it.isFile && FilenameUtils.getExtension(it.absolutePath) == EXTENSION_JAR) {
        ZipFile(it).apply {
          for (entry in entries()) {
            if (FilenameUtils.getExtension(entry.name) == EXTENSION_CLASS) {
              builder.reference(asClassRef(IOUtils.toByteArray(getInputStream(entry))))
            }
          }
        }
      }
    }

    FileUtils.iterateFiles(options.input, arrayOf(EXTENSION_CLASS), true).forEach {
      val reader = ClassReader(FileUtils.readFileToByteArray(it))

      val type = Type.getObjectType(reader.className)
      val parent = Type.getObjectType(reader.superName)

      reader.accept(ClassSpecVisitor(it, type, parent) {
        builder.spec(it)
      }, 0)
    }

    return builder.build()
  }

  private fun asClassRef(bytes: ByteArray): ClassRef {
    val reader = ClassReader(bytes)

    val parent = Type.getObjectType(reader.superName ?: Types.TYPE_OBJECT.internalName)
    val type = Type.getObjectType(reader.className)

    return ClassRef(type, parent, reader.access)
  }
}
