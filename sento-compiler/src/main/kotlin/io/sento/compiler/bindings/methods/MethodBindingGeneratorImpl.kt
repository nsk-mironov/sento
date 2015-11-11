package io.sento.compiler.bindings.methods

import io.sento.Optional
import io.sento.compiler.GeneratedContent
import io.sento.compiler.GenerationEnvironment
import io.sento.compiler.common.Annotations
import io.sento.compiler.common.Types
import io.sento.compiler.common.toClassFilePath
import io.sento.compiler.model.MethodBindingSpec
import io.sento.compiler.model.MethodSpec
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.Type
import org.objectweb.asm.commons.GeneratorAdapter
import org.objectweb.asm.commons.Method

internal class MethodBindingGeneratorImpl(private val binding: MethodBindingSpec) : MethodBindingGenerator {
  override fun bind(context: MethodBindingContext, environment: GenerationEnvironment): List<GeneratedContent> {
    val listener = createListenerSpec(context)
    val result = listOf(onCreateBindingListener(listener, environment))

    val annotation = context.annotation
    val adapter = context.adapter

    val method = context.method
    val optional = method.getAnnotation<Optional>() != null

    Annotations.ids(annotation).forEach {
      val view = adapter.newLocal(Types.TYPE_VIEW)

      adapter.loadArg(context.variable("finder"))
      adapter.push(it)

      adapter.loadArg(context.variable("source"))
      adapter.push(optional)

      adapter.invokeInterface(Types.TYPE_FINDER, Method.getMethod("android.view.View find(int, Object, boolean)"))
      adapter.storeLocal(view)

      adapter.newLabel().apply {
        adapter.loadLocal(view)
        adapter.ifNull(this)

        adapter.loadLocal(view)
        adapter.newInstance(listener.type)
        adapter.dup()

        adapter.loadArg(context.variable("target"))
        adapter.invokeConstructor(listener.type, Method.getMethod("void <init> (${listener.target.className})"))
        adapter.invokeVirtual(binding.owner, Method(binding.setter.name, "(L${binding.listener.internalName};)V"))

        adapter.mark(this)
      }
    }

    return result
  }

  private fun onCreateBindingListener(listener: ListenerSpec, environment: GenerationEnvironment): GeneratedContent {
    return GeneratedContent(listener.type.toClassFilePath(), environment.createClass {
      visitListenerHeader(listener, environment)
      visitListenerFields(listener, environment)

      visitListenerConstructor(listener, environment)
      visitListenerCallback(listener, environment)
    })
  }

  private fun ClassVisitor.visitListenerHeader(listener: ListenerSpec, environment: GenerationEnvironment) {
    visit(V1_6, ACC_PUBLIC + ACC_SUPER, listener.type.internalName, null, Types.TYPE_OBJECT.internalName, arrayOf(binding.listener.internalName))
  }

  private fun ClassVisitor.visitListenerFields(listener: ListenerSpec, environment: GenerationEnvironment) {
    visitField(ACC_PRIVATE + ACC_FINAL, "target", listener.target.descriptor, null, null)
  }

  private fun ClassVisitor.visitListenerConstructor(listener: ListenerSpec, environment: GenerationEnvironment) {
    GeneratorAdapter(ACC_PUBLIC, Method.getMethod("void <init> (${listener.target.className})"), null, null, this).apply {
      loadThis()
      invokeConstructor(Types.TYPE_OBJECT, Method.getMethod("void <init> ()"))

      loadThis()
      loadArg(0)
      putField(listener.type, "target", listener.target)

      returnValue()
      endMethod()
    }
  }

  private fun ClassVisitor.visitListenerCallback(listener: ListenerSpec, environment: GenerationEnvironment) {
    GeneratorAdapter(ACC_PUBLIC, Method(listener.callback.name, listener.callback.type.descriptor), listener.callback.signature, null, this).apply {
      loadThis()
      getField(listener.type, "target", listener.target)

      loadArg(0)
      invokeVirtual(listener.target, Method(listener.method.name, "(L${Types.TYPE_VIEW.internalName};)V"))

      returnValue()
      endMethod()
    }
  }

  private fun createListenerSpec(context: MethodBindingContext): ListenerSpec {
    return ListenerSpec(context.factory.newAnonymousType(), context.clazz.type, binding.callback, context.method)
  }

  private data class ListenerSpec(
      public val type: Type,
      public val target: Type,
      public val callback: MethodSpec,
      public val method: MethodSpec
  )
}
