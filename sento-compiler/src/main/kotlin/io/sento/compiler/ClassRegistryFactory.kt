package io.sento.compiler

import io.sento.MethodBinding
import io.sento.compiler.api.ClassRegistry
import io.sento.compiler.common.AnnotationProxy
import io.sento.compiler.common.Types
import io.sento.compiler.model.ClassRef
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
    val references = options.libs + options.input

    references.forEach {
      if (it.isDirectory) {
        FileUtils.iterateFiles(it, arrayOf(EXTENSION_CLASS), true).forEach {
          onProcessReferencedClass(builder, FileUtils.readFileToByteArray(it))
        }
      }

      if (it.isFile && FilenameUtils.getExtension(it.absolutePath) == EXTENSION_JAR) {
        ZipFile(it).apply {
          for (entry in entries()) {
            if (FilenameUtils.getExtension(entry.name) == EXTENSION_CLASS) {
              onProcessReferencedClass(builder, IOUtils.toByteArray(getInputStream(entry)))
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

  private fun onProcessReferencedClass(builder: ClassRegistry.Builder, bytes: ByteArray) {
    val reader = ClassReader(bytes)

    val parent = Type.getObjectType(reader.superName ?: Types.TYPE_OBJECT.internalName)
    val type = Type.getObjectType(reader.className)

    if (reader.access and Opcodes.ACC_ANNOTATION != 0) {
      reader.accept(object : ClassVisitor(Opcodes.ASM5) {
        override fun visitAnnotation(desc: String?, visible: Boolean): AnnotationVisitor? {
          if (Type.getType(desc) == Type.getType(MethodBinding::class.java)) {
            return AnnotationSpecVisitor(Type.getType(desc)) {
              println("annotation ${type.className}, values = ${AnnotationProxy.create<MethodBinding>(it.values)}")
            }
          }

          return super.visitAnnotation(desc, visible)
        }
      }, 0)
    }

    builder.reference(ClassRef(type, parent, reader.access))
  }
}
