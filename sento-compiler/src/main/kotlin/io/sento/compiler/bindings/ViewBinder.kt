package io.sento.compiler.bindings

import io.sento.compiler.GenerationEnvironment
import io.sento.compiler.annotations.id
import io.sento.compiler.common.GeneratorAdapter
import io.sento.compiler.common.Types
import io.sento.compiler.model.BindTargetSpec

internal class ViewBinder {
  public fun bind(targets: Collection<BindTargetSpec>, variables: VariablesContext, adapter: GeneratorAdapter, environment: GenerationEnvironment) {
    targets.forEach {
      adapter.loadLocal(variables.target())
      adapter.loadLocal(variables.view(it.annotation.id))

      if (it.field.type != Types.VIEW) {
        adapter.checkCast(it.field.type)
      }

      adapter.putField(it.clazz, it.field)
    }
  }

  public fun unbind(targets: Collection<BindTargetSpec>, variables: VariablesContext, adapter: GeneratorAdapter, environment: GenerationEnvironment) {
    targets.forEach {
      adapter.loadLocal(variables.target())
      adapter.pushNull()
      adapter.putField(it.clazz, it.field)
    }
  }
}
