package io.sento.compiler.bindings.fields

import io.sento.compiler.GenerationEnvironment
import io.sento.compiler.common.Annotations
import io.sento.compiler.common.Types
import org.objectweb.asm.Opcodes

internal class BindStringBindingGenerator : SimpleFieldBindingGenerator() {
  override fun onBind(context: FieldBindingContext, environment: GenerationEnvironment) {
    val visitor = context.visitor
    val annotation = context.annotation

    val field = context.field
    val clazz = context.clazz

    visitor.visitVarInsn(Opcodes.ALOAD, context.variable("target"))
    visitor.visitVarInsn(Opcodes.ALOAD, context.variable("finder"))
    visitor.visitVarInsn(Opcodes.ALOAD, context.variable("source"))

    visitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, Types.TYPE_FINDER.internalName, "resources", "(L${Types.TYPE_OBJECT.internalName};)L${Types.TYPE_RESOURCES.internalName};", true)
    visitor.visitLdcInsn(Annotations.id(annotation))

    when (field.type) {
      Types.TYPE_STRING -> {
        visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Types.TYPE_RESOURCES.internalName, "getString", "(I)L${Types.TYPE_STRING.internalName};", false)
        visitor.visitFieldInsn(Opcodes.PUTFIELD, clazz.type.internalName, field.name, field.type.descriptor)
      }

      Types.TYPE_CHAR_SEQUENCE -> {
        visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Types.TYPE_RESOURCES.internalName, "getText", "(I)L${Types.TYPE_CHAR_SEQUENCE.internalName};", false)
        visitor.visitFieldInsn(Opcodes.PUTFIELD, clazz.type.internalName, field.name, field.type.descriptor)
      }

      else -> {
        environment.fatal("Unsupported filed type \"${field.type.className}\" for @BindString")
      }
    }
  }
}
