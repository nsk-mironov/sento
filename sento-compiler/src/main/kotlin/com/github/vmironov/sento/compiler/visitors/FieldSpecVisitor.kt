package com.github.vmironov.sento.compiler.visitors

import com.github.vmironov.sento.compiler.specs.FieldSpec
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

public class FieldSpecVisitor(
    public val name: String,
    public val type: Type,
    public val action: (FieldSpec) -> Unit
) : FieldVisitor(Opcodes.ASM5) {
  private val builder = FieldSpec.Builder(name, type)

  override fun visitAnnotation(desc: String, visible: Boolean): AnnotationVisitor {
    return AnnotationSpecVisitor(Type.getType(desc)) {
      builder.annotation(it)
    }
  }

  override fun visitEnd() {
    action(builder.build())
  }
}
