package io.sento.compiler.bindings

import io.sento.compiler.GenerationEnvironment
import io.sento.compiler.annotations.ids
import io.sento.compiler.common.GeneratorAdapter
import io.sento.compiler.common.Methods
import io.sento.compiler.common.Types
import io.sento.compiler.model.ListenerTargetSpec
import io.sento.compiler.model.ViewOwner
import io.sento.compiler.model.ViewSpec

internal class ListenerBinder() {
  public fun bind(targets: Collection<ListenerTargetSpec>, variables: VariablesContext, adapter: GeneratorAdapter, environment: GenerationEnvironment) {
    for (target in targets) {
      adapter.loadLocal(variables.target())
      adapter.newInstance(target.type, Methods.getConstructor(target.clazz)) {
        adapter.loadLocal(variables.target())
      }
      adapter.putField(target.clazz, environment.naming.getSyntheticFieldName(target), target.listener.listener)
    }

    for (target in targets) {
      for (id in target.annotation.ids) {
        adapter.newLabel().apply {
          val view = ViewSpec(id, target.optional, target.clazz, ViewOwner.Method(target.method))
          val name = environment.naming.getSyntheticFieldName(view)

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
          adapter.getField(target.clazz, environment.naming.getSyntheticFieldName(target), target.listener.listener)

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
          val name = environment.naming.getSyntheticFieldName(view)

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
            adapter.getField(target.clazz, environment.naming.getSyntheticFieldName(target), target.listener.listener)
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
      adapter.putField(target.clazz, environment.naming.getSyntheticFieldName(target), target.listener.listener)
    }
  }
}
