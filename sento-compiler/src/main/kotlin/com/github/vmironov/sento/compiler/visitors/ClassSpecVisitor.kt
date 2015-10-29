package com.github.vmironov.sento.compiler.visitors

import com.github.vmironov.sento.compiler.specs.AnnotationSpec
import com.github.vmironov.sento.compiler.specs.ClassSpec
import com.github.vmironov.sento.compiler.specs.FieldSpec
import com.github.vmironov.sento.compiler.specs.MethodSpec
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

public open class ClassSpecVisitor(val type: Type, val parent: Type, val action: (ClassSpec) -> Unit) : ClassVisitor(Opcodes.ASM5) {
  private val builder = ClassSpec.Builder(type, parent)

  override fun visitAnnotation(desc: String, visible: Boolean): AnnotationVisitor {
    return AnnotationSpecVisitor(Type.getType(desc)) {
      if (shouldAcceptAnnotation(it)) {
        builder.annotation(it)
      }
    }
  }

  override fun visitMethod(access: Int, name: String, desc: String, signature: String?, exceptions: Array<out String>?): MethodVisitor {
    return MethodSpecVisitor(name, Type.getType(desc)) {
      if (shouldAcceptMethod(it)) {
        builder.method(it)
      }
    }
  }

  override fun visitField(access: Int, name: String, desc: String, signature: String?, value: Any?): FieldVisitor {
    return FieldSpecVisitor(name, Type.getType(desc)) {
      if (shouldAcceptField(it)) {
        builder.field(it)
      }
    }
  }

  override fun visitEnd() {
    builder.build().apply {
      if (shouldAcceptClass(this)) {
        action(this)
      }
    }
  }

  protected open fun shouldAcceptAnnotation(annotation: AnnotationSpec): Boolean {
    return true
  }

  protected open fun shouldAcceptField(field: FieldSpec): Boolean {
    return true
  }

  protected open fun shouldAcceptMethod(method: MethodSpec): Boolean {
    return true
  }

  protected open fun shouldAcceptClass(clazz: ClassSpec): Boolean {
    return true
  }
}
