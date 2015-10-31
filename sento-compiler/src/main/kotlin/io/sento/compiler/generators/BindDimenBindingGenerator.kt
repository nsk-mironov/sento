package io.sento.compiler.generators

import io.sento.BindDimen
import io.sento.compiler.GenerationEnvironment
import io.sento.compiler.common.Types
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

public class BindDimenBindingGenerator : FieldBindingGenerator<BindDimen> {
  override fun bind(context: FieldBindingContext<BindDimen>, environment: GenerationEnvironment) {
    val visitor = context.visitor
    val annotation = context.annotation

    val field = context.field
    val clazz = context.clazz

    visitor.visitVarInsn(Opcodes.ALOAD, context.variable("target"))
    visitor.visitVarInsn(Opcodes.ALOAD, context.variable("finder"))
    visitor.visitVarInsn(Opcodes.ALOAD, context.variable("source"))

    visitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, Types.TYPE_FINDER.internalName, "resources", "(L${Types.TYPE_OBJECT.internalName};)L${Types.TYPE_RESOURCES.internalName};", true);
    visitor.visitLdcInsn(annotation.value);

    when (field.type) {
      Type.FLOAT_TYPE -> {
        visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Types.TYPE_RESOURCES.internalName, "getDimension", "(I)F", false);
        visitor.visitFieldInsn(Opcodes.PUTFIELD, clazz.type.internalName, field.name, field.type.descriptor);
      }

      Type.INT_TYPE -> {
        visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Types.TYPE_RESOURCES.internalName, "getDimensionPixelSize", "(I)I", false);
        visitor.visitFieldInsn(Opcodes.PUTFIELD, clazz.type.internalName, field.name, field.type.descriptor);
      }

      else -> {
        environment.error("Unsupported type \"${field.type.className}\" for @${BindDimen::class.java.simpleName}")
      }
    }
  }
}
