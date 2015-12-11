package io.sento.compiler.bindings

import io.sento.compiler.GeneratedContent
import io.sento.compiler.GenerationEnvironment
import io.sento.compiler.annotations.ids
import io.sento.compiler.common.GeneratorAdapter
import io.sento.compiler.common.Methods
import io.sento.compiler.common.Types
import io.sento.compiler.common.isAbstract
import io.sento.compiler.common.isInterface
import io.sento.compiler.common.isPrivate
import io.sento.compiler.common.newMethod
import io.sento.compiler.model.ListenerTargetSpec
import io.sento.compiler.model.ViewOwner
import io.sento.compiler.model.ViewSpec
import org.objectweb.asm.Opcodes.ACC_FINAL
import org.objectweb.asm.Opcodes.ACC_PRIVATE
import org.objectweb.asm.Opcodes.ACC_PUBLIC
import org.objectweb.asm.Opcodes.ACC_SUPER
import org.objectweb.asm.Opcodes.V1_6

internal class ListenerBinder() {
  public fun bind(targets: Collection<ListenerTargetSpec>, variables: VariablesContext, adapter: GeneratorAdapter, environment: GenerationEnvironment) {
    for (target in targets) {
      adapter.loadLocal(variables.target())
      adapter.newInstance(target.type, Methods.getConstructor(target.clazz)) {
        adapter.loadLocal(variables.target())
      }
      adapter.putField(target.clazz, environment.naming.getSyntheticFieldNameForMethodTarget(target), target.listener.listener)
    }

    for (target in targets) {
      for (id in target.annotation.ids) {
        adapter.newLabel().apply {
          val view = ViewSpec(id, target.optional, target.clazz, ViewOwner.Method(target.method))
          val name = environment.naming.getSyntheticFieldNameForViewTarget(view)

          if (target.optional) {
            adapter.loadLocal(variables.target())
            adapter.getField(target.clazz, name, Types.VIEW)
            adapter.ifNull(this)
          }

          adapter.loadLocal(variables.target())
          adapter.getField(target.clazz, name, Types.VIEW)

          if (target.listener.owner.type != Types.VIEW) {
            adapter.checkCast(target.listener.owner)
          }

          adapter.loadLocal(variables.target())
          adapter.getField(target.clazz, environment.naming.getSyntheticFieldNameForMethodTarget(target), target.listener.listener)

          adapter.invokeVirtual(target.listener.owner, target.listener.setter)
          adapter.mark(this)
        }
      }
    }
  }

  public fun unbind(targets: Collection<ListenerTargetSpec>, variables: VariablesContext, adapter: GeneratorAdapter, environment: GenerationEnvironment) {
    for (target in targets) {
      for (id in target.annotation.ids) {
        adapter.newLabel().apply {
          val view = ViewSpec(id, target.optional, target.clazz, ViewOwner.Method(target.method))
          val name = environment.naming.getSyntheticFieldNameForViewTarget(view)

          if (target.optional) {
            adapter.loadLocal(variables.target())
            adapter.getField(target.clazz, name, Types.VIEW)
            adapter.ifNull(this)
          }

          adapter.loadLocal(variables.target())
          adapter.getField(target.clazz, name, Types.VIEW)

          if (target.listener.owner.type != Types.VIEW) {
            adapter.checkCast(target.listener.owner)
          }

          if (target.listener.setter != target.listener.unsetter) {
            adapter.loadLocal(variables.target())
            adapter.getField(target.clazz, environment.naming.getSyntheticFieldNameForMethodTarget(target), target.listener.listener)
          }

          if (target.listener.setter == target.listener.unsetter) {
            adapter.pushNull()
          }

          adapter.invokeVirtual(target.listener.owner, target.listener.unsetter)
          adapter.mark(this)
        }
      }
    }

    for (target in targets) {
      adapter.loadLocal(variables.target())
      adapter.pushNull()
      adapter.putField(target.clazz, environment.naming.getSyntheticFieldNameForMethodTarget(target), target.listener.listener)
    }
  }

  public fun generate(target: ListenerTargetSpec, environment: GenerationEnvironment): List<GeneratedContent> {
    return listOf(GeneratedContent(Types.getClassFilePath(target.type), environment.newClass {
      val parent = if (target.listener.listener.access.isInterface) Types.OBJECT else target.listener.listener.type
      val interfaces = if (target.listener.listener.access.isInterface) arrayOf(target.listener.listener.type) else emptyArray()

      visit(V1_6, ACC_PUBLIC + ACC_SUPER, target.type.internalName, null, parent.internalName, interfaces.map { it.internalName }.toTypedArray())
      visitField(ACC_PRIVATE + ACC_FINAL, "target", target.clazz.type.descriptor, null, null)

      newMethod(ACC_PUBLIC, Methods.getConstructor(target.clazz)) {
        loadThis()
        invokeConstructor(parent, Methods.getConstructor())

        loadThis()
        loadArg(0)
        putField(target.type, "target", target.clazz.type)
      }

      newMethod(ACC_PUBLIC, target.listener.callback) {
        loadThis()
        getField(target.type, "target", target.clazz.type)

        target.arguments.forEach {
          loadArg(it.index).apply {
            if (!Types.isPrimitive(it.type)) {
              checkCast(it.type)
            }
          }
        }

        if (target.method.access.isPrivate) {
          invokeStatic(target.clazz, environment.naming.getSyntheticAccessor(target.clazz, target.method))
        } else {
          invokeVirtual(target.clazz, target.method)
        }

        if (target.listener.callback.returns == Types.BOOLEAN && target.method.returns == Types.VOID) {
          push(false)
        }
      }

      environment.registry.listPublicMethods(target.listener.listener).filter {
        it.access.isAbstract && !Methods.equalsByJavaDeclaration(it, target.listener.callback)
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
