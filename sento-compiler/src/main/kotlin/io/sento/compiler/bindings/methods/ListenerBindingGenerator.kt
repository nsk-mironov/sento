package io.sento.compiler.bindings.methods

import io.sento.compiler.GeneratedContent
import io.sento.compiler.GenerationEnvironment
import io.sento.compiler.SentoException
import io.sento.compiler.annotations.ids
import io.sento.compiler.common.Methods
import io.sento.compiler.common.Types
import io.sento.compiler.common.isAbstract
import io.sento.compiler.common.isInterface
import io.sento.compiler.common.isPublic
import io.sento.compiler.common.simpleName
import io.sento.compiler.model.ListenerBindingSpec
import io.sento.compiler.model.MethodSpec
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes.ACC_FINAL
import org.objectweb.asm.Opcodes.ACC_PRIVATE
import org.objectweb.asm.Opcodes.ACC_PUBLIC
import org.objectweb.asm.Opcodes.ACC_SUPER
import org.objectweb.asm.Opcodes.V1_6
import org.objectweb.asm.Type
import org.objectweb.asm.commons.GeneratorAdapter
import org.slf4j.LoggerFactory
import java.util.ArrayList
import java.util.LinkedHashSet

internal class ListenerBindingGenerator(private val binding: ListenerBindingSpec) : MethodBindingGenerator {
  private val logger = LoggerFactory.getLogger(ListenerBindingGenerator::class.java)

  override fun bind(context: MethodBindingContext, environment: GenerationEnvironment): List<GeneratedContent> {
    logger.info("Generating @{} binding for '{}' method",
        context.annotation.type.simpleName, context.method.name)

    val listener = createListenerSpec(context, environment)
    val result = listOf(onCreateBindingListener(listener, environment))
    val adapter = context.adapter

    context.annotation.ids.forEach {
      val view = adapter.newLocal(binding.owner.type)

      adapter.loadArg(context.argument("finder"))
      adapter.push(it)

      adapter.loadArg(context.argument("source"))
      adapter.push(context.optional)

      adapter.invokeInterface(Types.FINDER, Methods.get("find", Types.VIEW, Types.INT, Types.OBJECT, Types.BOOLEAN)).apply {
        if (binding.owner.type != Types.VIEW) {
          adapter.checkCast(binding.owner.type)
        }
      }

      adapter.storeLocal(view)
      adapter.newLabel().apply {
        if (context.optional) {
          adapter.loadLocal(view)
          adapter.ifNull(this)
        }

        adapter.loadLocal(view)
        adapter.newInstance(listener.type)
        adapter.dup()

        adapter.loadLocal(context.variable("target"))
        adapter.invokeConstructor(listener.type, Methods.getConstructor(listener.target))
        adapter.invokeVirtual(binding.owner.type, Methods.get(binding.setter))

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

      val registry = environment.registry
      val callbacks = registry.listPublicMethods(binding.listener).filter {
        it.access.isAbstract
      }

      callbacks.forEach {
        if (it.name == listener.callback.name && it.type == listener.callback.type) {
          visitListenerCallback(listener, environment)
        } else {
          visitListenerStub(listener, it, environment)
        }
      }
    })
  }

  private fun ClassVisitor.visitListenerHeader(listener: ListenerSpec, environment: GenerationEnvironment) {
    visit(V1_6, ACC_PUBLIC + ACC_SUPER, listener.type.internalName, null, binding.listenerParent.internalName, binding.listenerInterfaces.map { it.internalName }.toTypedArray())
  }

  private fun ClassVisitor.visitListenerFields(listener: ListenerSpec, environment: GenerationEnvironment) {
    visitField(ACC_PRIVATE + ACC_FINAL, "target", listener.target.descriptor, null, null)
  }

  private fun ClassVisitor.visitListenerConstructor(listener: ListenerSpec, environment: GenerationEnvironment) {
    GeneratorAdapter(ACC_PUBLIC, Methods.getConstructor(listener.target), null, null, this).apply {
      loadThis()
      invokeConstructor(binding.listenerParent, Methods.getConstructor())

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

      if (!listener.method.access.isPublic) {
        invokeStatic(listener.target, Methods.getAccessor(listener.target, listener.method))
      } else {
        invokeVirtual(listener.target, Methods.get(listener.method))
      }

      if (listener.callback.returns == Types.BOOLEAN && listener.method.returns == Types.VOID) {
        push(false)
      }

      returnValue()
      endMethod()
    }
  }

  private fun ClassVisitor.visitListenerStub(listener: ListenerSpec, method: MethodSpec, environment: GenerationEnvironment) {
    GeneratorAdapter(ACC_PUBLIC, Methods.get(method), method.signature, null, this).apply {
      if (method.returns == Types.BOOLEAN) {
        push(false)
      }

      returnValue()
      endMethod()
    }
  }

  private fun createListenerSpec(context: MethodBindingContext, environment: GenerationEnvironment): ListenerSpec {
    val type = context.factory.newAnonymousType()
    val args = remapMethodArgs(context, environment)

    if (context.method.returns !in listOf(Types.VOID, Types.BOOLEAN)) {
      throw SentoException("Unable to generate @{0} binding for ''{1}#{2}'' method - it returns ''{3}'', but only {4} are supported.",
          context.annotation.type.simpleName, context.clazz.type.className, context.method.name, context.method.returns.className, listOf(Types.VOID.className, Types.BOOLEAN.className))
    }

    return ListenerSpec(type, context.clazz.type, binding.callback, context.method, args)
  }

  private fun remapMethodArgs(context: MethodBindingContext, environment: GenerationEnvironment): Collection<ArgumentSpec> {
    val result = ArrayList<ArgumentSpec>()
    val available = LinkedHashSet<Int>()

    val from = binding.callback
    val to = context.method

    for (index in 0..from.arguments.size - 1) {
      available.add(index)
    }

    for (argument in to.arguments) {
      val index = available.firstOrNull {
        available.contains(it) && environment.registry.isCastableFromTo(from.arguments[it], argument)
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

  private val ListenerBindingSpec.listenerParent: Type
    get() = if (listener.access.isInterface) Types.OBJECT else listener.type

  private val ListenerBindingSpec.listenerInterfaces: Array<Type>
    get() = if (listener.access.isInterface) arrayOf(listener.type) else emptyArray()

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
