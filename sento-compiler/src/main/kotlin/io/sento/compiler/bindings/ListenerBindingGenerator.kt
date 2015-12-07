package io.sento.compiler.bindings

import io.sento.compiler.GeneratedContent
import io.sento.compiler.GenerationEnvironment
import io.sento.compiler.annotations.ids
import io.sento.compiler.common.Methods
import io.sento.compiler.common.Types
import io.sento.compiler.common.isAbstract
import io.sento.compiler.common.isInterface
import io.sento.compiler.common.isPrivate
import io.sento.compiler.common.newMethod
import io.sento.compiler.model.ListenerBindingSpec
import io.sento.compiler.model.ListenerClassSpec
import io.sento.compiler.model.ViewSpec
import org.objectweb.asm.Opcodes.ACC_FINAL
import org.objectweb.asm.Opcodes.ACC_PRIVATE
import org.objectweb.asm.Opcodes.ACC_PUBLIC
import org.objectweb.asm.Opcodes.ACC_SUPER
import org.objectweb.asm.Opcodes.V1_6

internal class ListenerBindingGenerator(public val spec: ListenerClassSpec) {
  public fun bindFields(context: ListenerBindingContext, environment: GenerationEnvironment) {
    context.adapter.loadLocal(context.variable("target"))
    context.adapter.newInstance(context.binding.type, Methods.getConstructor(context.binding.target.clazz)) {
      context.adapter.loadLocal(context.variable("target"))
    }
    context.adapter.putField(context.binding.target.clazz, environment.naming.getSyntheticFieldNameForMethodTarget(context.binding.target), spec.listener)
  }

  public fun bindListeners(context: ListenerBindingContext, environment: GenerationEnvironment) {
    context.binding.target.annotation.ids.forEach {
      context.adapter.newLabel().apply {
        val view = ViewSpec(it, context.binding.target.optional, "method '${context.binding.target.method.name}'")
        val name = environment.naming.getSyntheticFieldNameForViewTarget(view)

        if (context.binding.target.optional) {
          context.adapter.loadLocal(context.variable("target"))
          context.adapter.getField(context.binding.target.clazz, name, Types.VIEW)
          context.adapter.ifNull(this)
        }

        context.adapter.loadLocal(context.variable("target"))
        context.adapter.getField(context.binding.target.clazz, name, Types.VIEW)

        if (spec.owner.type != Types.VIEW) {
          context.adapter.checkCast(spec.owner)
        }

        context.adapter.loadLocal(context.variable("target"))
        context.adapter.getField(context.binding.target.clazz, environment.naming.getSyntheticFieldNameForMethodTarget(context.binding.target), spec.listener)

        context.adapter.invokeVirtual(spec.owner, spec.setter)
        context.adapter.mark(this)
      }
    }
  }

  public fun unbindFields(context: ListenerBindingContext, environment: GenerationEnvironment) {
    context.adapter.loadLocal(context.variables["target"]!!)
    context.adapter.pushNull()
    context.adapter.putField(context.binding.target.clazz, environment.naming.getSyntheticFieldNameForMethodTarget(context.binding.target), spec.listener)
  }

  public fun unbindListeners(context: ListenerBindingContext, environment: GenerationEnvironment) {
    context.binding.target.annotation.ids.forEach {
      context.adapter.newLabel().apply {
        val view = ViewSpec(it, context.binding.target.optional, "method '${context.binding.target.method.name}'")
        val name = environment.naming.getSyntheticFieldNameForViewTarget(view)

        if (context.binding.target.optional) {
          context.adapter.loadLocal(context.variable("target"))
          context.adapter.getField(context.binding.target.clazz, name, Types.VIEW)
          context.adapter.ifNull(this)
        }

        context.adapter.loadLocal(context.variable("target"))
        context.adapter.getField(context.binding.target.clazz, name, Types.VIEW)

        if (spec.owner.type != Types.VIEW) {
          context.adapter.checkCast(spec.owner)
        }

        if (context.binding.descriptor.setter != context.binding.descriptor.unsetter) {
          context.adapter.loadLocal(context.variable("target"))
          context.adapter.getField(context.binding.target.clazz, environment.naming.getSyntheticFieldNameForMethodTarget(context.binding.target), spec.listener)
        }

        if (context.binding.descriptor.setter == context.binding.descriptor.unsetter) {
          context.adapter.pushNull()
        }

        context.adapter.invokeVirtual(spec.owner, spec.unsetter)
        context.adapter.mark(this)
      }
    }
  }

  public fun generate(listener: ListenerBindingSpec, environment: GenerationEnvironment): List<GeneratedContent> {
    return listOf(GeneratedContent(Types.getClassFilePath(listener.type), environment.newClass {
      val parent = if (spec.listener.access.isInterface) Types.OBJECT else spec.listener.type
      val interfaces = if (spec.listener.access.isInterface) arrayOf(spec.listener.type) else emptyArray()

      visit(V1_6, ACC_PUBLIC + ACC_SUPER, listener.type.internalName, null, parent.internalName, interfaces.map { it.internalName }.toTypedArray())
      visitField(ACC_PRIVATE + ACC_FINAL, "target", listener.target.clazz.type.descriptor, null, null)

      newMethod(ACC_PUBLIC, Methods.getConstructor(listener.target.clazz)) {
        loadThis()
        invokeConstructor(parent, Methods.getConstructor())

        loadThis()
        loadArg(0)
        putField(listener.type, "target", listener.target.clazz.type)
      }

      newMethod(ACC_PUBLIC, listener.descriptor.callback) {
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
          invokeStatic(listener.target.clazz, environment.naming.getSyntheticAccessor(listener.target.clazz, listener.target.method))
        } else {
          invokeVirtual(listener.target.clazz, listener.target.method)
        }

        if (listener.descriptor.callback.returns == Types.BOOLEAN && listener.target.method.returns == Types.VOID) {
          push(false)
        }
      }

      environment.registry.listPublicMethods(spec.listener).filter {
        it.access.isAbstract && !Methods.equalsByJavaDeclaration(it, listener.descriptor.callback)
      }.forEach {
        newMethod(ACC_PUBLIC, it) {
          if (it.returns == Types.BOOLEAN) {
            push(false)
          }
        }
      }
    }))
  }
}
