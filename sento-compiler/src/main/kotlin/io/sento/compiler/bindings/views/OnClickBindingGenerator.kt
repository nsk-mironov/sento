package io.sento.compiler.bindings.views

import io.sento.OnClick
import io.sento.Optional
import io.sento.compiler.api.GeneratedContent
import io.sento.compiler.api.GenerationEnvironment
import io.sento.compiler.bindings.MethodBindingContext
import io.sento.compiler.bindings.MethodBindingGenerator
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

internal class OnClickBindingGenerator : MethodBindingGenerator<OnClick> {
  override fun bind(context: MethodBindingContext<OnClick>, environment: GenerationEnvironment): List<GeneratedContent> {
    val listener = ListenerSpec(context.factory.newAnonymousType(), context.clazz.type, context.method)
    val result = listOf(onCreateOnClickListener(listener, environment))

    val visitor = context.visitor
    val annotation = context.annotation
    val clazz = context.clazz

    val method = context.method
    val optional = method.getAnnotation<Optional>() != null

    annotation.value.forEach {
      visitor.visitVarInsn(ALOAD, context.variable("finder"))
      visitor.visitLdcInsn(it)
      visitor.visitVarInsn(ALOAD, context.variable("source"))
      visitor.visitInsn(if (optional) Opcodes.ICONST_1 else Opcodes.ICONST_0)
      visitor.visitMethodInsn(INVOKEINTERFACE, Types.TYPE_FINDER.internalName, "find", "(IL${Types.TYPE_OBJECT.internalName};Z)L${Types.TYPE_VIEW.internalName};", true)
      visitor.visitTypeInsn(NEW, listener.type.internalName)
      visitor.visitInsn(DUP)
      visitor.visitVarInsn(ALOAD, context.variable("target"))
      visitor.visitMethodInsn(INVOKESPECIAL, listener.type.internalName, "<init>", "(L${clazz.type.internalName};)V", false)
      visitor.visitMethodInsn(INVOKEVIRTUAL, Types.TYPE_VIEW.internalName, "setOnClickListener", "(L${Types.TYPE_VIEW.internalName}\$OnClickListener;)V", false)
    }

    return result
  }

  private fun onCreateOnClickListener(listener: ListenerSpec, environment: GenerationEnvironment): GeneratedContent {
    return GeneratedContent(listener.type.toClassFilePath(), with (ClassWriter(0)) {
      visitListenerHeader(listener, environment)
      visitListenerFields(listener, environment)
      visitListenerConstructor(listener, environment)
      visitListenerOnClick(listener, environment)
      toByteArray()
    })
  }

  private fun ClassVisitor.visitListenerHeader(listener: ListenerSpec, environment: GenerationEnvironment) {
    visit(Opcodes.V1_6, ACC_PUBLIC + ACC_SUPER, listener.type.internalName, null, Types.TYPE_OBJECT.internalName, arrayOf("${Types.TYPE_VIEW.internalName}\$OnClickListener"))
    visitSource(listener.type.toSourceFilePath(), null)
  }

  private fun ClassVisitor.visitListenerFields(listener: ListenerSpec, environment: GenerationEnvironment) {
    visitField(ACC_PRIVATE + ACC_FINAL, "target", listener.target.descriptor, null, null)
  }

  private fun ClassVisitor.visitListenerConstructor(listener: ListenerSpec, environment: GenerationEnvironment) {
    val visitor = visitMethod(ACC_PUBLIC, "<init>", "(L${listener.target.internalName};)V", null, null)

    val start = Label()
    val end = Label()

    visitor.visitCode()
    visitor.visitLabel(start)
    visitor.visitVarInsn(ALOAD, 0)
    visitor.visitMethodInsn(INVOKESPECIAL, Types.TYPE_OBJECT.internalName, "<init>", "()V", false)
    visitor.visitVarInsn(ALOAD, 0)
    visitor.visitVarInsn(ALOAD, 1)
    visitor.visitFieldInsn(PUTFIELD, listener.type.internalName, "target", listener.target.descriptor)
    visitor.visitInsn(RETURN)
    visitor.visitLabel(end)
    visitor.visitLocalVariable("this", listener.type.descriptor, null, start, end, 0)
    visitor.visitLocalVariable("target", listener.target.descriptor, null, start, end, 1)
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
    visitor.visitFieldInsn(GETFIELD, listener.type.internalName, "target", listener.target.descriptor)
    visitor.visitVarInsn(ALOAD, 1)
    visitor.visitMethodInsn(INVOKEVIRTUAL, listener.target.internalName, listener.method.name, "(L${Types.TYPE_VIEW.internalName};)V", false)
    visitor.visitInsn(RETURN)
    visitor.visitLabel(end)
    visitor.visitLocalVariable("this", listener.type.descriptor, null, start, end, 0)
    visitor.visitLocalVariable("view", Types.TYPE_VIEW.descriptor, null, start, end, 1)
    visitor.visitMaxs(2, 2)
    visitor.visitEnd()
  }

  private class ListenerSpec(val type: Type, val target: Type, val method: MethodSpec) {

  }
}
