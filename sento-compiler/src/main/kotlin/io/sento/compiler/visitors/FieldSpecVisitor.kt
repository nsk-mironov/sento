package io.sento.compiler.visitors

import io.sento.compiler.common.Types
import io.sento.compiler.model.FieldSpec
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

internal class FieldSpecVisitor(
    private val name: String,
    private val type: Type,
    private val action: (FieldSpec) -> Unit
) : FieldVisitor(Opcodes.ASM5) {
  private val builder = FieldSpec.Builder(name, type)

  override fun visitAnnotation(desc: String, visible: Boolean): AnnotationVisitor {
    return AnnotationSpecVisitor(Type.getType(desc)) {
      if (!Types.isSystemClass(it.type)) {
        builder.annotation(it)
      }
    }
  }

  override fun visitEnd() {
    action(builder.build())
  }
}
