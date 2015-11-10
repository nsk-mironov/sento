package io.sento.compiler.bindings.fields

import io.sento.Optional
import io.sento.compiler.GeneratedContent
import io.sento.compiler.GenerationEnvironment
import io.sento.compiler.common.Annotations
import io.sento.compiler.common.Types
import org.objectweb.asm.Opcodes

internal class BindViewBindingGenerator : FieldBindingGenerator {
  override fun bind(context: FieldBindingContext, environment: GenerationEnvironment): List<GeneratedContent> {
    val visitor = context.visitor
    val annotation = context.annotation

    val field = context.field
    val clazz = context.clazz

    val optional = field.getAnnotation<Optional>() != null
    val isInterface = environment.registry.isInterface(field.type)
    val isView = environment.registry.isSubclassOf(field.type, Types.TYPE_VIEW)

    if (!isInterface && !isView) {
      throw RuntimeException("${field.type.className} isn't a subclass of ${Types.TYPE_VIEW.className}")
    }

    visitor.visitVarInsn(Opcodes.ALOAD, context.variable("target"))
    visitor.visitVarInsn(Opcodes.ALOAD, context.variable("finder"))
    visitor.visitLdcInsn(Annotations.id(annotation))
    visitor.visitVarInsn(Opcodes.ALOAD, context.variable("source"))
    visitor.visitInsn(if (optional) Opcodes.ICONST_1 else Opcodes.ICONST_0)

    visitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, Types.TYPE_FINDER.internalName, "find", "(IL${Types.TYPE_OBJECT.internalName};Z)L${Types.TYPE_VIEW.internalName};", true)
    visitor.visitTypeInsn(Opcodes.CHECKCAST, field.type.internalName)
    visitor.visitFieldInsn(Opcodes.PUTFIELD, clazz.type.internalName, field.name, field.type.descriptor)

    return emptyList()
  }

  override fun unbind(context: FieldBindingContext, environment: GenerationEnvironment): List<GeneratedContent> {
    val visitor = context.visitor
    val field = context.field
    val clazz = context.clazz

    visitor.visitVarInsn(Opcodes.ALOAD, context.variable("target"))
    visitor.visitInsn(Opcodes.ACONST_NULL)
    visitor.visitFieldInsn(Opcodes.PUTFIELD, clazz.type.internalName, field.name, field.type.descriptor)

    return emptyList()
  }
}
