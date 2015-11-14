package io.sento.compiler.bindings.methods

import io.sento.Optional
import io.sento.compiler.GeneratedContent
import io.sento.compiler.GenerationEnvironment
import io.sento.compiler.SentoException
import io.sento.compiler.common.Annotations
import io.sento.compiler.common.Methods
import io.sento.compiler.common.Types
import io.sento.compiler.common.simpleName
import io.sento.compiler.model.ListenerBindingSpec
import io.sento.compiler.model.MethodSpec
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.Type
import org.objectweb.asm.commons.GeneratorAdapter
import org.slf4j.LoggerFactory
import java.util.ArrayList
import java.util.LinkedHashSet

internal class ListenerBindingGenerator(private val binding: ListenerBindingSpec) : MethodBindingGenerator {
  private val logger = LoggerFactory.getLogger(ListenerBindingGenerator::class.java)

  override fun bind(context: MethodBindingContext, environment: GenerationEnvironment): List<GeneratedContent> {
    val annotation = context.annotation
    val adapter = context.adapter

    val method = context.method
    val optional = method.getAnnotation<Optional>() != null

    logger.info("Generating @{} binding for '{}' method",
        annotation.type.simpleName, method.name)

    val listener = createListenerSpec(context, environment)
    val result = listOf(onCreateBindingListener(listener, environment))

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

      listener.args.forEach {
        loadArg(it.index).apply {
          if (!Types.isPrimitive(it.type)) {
            checkCast(it.type)
          }
        }
      }

      invokeVirtual(listener.target, Methods.get(listener.method.name, listener.method.type.returnType, *listener.args.map {
        it.type
      }.toTypedArray()))

      returnValue()
      endMethod()
    }
  }

  private fun createListenerSpec(context: MethodBindingContext, environment: GenerationEnvironment): ListenerSpec {
    return ListenerSpec(
        type = context.factory.newAnonymousType(),
        target = context.clazz.type,
        callback = binding.callback,
        method = context.method,
        args = remapMethodArgs(context, environment)
    )
  }

  private fun remapMethodArgs(context: MethodBindingContext, environment: GenerationEnvironment): Collection<ArgumentSpec> {
    val result = ArrayList<ArgumentSpec>()
    val available = LinkedHashSet<Int>()

    val from = binding.callback
    val to = context.method

    val argsFrom = from.type.argumentTypes.orEmpty()
    val argsTo = to.type.argumentTypes.orEmpty()

    for (index in 0..argsFrom.size - 1) {
      available.add(index)
    }

    for (argument in argsTo) {
      val index = available.firstOrNull {
        available.contains(it) && environment.registry.isCastableFromTo(argsFrom[it], argument)
      }

      if (index == null) {
        throw SentoException("Unable to generate @{0} binding for ''{1}#{2}'' method - argument ''{3}'' didn''t match any listener parameters.",
            context.annotation.type.simpleName, context.clazz.type.className, context.method.name, argument.className)
      }

      result.add(ArgumentSpec(index, argument))
      available.remove(index)
    }

    return result
  }

  private data class ListenerSpec(
      public val type: Type,
      public val target: Type,
      public val callback: MethodSpec,
      public val method: MethodSpec,
      public val args: Collection<ArgumentSpec>
  )

  private data class ArgumentSpec(
      public val index: Int,
      public val type: Type
  )
}
