package io.sento.compiler.bindings.resources

import io.sento.BindColor
import io.sento.compiler.bindings.FieldBindingContext
import io.sento.compiler.bindings.FieldBindingGenerator
import io.sento.compiler.api.GenerationEnvironment
import io.sento.compiler.common.Types
import org.objectweb.asm.Opcodes

internal class BindColorBindingGenerator : FieldBindingGenerator<BindColor> {
  override fun bind(context: FieldBindingContext<BindColor>, environment: GenerationEnvironment) {
    val visitor = context.visitor
    val annotation = context.annotation

    val field = context.field
    val clazz = context.clazz

    visitor.visitVarInsn(Opcodes.ALOAD, context.variable("target"))
    visitor.visitVarInsn(Opcodes.ALOAD, context.variable("finder"))
    visitor.visitVarInsn(Opcodes.ALOAD, context.variable("source"))

    visitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, Types.TYPE_FINDER.internalName, "resources", "(L${Types.TYPE_OBJECT.internalName};)L${Types.TYPE_RESOURCES.internalName};", true)
    visitor.visitLdcInsn(annotation.value)

    when (field.type) {
      Types.TYPE_INT -> {
        visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Types.TYPE_RESOURCES.internalName, "getColor", "(I)I", false)
        visitor.visitFieldInsn(Opcodes.PUTFIELD, clazz.type.internalName, field.name, field.type.descriptor)
      }

      Types.TYPE_COLOR_STATE_LIST -> {
        visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Types.TYPE_RESOURCES.internalName, "getColorStateList", "(I)L${Types.TYPE_COLOR_STATE_LIST.internalName};", false)
        visitor.visitFieldInsn(Opcodes.PUTFIELD, clazz.type.internalName, field.name, field.type.descriptor)
      }

      else -> {
        environment.fatal("Unsupported filed type \"${field.type.className}\" for @BindColor")
      }
    }
  }
}
