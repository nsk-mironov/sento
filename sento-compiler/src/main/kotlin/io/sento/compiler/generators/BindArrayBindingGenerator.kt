package io.sento.compiler.generators

import io.sento.BindArray
import io.sento.compiler.GenerationEnvironment
import io.sento.compiler.common.Types
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

public class BindArrayBindingGenerator : FieldBindingGenerator<BindArray> {
  override fun bind(context: FieldBindingContext<BindArray>, environment: GenerationEnvironment) {
    val visitor = context.visitor
    val annotation = context.annotation

    val field = context.field
    val clazz = context.clazz

    visitor.visitVarInsn(Opcodes.ALOAD, context.variable("target"))
    visitor.visitVarInsn(Opcodes.ALOAD, context.variable("finder"))
    visitor.visitVarInsn(Opcodes.ALOAD, context.variable("source"))

    visitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, Types.TYPE_FINDER.internalName, "resources", "(L${Types.TYPE_OBJECT.internalName};)L${Types.TYPE_RESOURCES.internalName};", true)
    visitor.visitLdcInsn(annotation.value)

    if (field.type.sort != Type.ARRAY) {
      return environment.fatal("@BindArray should be used only with arrays")
    }

    when (field.type.elementType) {
      Types.TYPE_INT -> {
        visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Types.TYPE_RESOURCES.internalName, "getIntArray", "(I)[I", false)
        visitor.visitFieldInsn(Opcodes.PUTFIELD, clazz.type.internalName, field.name, field.type.descriptor)
      }

      Types.TYPE_STRING -> {
        visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Types.TYPE_RESOURCES.internalName, "getStringArray", "(I)[Ljava/lang/String;", false)
        visitor.visitFieldInsn(Opcodes.PUTFIELD, clazz.type.internalName, field.name, field.type.descriptor)
      }

      Types.TYPE_CHAR_SEQUENCE -> {
        visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Types.TYPE_RESOURCES.internalName, "getTextArray", "(I)[Ljava/lang/CharSequence;", false)
        visitor.visitFieldInsn(Opcodes.PUTFIELD, clazz.type.internalName, field.name, field.type.descriptor)
      }

      else -> {
        environment.fatal("Unsupported filed type \"${field.type.className}\" for @BindArray")
      }
    }
  }
}
