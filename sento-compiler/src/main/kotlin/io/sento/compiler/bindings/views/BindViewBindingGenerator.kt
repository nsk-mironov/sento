package io.sento.compiler.bindings.views

import io.sento.Bind
import io.sento.Optional
import io.sento.compiler.bindings.FieldBindingContext
import io.sento.compiler.api.GenerationEnvironment
import io.sento.compiler.bindings.SimpleFieldBindingGenerator
import io.sento.compiler.common.Types
import org.objectweb.asm.Opcodes

internal class BindViewBindingGenerator : SimpleFieldBindingGenerator<Bind>() {
  override fun onBind(context: FieldBindingContext<Bind>, environment: GenerationEnvironment) {
    val visitor = context.visitor
    val annotation = context.annotation

    val field = context.field
    val clazz = context.clazz

    val optional = field.getAnnotation(Optional::class.java) != null

    if (!environment.registry.isSubclassOf(field.type, Types.TYPE_VIEW)) {
      throw RuntimeException("${field.type.className} isn't a subclass of ${Types.TYPE_VIEW.internalName}")
    }

    visitor.visitVarInsn(Opcodes.ALOAD, context.variable("target"))
    visitor.visitVarInsn(Opcodes.ALOAD, context.variable("finder"))
    visitor.visitLdcInsn(annotation.value)
    visitor.visitVarInsn(Opcodes.ALOAD, context.variable("source"))
    visitor.visitInsn(if (optional) Opcodes.ICONST_1 else Opcodes.ICONST_0)

    visitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, Types.TYPE_FINDER.internalName, "find", "(IL${Types.TYPE_OBJECT.internalName};Z)L${Types.TYPE_VIEW.internalName};", true)
    visitor.visitTypeInsn(Opcodes.CHECKCAST, field.type.internalName)
    visitor.visitFieldInsn(Opcodes.PUTFIELD, clazz.type.internalName, field.name, field.type.descriptor)
  }

  override fun onUnbind(context: FieldBindingContext<Bind>, environment: GenerationEnvironment) {
    val visitor = context.visitor
    val field = context.field
    val clazz = context.clazz

    visitor.visitVarInsn(Opcodes.ALOAD, context.variable("target"))
    visitor.visitInsn(Opcodes.ACONST_NULL)
    visitor.visitFieldInsn(Opcodes.PUTFIELD, clazz.type.internalName, field.name, field.type.descriptor)
  }
}
