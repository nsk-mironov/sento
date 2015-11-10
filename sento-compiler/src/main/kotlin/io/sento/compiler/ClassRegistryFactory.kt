package io.sento.compiler

import io.sento.compiler.common.FileOpener
import io.sento.compiler.common.JarOpener
import io.sento.compiler.common.Opener
import io.sento.compiler.common.Types
import io.sento.compiler.model.ClassReference
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.IOUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Type
import java.io.File
import java.util.ArrayList
import java.util.zip.ZipFile

internal object ClassRegistryFactory {
  private const val EXTENSION_CLASS = "class"
  private const val EXTENSION_JAR = "jar"

  public fun create(options: SentoOptions): ClassRegistry {
    return ClassRegistry.Builder()
        .inputs(createClassReferences(options.inputs))
        .references(createClassReferences(options.libs))
        .build()
  }

  private fun createClassReferences(files: Collection<File>): Collection<ClassReference> {
    return ArrayList<ClassReference>().apply {
      for (file in files) {
        if (file.isFile && FilenameUtils.getExtension(file.absolutePath) == EXTENSION_JAR) {
          ZipFile(file).use {
            for (entry in it.entries()) {
              if (FilenameUtils.getExtension(entry.name) == EXTENSION_CLASS) {
                add(createClassReference(JarOpener(file, entry.name), it.getInputStream(entry).use {
                  IOUtils.toByteArray(it)
                }))
              }
            }
          }
        }

        if (file.isDirectory) {
          FileUtils.iterateFiles(file, arrayOf(EXTENSION_CLASS), true).forEach {
            add(createClassReference(FileOpener(it), FileUtils.readFileToByteArray(it)))
          }
        }
      }
    }
  }

  private fun createClassReference(opener: Opener, bytes: ByteArray): ClassReference {
    val reader = ClassReader(bytes)

    val parent = Type.getObjectType(reader.superName ?: Types.TYPE_OBJECT.internalName)
    val type = Type.getObjectType(reader.className)

    val interfaces = reader.interfaces.orEmpty().map {
      Type.getObjectType(it)
    }

    return ClassReference(reader.access, type, parent, interfaces, opener)
  }
}
