package io.sento.compiler.generators

import io.sento.Bind
import io.sento.compiler.GenerationEnvironment
import io.sento.compiler.common.Types
import org.objectweb.asm.Opcodes

public class BindViewBindingGenerator : FieldBindingGenerator<Bind> {
  override fun bind(context: FieldBindingContext<Bind>, environment: GenerationEnvironment) {
    val visitor = context.visitor
    val annotation = context.annotation

    val field = context.field
    val clazz = context.clazz

    visitor.visitVarInsn(Opcodes.ALOAD, context.variable("target"))
    visitor.visitVarInsn(Opcodes.ALOAD, context.variable("finder"))
    visitor.visitLdcInsn(annotation.value)
    visitor.visitVarInsn(Opcodes.ALOAD, context.variable("source"))

    visitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, Types.TYPE_FINDER.internalName, "find", "(IL${Types.TYPE_OBJECT.internalName};)L${Types.TYPE_VIEW.internalName};", true)
    visitor.visitTypeInsn(Opcodes.CHECKCAST, field.type.internalName)
    visitor.visitFieldInsn(Opcodes.PUTFIELD, clazz.type.internalName, field.name, field.type.descriptor)
  }

  override fun unbind(context: FieldBindingContext<Bind>, environment: GenerationEnvironment) {
    val visitor = context.visitor
    val field = context.field
    val clazz = context.clazz

    visitor.visitVarInsn(Opcodes.ALOAD, context.variable("target"))
    visitor.visitInsn(Opcodes.ACONST_NULL)
    visitor.visitFieldInsn(Opcodes.PUTFIELD, clazz.type.internalName, field.name, field.type.descriptor)
  }
}
