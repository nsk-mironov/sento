package io.sento.compiler.bindings.methods

import io.sento.MethodBinding
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
import org.objectweb.asm.Type

internal class MethodBindingGeneratorImpl(private val binding: MethodBinding) : MethodBindingGenerator {
  override fun bind(context: MethodBindingContext, environment: GenerationEnvironment): List<GeneratedContent> {
    val listener = createListenerSpec(context)
    val result = listOf(onCreateOnClickListener(listener, environment))

    val visitor = context.visitor
    val annotation = context.annotation

    val method = context.method
    val optional = method.getAnnotation<Optional>() != null

    annotation.value<IntArray>("value")?.forEach {
      visitor.visitVarInsn(Opcodes.ALOAD, context.variable("finder"))
      visitor.visitLdcInsn(it)
      visitor.visitVarInsn(Opcodes.ALOAD, context.variable("source"))
      visitor.visitInsn(if (optional) Opcodes.ICONST_1 else Opcodes.ICONST_0)
      visitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, Types.TYPE_FINDER.internalName, "find", "(IL${Types.TYPE_OBJECT.internalName};Z)L${Types.TYPE_VIEW.internalName};", true)
      visitor.visitTypeInsn(Opcodes.NEW, listener.generatedType.internalName)
      visitor.visitInsn(Opcodes.DUP)
      visitor.visitVarInsn(Opcodes.ALOAD, context.variable("target"))
      visitor.visitMethodInsn(Opcodes.INVOKESPECIAL, listener.generatedType.internalName, "<init>", "(L${listener.generatedTarget.internalName};)V", false)
      visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, listener.listenerOwner.internalName, listener.listenerSetter, "(L${listener.listenerType.internalName};)V", false)
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
    visit(Opcodes.V1_6, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, listener.generatedType.internalName, null, Types.TYPE_OBJECT.internalName, arrayOf(listener.listenerType.internalName))
    visitSource(listener.generatedType.toSourceFilePath(), null)
  }

  private fun ClassVisitor.visitListenerFields(listener: ListenerSpec, environment: GenerationEnvironment) {
    visitField(Opcodes.ACC_PRIVATE + Opcodes.ACC_FINAL, "target", listener.generatedTarget.descriptor, null, null)
  }

  private fun ClassVisitor.visitListenerConstructor(listener: ListenerSpec, environment: GenerationEnvironment) {
    val visitor = visitMethod(Opcodes.ACC_PUBLIC, "<init>", "(L${listener.generatedTarget.internalName};)V", null, null)

    val start = Label()
    val end = Label()

    visitor.visitCode()
    visitor.visitLabel(start)
    visitor.visitVarInsn(Opcodes.ALOAD, 0)
    visitor.visitMethodInsn(Opcodes.INVOKESPECIAL, Types.TYPE_OBJECT.internalName, "<init>", "()V", false)
    visitor.visitVarInsn(Opcodes.ALOAD, 0)
    visitor.visitVarInsn(Opcodes.ALOAD, 1)
    visitor.visitFieldInsn(Opcodes.PUTFIELD, listener.generatedType.internalName, "target", listener.generatedTarget.descriptor)
    visitor.visitInsn(Opcodes.RETURN)
    visitor.visitLabel(end)
    visitor.visitLocalVariable("this", listener.generatedType.descriptor, null, start, end, 0)
    visitor.visitLocalVariable("target", listener.generatedTarget.descriptor, null, start, end, 1)
    visitor.visitMaxs(2, 2)
    visitor.visitEnd()
  }

  private fun ClassVisitor.visitListenerOnClick(listener: ListenerSpec, environment: GenerationEnvironment) {
    val visitor = visitMethod(Opcodes.ACC_PUBLIC, "onClick", "(L${Types.TYPE_VIEW.internalName};)V", null, null)

    val start = Label()
    val end = Label()

    visitor.visitCode()
    visitor.visitLabel(start)
    visitor.visitVarInsn(Opcodes.ALOAD, 0)
    visitor.visitFieldInsn(Opcodes.GETFIELD, listener.generatedType.internalName, "target", listener.generatedTarget.descriptor)
    visitor.visitVarInsn(Opcodes.ALOAD, 1)
    visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, listener.generatedTarget.internalName, listener.method.name, "(L${Types.TYPE_VIEW.internalName};)V", false)
    visitor.visitInsn(Opcodes.RETURN)
    visitor.visitLabel(end)
    visitor.visitLocalVariable("this", listener.generatedType.descriptor, null, start, end, 0)
    visitor.visitLocalVariable("view", Types.TYPE_VIEW.descriptor, null, start, end, 1)
    visitor.visitMaxs(2, 2)
    visitor.visitEnd()
  }

  private fun createListenerSpec(context: MethodBindingContext): ListenerSpec {
    return ListenerSpec(
        listenerType = Type.getType(binding.listener.replace('.', '/')),
        listenerOwner = Type.getType(binding.owner.replace('.', '/')),
        listenerSetter = binding.setter,

        generatedType = context.factory.newAnonymousType(),
        generatedTarget = context.clazz.type,

        method = context.method
    )
  }

  private data class ListenerSpec(
      public val listenerType: Type,
      public val listenerOwner: Type,
      public val listenerSetter: String,

      public val generatedType: Type,
      public val generatedTarget: Type,

      public val method: MethodSpec
  )
}
