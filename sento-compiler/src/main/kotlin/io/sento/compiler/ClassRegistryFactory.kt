package io.sento.compiler

import io.sento.compiler.common.Types
import io.sento.compiler.model.ClassReference
import io.sento.compiler.model.ClassSpec
import io.sento.compiler.visitors.ClassSpecVisitor
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.IOUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Type
import java.util.ArrayList
import java.util.zip.ZipFile

internal object ClassRegistryFactory {
  private const val EXTENSION_CLASS = "class"
  private const val EXTENSION_JAR = "jar"

  public fun create(options: SentoOptions): ClassRegistry {
    return with (ClassRegistry.Builder()) {
      val references = createClassReferencesRegistry(options)
      val annotations = createAnnotationSpecsRegistry(references)
      val specs = createClassSpecsRegistry(options)

      references.forEach {
        reference(it)
      }

      annotations.forEach {
        println("annotation $it")
        annotation(it)
      }

      specs.forEach {
        spec(it)
      }

      build()
    }
  }

  private fun createClassReferencesRegistry(options: SentoOptions): Collection<ClassReference> {
    val result = ArrayList<ClassReference>()

    for (file in options.libs + options.input) {
      if (file.isFile && FilenameUtils.getExtension(file.absolutePath) == EXTENSION_JAR) {
        ZipFile(file).use {
          for (entry in it.entries()) {
            if (FilenameUtils.getExtension(entry.name) == EXTENSION_CLASS) {
              result.add(createClassReference(JarOpener(file, entry.name), it.getInputStream(entry).use {
                IOUtils.toByteArray(it)
              }))
            }
          }
        }
      }

      if (file.isDirectory) {
        FileUtils.iterateFiles(file, arrayOf(EXTENSION_CLASS), true).forEach {
          result.add(createClassReference(FileOpener(file), FileUtils.readFileToByteArray(it)))
        }
      }
    }

    return result
  }

  private fun createAnnotationSpecsRegistry(references: Collection<ClassReference>): Collection<ClassSpec> {
    return ArrayList<ClassSpec>().apply {
      references.forEach {
        if (it.isAnnotation && !Types.isSystemClass(it.type)) {
          ClassReader(it.opener.open()).accept(ClassSpecVisitor(it.type, it.parent, it.opener) {
            add(it)
          }, 0)
        }
      }
    }
  }

  private fun createClassSpecsRegistry(options: SentoOptions): Collection<ClassSpec> {
    return ArrayList<ClassSpec>().apply {
      FileUtils.iterateFiles(options.input, arrayOf(EXTENSION_CLASS), true).forEach {
        val reader = ClassReader(FileUtils.readFileToByteArray(it))

        val type = Type.getObjectType(reader.className)
        val parent = Type.getObjectType(reader.superName)

        reader.accept(ClassSpecVisitor(type, parent, FileOpener(it)) {
          add(it)
        }, 0)
      }
    }
  }

  private fun createClassReference(opener: Opener, bytes: ByteArray): ClassReference {
    val reader = ClassReader(bytes)

    val parent = Type.getObjectType(reader.superName ?: Types.TYPE_OBJECT.internalName)
    val type = Type.getObjectType(reader.className)

    return ClassReference(type, parent, reader.access, opener)
  }
}
