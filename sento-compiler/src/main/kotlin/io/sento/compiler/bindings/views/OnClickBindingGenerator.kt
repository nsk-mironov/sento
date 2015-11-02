package io.sento.compiler.bindings.views

import io.sento.OnClick
import io.sento.compiler.api.GeneratedContent
import io.sento.compiler.api.GenerationEnvironment
import io.sento.compiler.bindings.MethodBindingContext
import io.sento.compiler.bindings.MethodBindingGenerator
import io.sento.compiler.common.Types
import io.sento.compiler.common.toClassFilePath
import io.sento.compiler.common.toSourceFilePath
import io.sento.compiler.specs.MethodSpec
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Label
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.Type

public class OnClickBindingGenerator : MethodBindingGenerator<OnClick> {
  override fun bind(context: MethodBindingContext<OnClick>, environment: GenerationEnvironment): List<GeneratedContent> {
    return listOf(onCreateOnClickListener(context, environment))
  }

  private fun onCreateOnClickListener(context: MethodBindingContext<OnClick>, environment: GenerationEnvironment): GeneratedContent {
    val type = context.factory.newAnonymousType()
    val listener = ListenerSpec(type, context.clazz.type, context.method)
    val writer = ClassWriter(0)

    writer.visitListenerHeader(listener, environment)
    writer.visitListenerFields(listener, environment)
    writer.visitListenerConstructor(listener, environment)
    writer.visitListenerOnClick(listener, environment)

    return GeneratedContent(type.toClassFilePath(), writer.toByteArray())
  }

  private fun ClassVisitor.visitListenerHeader(listener: ListenerSpec, environment: GenerationEnvironment) {
    visit(Opcodes.V1_6, ACC_PUBLIC + ACC_SUPER, listener.type.internalName, null, Types.TYPE_OBJECT.internalName, arrayOf("android/view/View\$OnClickListener"))
    visitSource(listener.type.toSourceFilePath(), null)
    visitInnerClass("android/view/View\$OnClickListener", "android/view/View", "OnClickListener", ACC_PUBLIC + ACC_STATIC + ACC_ABSTRACT + ACC_INTERFACE)
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
    val visitor = visitMethod(ACC_PUBLIC, "onClick", "(Landroid/view/View;)V", null, null)

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
