package io.sento.compiler.bindings

import io.sento.Optional
import io.sento.compiler.GeneratedContent
import io.sento.compiler.GenerationEnvironment
import io.sento.compiler.common.Types
import io.sento.compiler.common.toClassFilePath
import io.sento.compiler.common.toSourceFilePath
import io.sento.compiler.model.MethodSpec
import org.objectweb.asm.ClassVisitor

import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Label
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.Type

internal class MethodBindingGenerator {
  public fun bind(context: MethodBindingContext, environment: GenerationEnvironment): List<GeneratedContent> {
    val listener = ListenerSpec.from(context)
    val result = listOf(onCreateOnClickListener(listener, environment))

    val visitor = context.visitor
    val annotation = context.annotation

    val method = context.method
    val optional = method.getAnnotation<Optional>() != null

    annotation.value<IntArray>("value")?.forEach {
      visitor.visitVarInsn(ALOAD, context.variable("finder"))
      visitor.visitLdcInsn(it)
      visitor.visitVarInsn(ALOAD, context.variable("source"))
      visitor.visitInsn(if (optional) Opcodes.ICONST_1 else Opcodes.ICONST_0)
      visitor.visitMethodInsn(INVOKEINTERFACE, Types.TYPE_FINDER.internalName, "find", "(IL${Types.TYPE_OBJECT.internalName};Z)L${Types.TYPE_VIEW.internalName};", true)
      visitor.visitTypeInsn(NEW, listener.generatedType.internalName)
      visitor.visitInsn(DUP)
      visitor.visitVarInsn(ALOAD, context.variable("target"))
      visitor.visitMethodInsn(INVOKESPECIAL, listener.generatedType.internalName, "<init>", "(L${listener.generatedTarget.internalName};)V", false)
      visitor.visitMethodInsn(INVOKEVIRTUAL, listener.listenerOwner.internalName, listener.listenerSetter, "(L${listener.listenerType.internalName};)V", false)
    }

    return result
  }

  private fun onCreateOnClickListener(listener: ListenerSpec, environment: GenerationEnvironment): GeneratedContent {
    return GeneratedContent(listener.generatedType.toClassFilePath(), with (ClassWriter(0)) {
      visitListenerHeader(listener, environment)
      visitListenerFields(listener, environment)
      visitListenerConstructor(listener, environment)
      visitListenerOnClick(listener, environment)
      toByteArray()
    })
  }

  private fun ClassVisitor.visitListenerHeader(listener: ListenerSpec, environment: GenerationEnvironment) {
    visit(Opcodes.V1_6, ACC_PUBLIC + ACC_SUPER, listener.generatedType.internalName, null, Types.TYPE_OBJECT.internalName, arrayOf(listener.listenerType.internalName))
    visitSource(listener.generatedType.toSourceFilePath(), null)
  }

  private fun ClassVisitor.visitListenerFields(listener: ListenerSpec, environment: GenerationEnvironment) {
    visitField(ACC_PRIVATE + ACC_FINAL, "target", listener.generatedTarget.descriptor, null, null)
  }

  private fun ClassVisitor.visitListenerConstructor(listener: ListenerSpec, environment: GenerationEnvironment) {
    val visitor = visitMethod(ACC_PUBLIC, "<init>", "(L${listener.generatedTarget.internalName};)V", null, null)

    val start = Label()
    val end = Label()

    visitor.visitCode()
    visitor.visitLabel(start)
    visitor.visitVarInsn(ALOAD, 0)
    visitor.visitMethodInsn(INVOKESPECIAL, Types.TYPE_OBJECT.internalName, "<init>", "()V", false)
    visitor.visitVarInsn(ALOAD, 0)
    visitor.visitVarInsn(ALOAD, 1)
    visitor.visitFieldInsn(PUTFIELD, listener.generatedType.internalName, "target", listener.generatedTarget.descriptor)
    visitor.visitInsn(RETURN)
    visitor.visitLabel(end)
    visitor.visitLocalVariable("this", listener.generatedType.descriptor, null, start, end, 0)
    visitor.visitLocalVariable("target", listener.generatedTarget.descriptor, null, start, end, 1)
    visitor.visitMaxs(2, 2)
    visitor.visitEnd()
  }

  private fun ClassVisitor.visitListenerOnClick(listener: ListenerSpec, environment: GenerationEnvironment) {
    val visitor = visitMethod(ACC_PUBLIC, "onClick", "(L${Types.TYPE_VIEW.internalName};)V", null, null)

    val start = Label()
    val end = Label()

    visitor.visitCode()
    visitor.visitLabel(start)
    visitor.visitVarInsn(ALOAD, 0)
    visitor.visitFieldInsn(GETFIELD, listener.generatedType.internalName, "target", listener.generatedTarget.descriptor)
    visitor.visitVarInsn(ALOAD, 1)
    visitor.visitMethodInsn(INVOKEVIRTUAL, listener.generatedTarget.internalName, listener.method.name, "(L${Types.TYPE_VIEW.internalName};)V", false)
    visitor.visitInsn(RETURN)
    visitor.visitLabel(end)
    visitor.visitLocalVariable("this", listener.generatedType.descriptor, null, start, end, 0)
    visitor.visitLocalVariable("view", Types.TYPE_VIEW.descriptor, null, start, end, 1)
    visitor.visitMaxs(2, 2)
    visitor.visitEnd()
  }

  private class ListenerSpec(
      public val listenerType: Type,
      public val listenerOwner: Type,
      public val listenerSetter: String,

      public val generatedType: Type,
      public val generatedTarget: Type,

      public val method: MethodSpec
  ) {
    public companion object {
      public fun from(context: MethodBindingContext): ListenerSpec {
        return ListenerSpec(
            listenerType = Type.getType(context.binding.listener.replace('.', '/')),
            listenerOwner = Type.getType(context.binding.owner.replace('.', '/')),
            listenerSetter = context.binding.setter,

            generatedType = context.factory.newAnonymousType(),
            generatedTarget = context.clazz.type,

            method = context.method
        )
      }
    }
  }
}
