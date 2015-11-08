package io.sento.compiler

import io.sento.MethodBinding
import io.sento.compiler.common.AnnotationProxy
import io.sento.compiler.common.Types
import io.sento.compiler.model.ClassReference
import io.sento.compiler.visitors.AnnotationSpecVisitor
import io.sento.compiler.visitors.ClassSpecVisitor
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.IOUtils
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import java.util.zip.ZipFile

internal object ClassRegistryFactory {
  private const val EXTENSION_CLASS = "class"
  private const val EXTENSION_JAR = "jar"

  public fun create(options: SentoOptions): ClassRegistry {
    val builder = ClassRegistry.Builder()

    for (file in options.libs + options.input) {
      if (file.isFile && FilenameUtils.getExtension(file.absolutePath) == EXTENSION_JAR) {
        ZipFile(file).use {
          for (entry in it.entries()) {
            if (FilenameUtils.getExtension(entry.name) == EXTENSION_CLASS) {
              onProcessReferencedClass(builder, JarOpener(file, entry.name), it.getInputStream(entry).use {
                IOUtils.toByteArray(it)
              })
            }
          }
        }
      }

      if (file.isDirectory) {
        FileUtils.iterateFiles(file, arrayOf(EXTENSION_CLASS), true).forEach {
          onProcessReferencedClass(builder, FileOpener(file), FileUtils.readFileToByteArray(it))
        }
      }
    }

    for (reference in builder.references) {
      if (reference.isAnnotation && !Types.isSystemClass(reference.type)) {
        val reader = ClassReader(reference.opener.open())
        val type = reference.type

        reader.accept(object : ClassVisitor(Opcodes.ASM5) {
          override fun visitAnnotation(desc: String, visible: Boolean): AnnotationVisitor? {
            if (Type.getType(desc) == Type.getType(MethodBinding::class.java)) {
              return AnnotationSpecVisitor(Type.getType(desc)) {
                println("annotation ${type.className}, values = ${AnnotationProxy.create<MethodBinding>(it.values)}")
              }
            }

            return super.visitAnnotation(desc, visible)
          }
        }, 0)
      }
    }

    FileUtils.iterateFiles(options.input, arrayOf(EXTENSION_CLASS), true).forEach {
      val reader = ClassReader(FileUtils.readFileToByteArray(it))

      val type = Type.getObjectType(reader.className)
      val parent = Type.getObjectType(reader.superName)

      reader.accept(ClassSpecVisitor(type, parent, FileOpener(it)) {
        builder.spec(it)
      }, 0)
    }

    return builder.build()
  }

  private fun onProcessReferencedClass(builder: ClassRegistry.Builder, opener: Opener, bytes: ByteArray) {
    val reader = ClassReader(bytes)

    val parent = Type.getObjectType(reader.superName ?: Types.TYPE_OBJECT.internalName)
    val type = Type.getObjectType(reader.className)

    builder.reference(ClassReference(type, parent, reader.access, opener))
  }
}
