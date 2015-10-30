package com.github.vmironov.sento.compiler.visitors

import com.github.vmironov.sento.compiler.specs.ClassSpec
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

internal class ClassSpecVisitor(val type: Type, val parent: Type, val action: (ClassSpec) -> Unit) : ClassVisitor(Opcodes.ASM5) {
  private val builder = ClassSpec.Builder(type, parent)

  override fun visitAnnotation(desc: String, visible: Boolean): AnnotationVisitor {
    return AnnotationSpecVisitor(Type.getType(desc)) {
        builder.annotation(it)
    }
  }

  override fun visitMethod(access: Int, name: String, desc: String, signature: String?, exceptions: Array<out String>?): MethodVisitor {
    return MethodSpecVisitor(name, Type.getType(desc)) {
      builder.method(it)
    }
  }

  override fun visitField(access: Int, name: String, desc: String, signature: String?, value: Any?): FieldVisitor {
    return FieldSpecVisitor(name, Type.getType(desc)) {
      builder.field(it)
    }
  }

  override fun visitEnd() {
    action(builder.build())
  }
}
