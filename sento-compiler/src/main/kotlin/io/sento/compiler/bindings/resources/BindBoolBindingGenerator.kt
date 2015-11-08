package io.sento.compiler.bindings.resources

import io.sento.BindBool
import io.sento.compiler.bindings.FieldBindingContext
import io.sento.compiler.GenerationEnvironment
import io.sento.compiler.bindings.SimpleFieldBindingGenerator
import io.sento.compiler.common.Types
import org.objectweb.asm.Opcodes

internal class BindBoolBindingGenerator : SimpleFieldBindingGenerator<BindBool>() {
  override fun onBind(context: FieldBindingContext<BindBool>, environment: GenerationEnvironment) {
    val visitor = context.visitor
    val annotation = context.annotation

    val field = context.field
    val clazz = context.clazz

    visitor.visitVarInsn(Opcodes.ALOAD, context.variable("target"))
    visitor.visitVarInsn(Opcodes.ALOAD, context.variable("finder"))
    visitor.visitVarInsn(Opcodes.ALOAD, context.variable("source"))

    visitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, Types.TYPE_FINDER.internalName, "resources", "(L${Types.TYPE_OBJECT.internalName};)L${Types.TYPE_RESOURCES.internalName};", true)
    visitor.visitLdcInsn(annotation.value)

    visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Types.TYPE_RESOURCES.internalName, "getBoolean", "(I)Z", false)
    visitor.visitFieldInsn(Opcodes.PUTFIELD, clazz.type.internalName, field.name, field.type.descriptor)
  }
}
