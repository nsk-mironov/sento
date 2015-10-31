package io.sento.compiler.generators

import io.sento.BindString
import io.sento.compiler.GenerationEnvironment
import io.sento.compiler.common.Types
import org.objectweb.asm.Opcodes

public class BindStringBindingGenerator : FieldBindingGenerator<BindString> {
  override fun bind(context: FieldBindingContext<BindString>, environment: GenerationEnvironment) {
    val visitor = context.visitor
    val annotation = context.annotation

    val field = context.field
    val clazz = context.clazz

    visitor.visitVarInsn(Opcodes.ALOAD, context.variable("target"))
    visitor.visitVarInsn(Opcodes.ALOAD, context.variable("finder"))
    visitor.visitVarInsn(Opcodes.ALOAD, context.variable("source"))

    visitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, Types.TYPE_FINDER.internalName, "resources", "(L${Types.TYPE_OBJECT.internalName};)L${Types.TYPE_RESOURCES.internalName};", true);
    visitor.visitLdcInsn(annotation.value);

    visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Types.TYPE_RESOURCES.internalName, "getString", "(I)Ljava/lang/String;", false);
    visitor.visitFieldInsn(Opcodes.PUTFIELD, clazz.type.internalName, field.name, field.type.descriptor);
  }
}
