package io.sento.compiler.bindings.methods

import io.sento.Optional
import io.sento.compiler.GeneratedContent
import io.sento.compiler.GenerationEnvironment
import io.sento.compiler.common.Annotations
import io.sento.compiler.common.Methods
import io.sento.compiler.common.Types
import io.sento.compiler.model.ListenerBindingSpec
import io.sento.compiler.model.MethodSpec
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.Type
import org.objectweb.asm.commons.GeneratorAdapter

internal class ListenerBindingGenerator(private val binding: ListenerBindingSpec) : MethodBindingGenerator {
  override fun bind(context: MethodBindingContext, environment: GenerationEnvironment): List<GeneratedContent> {
    val listener = createListenerSpec(context)
    val result = listOf(onCreateBindingListener(listener, environment))

    val annotation = context.annotation
    val adapter = context.adapter

    val method = context.method
    val optional = method.getAnnotation<Optional>() != null

    Annotations.ids(annotation).forEach {
      val view = adapter.newLocal(Types.VIEW)

      adapter.loadArg(context.variable("finder"))
      adapter.push(it)

      adapter.loadArg(context.variable("source"))
      adapter.push(optional)

      adapter.invokeInterface(Types.FINDER, Methods.get("find", Types.VIEW, Types.INT, Types.OBJECT, Types.BOOLEAN))
      adapter.storeLocal(view)

      adapter.newLabel().apply {
        adapter.loadLocal(view)
        adapter.ifNull(this)

        adapter.loadLocal(view)
        adapter.newInstance(listener.type)
        adapter.dup()

        adapter.loadArg(context.variable("target"))
        adapter.invokeConstructor(listener.type, Methods.getConstructor(listener.target))
        adapter.invokeVirtual(binding.owner, Methods.get(binding.setter))

        adapter.mark(this)
      }
    }

    return result
  }

  private fun onCreateBindingListener(listener: ListenerSpec, environment: GenerationEnvironment): GeneratedContent {
    return GeneratedContent(Types.getClassFilePath(listener.type), environment.createClass {
      visitListenerHeader(listener, environment)
      visitListenerFields(listener, environment)

      visitListenerConstructor(listener, environment)
      visitListenerCallback(listener, environment)
    })
  }

  private fun ClassVisitor.visitListenerHeader(listener: ListenerSpec, environment: GenerationEnvironment) {
    visit(V1_6, ACC_PUBLIC + ACC_SUPER, listener.type.internalName, null, Types.OBJECT.internalName, arrayOf(binding.listener.internalName))
  }

  private fun ClassVisitor.visitListenerFields(listener: ListenerSpec, environment: GenerationEnvironment) {
    visitField(ACC_PRIVATE + ACC_FINAL, "target", listener.target.descriptor, null, null)
  }

  private fun ClassVisitor.visitListenerConstructor(listener: ListenerSpec, environment: GenerationEnvironment) {
    GeneratorAdapter(ACC_PUBLIC, Methods.getConstructor(listener.target), null, null, this).apply {
      loadThis()
      invokeConstructor(Types.OBJECT, Methods.getConstructor())

      loadThis()
      loadArg(0)
      putField(listener.type, "target", listener.target)

      returnValue()
      endMethod()
    }
  }

  private fun ClassVisitor.visitListenerCallback(listener: ListenerSpec, environment: GenerationEnvironment) {
    GeneratorAdapter(ACC_PUBLIC, Methods.get(listener.callback), listener.callback.signature, null, this).apply {
      loadThis()
      getField(listener.type, "target", listener.target)

      loadArg(0)
      invokeVirtual(listener.target, Methods.get(listener.method.name, Types.VOID, Types.VIEW))

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
