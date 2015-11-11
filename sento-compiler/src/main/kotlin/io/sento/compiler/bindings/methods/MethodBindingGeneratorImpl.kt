package io.sento.compiler.bindings.methods

import io.sento.Optional
import io.sento.compiler.GeneratedContent
import io.sento.compiler.GenerationEnvironment
import io.sento.compiler.common.Annotations
import io.sento.compiler.common.Types
import io.sento.compiler.common.toClassFilePath
import io.sento.compiler.common.toSourceFilePath
import io.sento.compiler.model.MethodBindingSpec
import io.sento.compiler.model.MethodSpec
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.commons.GeneratorAdapter
import org.objectweb.asm.commons.Method

internal class MethodBindingGeneratorImpl(private val binding: MethodBindingSpec) : MethodBindingGenerator {
  override fun bind(context: MethodBindingContext, environment: GenerationEnvironment): List<GeneratedContent> {
    val listener = createListenerSpec(context)
    val result = listOf(onCreateBindingListener(listener, environment))

    val visitor = context.visitor
    val annotation = context.annotation

    val method = context.method
    val optional = method.getAnnotation<Optional>() != null

    Annotations.ids(annotation).forEach {
      visitor.visitVarInsn(Opcodes.ALOAD, context.variable("finder"))
      visitor.visitLdcInsn(it)
      visitor.visitVarInsn(Opcodes.ALOAD, context.variable("source"))
      visitor.visitInsn(if (optional) Opcodes.ICONST_1 else Opcodes.ICONST_0)
      visitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, Types.TYPE_FINDER.internalName, "find", "(IL${Types.TYPE_OBJECT.internalName};Z)L${Types.TYPE_VIEW.internalName};", true)
      visitor.visitTypeInsn(Opcodes.NEW, listener.generatedType.internalName)
      visitor.visitInsn(Opcodes.DUP)
      visitor.visitVarInsn(Opcodes.ALOAD, context.variable("target"))
      visitor.visitMethodInsn(Opcodes.INVOKESPECIAL, listener.generatedType.internalName, "<init>", "(L${listener.generatedTarget.internalName};)V", false)
      visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, listener.listenerOwner.internalName, listener.listenerSetter.name, "(L${listener.listenerType.internalName};)V", false)
    }

    return result
  }

  private fun onCreateBindingListener(listener: ListenerSpec, environment: GenerationEnvironment): GeneratedContent {
    return GeneratedContent(listener.generatedType.toClassFilePath(), environment.createClass {
      visitListenerHeader(listener, environment)
      visitListenerFields(listener, environment)

      visitListenerConstructor(listener, environment)
      visitListenerCallback(listener, environment)
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
    GeneratorAdapter(Opcodes.ACC_PUBLIC, Method.getMethod("void <init> (${listener.generatedTarget.className})"), null, null, this).apply {
      loadThis()
      invokeConstructor(Types.TYPE_OBJECT, Method.getMethod("void <init> ()"))

      loadThis()
      loadArg(0)
      putField(listener.generatedType, "target", listener.generatedTarget)

      returnValue()
      endMethod()
    }
  }

  private fun ClassVisitor.visitListenerCallback(listener: ListenerSpec, environment: GenerationEnvironment) {
    GeneratorAdapter(Opcodes.ACC_PUBLIC, Method(listener.generatedMethod.name, listener.generatedMethod.type.descriptor), listener.generatedMethod.signature, null, this).apply {
      loadThis()
      getField(listener.generatedType, "target", listener.generatedTarget)

      loadArg(0)
      invokeVirtual(listener.generatedTarget, Method(listener.method.name, "(L${Types.TYPE_VIEW.internalName};)V"))

      returnValue()
      endMethod()
    }
  }

  private fun createListenerSpec(context: MethodBindingContext): ListenerSpec {
    return ListenerSpec(
        listenerType = binding.listener,
        listenerOwner = binding.owner,
        listenerSetter = binding.setter,

        generatedType = context.factory.newAnonymousType(),
        generatedTarget = context.clazz.type,
        generatedMethod = binding.callback,

        method = context.method
    )
  }

  private data class ListenerSpec(
      public val listenerType: Type,
      public val listenerOwner: Type,
      public val listenerSetter: MethodSpec,

      public val generatedType: Type,
      public val generatedTarget: Type,
      public val generatedMethod: MethodSpec,

      public val method: MethodSpec
  )
}
