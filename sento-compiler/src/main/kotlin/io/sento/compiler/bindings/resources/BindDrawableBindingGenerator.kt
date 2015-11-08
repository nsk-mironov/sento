package io.sento.compiler.bindings.resources

import io.sento.BindDrawable
import io.sento.compiler.bindings.FieldBindingContext
import io.sento.compiler.GenerationEnvironment
import io.sento.compiler.bindings.SimpleFieldBindingGenerator
import io.sento.compiler.common.Types
import org.objectweb.asm.Opcodes

internal class BindDrawableBindingGenerator : SimpleFieldBindingGenerator<BindDrawable>() {
  override fun onBind(context: FieldBindingContext<BindDrawable>, environment: GenerationEnvironment) {
    val visitor = context.visitor
    val annotation = context.annotation

    val field = context.field
    val clazz = context.clazz

    visitor.visitVarInsn(Opcodes.ALOAD, context.variable("target"))
    visitor.visitVarInsn(Opcodes.ALOAD, context.variable("finder"))
    visitor.visitVarInsn(Opcodes.ALOAD, context.variable("source"))

    visitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, Types.TYPE_FINDER.internalName, "resources", "(L${Types.TYPE_OBJECT.internalName};)L${Types.TYPE_RESOURCES.internalName};", true)
    visitor.visitLdcInsn(annotation.value)

    visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Types.TYPE_RESOURCES.internalName, "getDrawable", "(I)L${Types.TYPE_DRAWABLE.internalName};", false)
    visitor.visitTypeInsn(Opcodes.CHECKCAST, field.type.internalName)
    visitor.visitFieldInsn(Opcodes.PUTFIELD, clazz.type.internalName, field.name, field.type.descriptor)
  }
}
