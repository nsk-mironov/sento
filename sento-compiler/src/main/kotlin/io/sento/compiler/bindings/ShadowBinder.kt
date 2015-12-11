package io.sento.compiler.bindings

import io.sento.compiler.GenerationEnvironment
import io.sento.compiler.common.GeneratorAdapter
import io.sento.compiler.common.Types
import io.sento.compiler.model.ViewSpec

internal class ShadowBinder {
  public fun bind(targets: Collection<ViewSpec>, variables: VariablesContext, adapter: GeneratorAdapter, environment: GenerationEnvironment) {
    targets.distinctBy { it.id }.forEach {
      adapter.loadLocal(variables.target())
      adapter.loadLocal(variables.view(it.id))
      adapter.putField(it.clazz, environment.naming.getSyntheticFieldName(it), Types.VIEW)
    }
  }

  public fun unbind(targets: Collection<ViewSpec>, variables: VariablesContext, adapter: GeneratorAdapter, environment: GenerationEnvironment) {
    targets.distinctBy { it.id }.forEach {
      adapter.loadLocal(variables.target())
      adapter.pushNull()
      adapter.putField(it.clazz, environment.naming.getSyntheticFieldName(it), Types.VIEW)
    }
  }
}
