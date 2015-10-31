package io.sento.compiler.visitors

import io.sento.compiler.specs.AnnotationSpec
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

internal class AnnotationSpecVisitor(val type: Type, val action: (AnnotationSpec) -> Unit) : AnnotationVisitor(Opcodes.ASM5) {
  private val builder = AnnotationSpec.Builder(type)

  override fun visit(name: String, value: Any?) {
    builder.value(name, value)
  }

  override fun visitEnd() {
    action(builder.build())
  }
}
