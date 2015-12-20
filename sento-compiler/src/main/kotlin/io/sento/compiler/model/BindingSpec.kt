package io.sento.compiler.model

import io.sento.compiler.GenerationEnvironment
import io.sento.compiler.common.OptionalAware
import io.sento.compiler.common.Types
import io.sento.compiler.reflect.ClassSpec

internal data class BindingSpec private constructor(
    public val clazz: ClassSpec,
    public val bindings: Collection<BindTargetSpec>,
    public val listeners: Collection<ListenerTargetSpec>,
    public val views: Collection<ViewSpec>
) {
  public companion object {
    public fun from(clazz: ClassSpec, environment: GenerationEnvironment): BindingSpec {
      val optional = OptionalAware(clazz)

      val bindings = createBindingTargets(clazz, optional, environment)
      val listeners = createListenerTargets(clazz, optional, environment)
      val views = bindings.flatMap { it.views } + listeners.flatMap { it.views }

      return BindingSpec(clazz, bindings, listeners, views)
    }

    private fun createBindingTargets(clazz: ClassSpec, optional: OptionalAware, environment: GenerationEnvironment): Collection<BindTargetSpec> {
      return clazz.fields.flatMap { field ->
        field.annotations.filter { it.type == Types.BIND }.map { annotation ->
          BindTargetSpec.create(clazz, field, annotation, optional.isOptional(field), environment)
        }
      }
    }

    private fun createListenerTargets(clazz: ClassSpec, optional: OptionalAware, environment: GenerationEnvironment): Collection<ListenerTargetSpec> {
      return clazz.methods.flatMap { method ->
        method.annotations.mapNotNull { annotation ->
          environment.registry.resolveListenerClassSpec(annotation)?.let {
            ListenerTargetSpec.create(clazz, method, annotation, optional.isOptional(method), environment)
          }
        }
      }
    }
  }
}

