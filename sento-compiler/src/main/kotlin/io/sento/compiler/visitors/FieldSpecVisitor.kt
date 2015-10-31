package io.sento.compiler.visitors

import io.sento.compiler.specs.FieldSpec
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

internal class FieldSpecVisitor(
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
