package io.sento.compiler.visitors

import io.sento.compiler.common.Types
import io.sento.compiler.model.MethodSpec
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

internal class MethodSpecVisitor(
    private val access: Int,
    private val name: String,
    private val type: Type,
    private val action: (MethodSpec) -> Unit
) : MethodVisitor(Opcodes.ASM5) {
  private val builder = MethodSpec.Builder(access, name, type)

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
