package io.sento.compiler.visitors

import io.sento.compiler.specs.MethodSpec
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

internal class MethodSpecVisitor(
    public val name: String,
    public val type: Type,
    public val action: (MethodSpec) -> Unit
) : MethodVisitor(Opcodes.ASM5) {
  private val builder = MethodSpec.Builder(name, type)

  override fun visitAnnotation(desc: String, visible: Boolean): AnnotationVisitor {
    return AnnotationSpecVisitor(Type.getType(desc)) {
      builder.annotation(it)
    }
  }

  override fun visitEnd() {
    action(builder.build())
  }
}
