package com.github.vmironov.sento.compiler

import com.github.vmironov.sento.compiler.generators.BindingGenerator
import com.github.vmironov.sento.compiler.specs.ClassSpec
import com.github.vmironov.sento.compiler.visitors.AnnotationSpecVisitor
import com.github.vmironov.sento.compiler.visitors.FieldSpecVisitor
import com.github.vmironov.sento.compiler.visitors.MethodSpecVisitor
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

internal class SentoClassVisitor(val type: Type, val parent: Type, val generator: BindingGenerator, val action: (ClassSpec) -> Unit) : ClassVisitor(Opcodes.ASM5) {
  private val builder = ClassSpec.Builder(type, parent)

  override fun visitAnnotation(desc: String, visible: Boolean): AnnotationVisitor {
    return AnnotationSpecVisitor(Type.getType(desc)) {
      if (generator.shouldAcceptClassAnnotation(it)) {
        builder.annotation(it)
      }
    }
  }

  override fun visitMethod(access: Int, name: String, desc: String, signature: String?, exceptions: Array<out String>?): MethodVisitor {
    return MethodSpecVisitor(name, Type.getType(desc)) {
      if (generator.shouldAcceptClassMethod(it)) {
        builder.method(it)
      }
    }
  }

  override fun visitField(access: Int, name: String, desc: String, signature: String?, value: Any?): FieldVisitor {
    return FieldSpecVisitor(name, Type.getType(desc)) {
      if (generator.shouldAcceptClassField(it)) {
        builder.field(it)
      }
    }
  }

  override fun visitEnd() {
    builder.build().apply {
      if (generator.shouldAcceptClass(this)) {
        action(this)
      }
    }
  }
}
