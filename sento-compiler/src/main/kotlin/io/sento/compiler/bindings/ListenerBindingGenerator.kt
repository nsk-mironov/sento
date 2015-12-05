package io.sento.compiler.bindings

import io.sento.compiler.GeneratedContent
import io.sento.compiler.GenerationEnvironment
import io.sento.compiler.annotations.ids
import io.sento.compiler.common.Methods
import io.sento.compiler.common.Naming
import io.sento.compiler.common.Types
import io.sento.compiler.common.body
import io.sento.compiler.common.isAbstract
import io.sento.compiler.common.isInterface
import io.sento.compiler.common.isPrivate
import io.sento.compiler.common.simpleName
import io.sento.compiler.model.ListenerBindingSpec
import io.sento.compiler.model.ListenerClassSpec
import io.sento.compiler.reflection.MethodSpec
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes.ACC_FINAL
import org.objectweb.asm.Opcodes.ACC_PRIVATE
import org.objectweb.asm.Opcodes.ACC_PUBLIC
import org.objectweb.asm.Opcodes.ACC_SUPER
import org.objectweb.asm.Opcodes.V1_6
import org.objectweb.asm.Type
import org.objectweb.asm.commons.GeneratorAdapter
import org.slf4j.LoggerFactory

internal class ListenerBindingGenerator(public val spec: ListenerClassSpec) {
  private val logger = LoggerFactory.getLogger(ListenerBindingGenerator::class.java)

  public fun bind(context: ListenerBindingContext, environment: GenerationEnvironment): List<GeneratedContent> {
    logger.info("Generating @{} binding for '{}' method",
        context.binding.target.annotation.type.simpleName, context.binding.target.method.name)

    val listener = context.binding
    val result = listOf(onCreateBindingListener(listener, environment))
    val adapter = context.adapter

    context.binding.target.annotation.ids.forEach {
      adapter.newLabel().apply {
        if (context.binding.target.optional) {
          adapter.loadLocal(context.variable("view$it"))
          adapter.ifNull(this)
        }

        adapter.loadLocal(context.variable("view$it")).apply {
          if (spec.owner.type != Types.VIEW) {
            adapter.checkCast(spec.owner.type)
          }
        }

        adapter.newInstance(listener.type)
        adapter.dup()

        adapter.loadLocal(context.variable("target"))
        adapter.invokeConstructor(listener.type, Methods.getConstructor(listener.target.clazz.type))
        adapter.invokeVirtual(spec.owner.type, Methods.get(spec.setter))

        adapter.mark(this)
      }
    }

    return result
  }

  public fun unbind(context: ListenerBindingContext, environment: GenerationEnvironment): List<GeneratedContent> {
    return emptyList()
  }

  private fun onCreateBindingListener(listener: ListenerBindingSpec, environment: GenerationEnvironment): GeneratedContent {
    return GeneratedContent(Types.getClassFilePath(listener.type), environment.newClass {
      visitListenerHeader(listener, environment)
      visitListenerFields(listener, environment)
      visitListenerConstructor(listener, environment)

      val registry = environment.registry
      val callbacks = registry.listPublicMethods(spec.listener).filter {
        it.access.isAbstract
      }

      callbacks.forEach {
        if (it.name == listener.descriptor.callback.name && it.type == listener.descriptor.callback.type) {
          visitListenerCallback(listener, environment)
        } else {
          visitListenerStub(listener, it, environment)
        }
      }
    })
  }

  private fun ClassVisitor.visitListenerHeader(listener: ListenerBindingSpec, environment: GenerationEnvironment) {
    visit(V1_6, ACC_PUBLIC + ACC_SUPER, listener.type.internalName, null, spec.listenerParent.internalName, spec.listenerInterfaces.map { it.internalName }.toTypedArray())
  }

  private fun ClassVisitor.visitListenerFields(listener: ListenerBindingSpec, environment: GenerationEnvironment) {
    visitField(ACC_PRIVATE + ACC_FINAL, "target", listener.target.clazz.type.descriptor, null, null)
  }

  private fun ClassVisitor.visitListenerConstructor(listener: ListenerBindingSpec, environment: GenerationEnvironment) {
    GeneratorAdapter(ACC_PUBLIC, Methods.getConstructor(listener.target.clazz.type), null, null, this).body {
      loadThis()
      invokeConstructor(spec.listenerParent, Methods.getConstructor())

      loadThis()
      loadArg(0)
      putField(listener.type, "target", listener.target.clazz.type)
    }
  }

  private fun ClassVisitor.visitListenerCallback(listener: ListenerBindingSpec, environment: GenerationEnvironment) {
    GeneratorAdapter(ACC_PUBLIC, Methods.get(listener.descriptor.callback), listener.descriptor.callback.signature, null, this).body {
      loadThis()
      getField(listener.type, "target", listener.target.clazz.type)

      listener.args.forEach {
        loadArg(it.index).apply {
          if (!Types.isPrimitive(it.type)) {
            checkCast(it.type)
          }
        }
      }

      if (listener.target.method.access.isPrivate) {
        invokeStatic(listener.target.clazz.type, Naming.getSyntheticAccessor(listener.target.clazz.type, listener.target.method))
      } else {
        invokeVirtual(listener.target.clazz.type, Methods.get(listener.target.method))
      }

      if (listener.descriptor.callback.returns == Types.BOOLEAN && listener.target.method.returns == Types.VOID) {
        push(false)
      }
    }
  }

  private fun ClassVisitor.visitListenerStub(listener: ListenerBindingSpec, method: MethodSpec, environment: GenerationEnvironment) {
    GeneratorAdapter(ACC_PUBLIC, Methods.get(method), method.signature, null, this).body {
      if (method.returns == Types.BOOLEAN) {
        push(false)
      }
    }
  }

  private val ListenerClassSpec.listenerParent: Type
    get() = if (listener.access.isInterface) Types.OBJECT else listener.type

  private val ListenerClassSpec.listenerInterfaces: Array<Type>
    get() = if (listener.access.isInterface) arrayOf(listener.type) else emptyArray()
}
