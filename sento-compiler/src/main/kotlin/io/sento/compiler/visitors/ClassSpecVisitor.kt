package io.sento.compiler.visitors

import io.sento.compiler.Opener
import io.sento.compiler.common.Types
import io.sento.compiler.model.ClassSpec
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

internal class ClassSpecVisitor(
    private val access: Int,
    private val type: Type,
    private val parent: Type,
    private val opener: Opener,
    private val action: (ClassSpec) -> Unit
) : ClassVisitor(Opcodes.ASM5) {
  private val builder = ClassSpec.Builder(access, type, parent, opener)

  override fun visitAnnotation(desc: String, visible: Boolean): AnnotationVisitor {
    return AnnotationSpecVisitor(Type.getType(desc)) {
      if (!Types.isSystemClass(it.type)) {
        builder.annotation(it)
      }
    }
  }

  override fun visitMethod(access: Int, name: String, desc: String, signature: String?, exceptions: Array<out String>?): MethodVisitor {
    return MethodSpecVisitor(access, name, Type.getType(desc)) {
      builder.method(it)
    }
  }

  override fun visitField(access: Int, name: String, desc: String, signature: String?, value: Any?): FieldVisitor {
    return FieldSpecVisitor(access, name, Type.getType(desc)) {
      builder.field(it)
    }
  }

  override fun visitEnd() {
    action(builder.build())
  }
}
