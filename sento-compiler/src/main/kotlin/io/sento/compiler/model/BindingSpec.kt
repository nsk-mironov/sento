package io.sento.compiler.model

import io.sento.compiler.GenerationEnvironment
import io.sento.compiler.annotations.ids
import io.sento.compiler.common.OptionalAware
import io.sento.compiler.common.Types
import io.sento.compiler.reflect.ClassSpec
import java.util.ArrayList

internal data class BindingSpec private constructor(
    public val bindings: Collection<BindTargetSpec>,
    public val listeners: Collection<ListenerTargetSpec>,
    public val views: Collection<ViewSpec>
) {
  public companion object {
    public fun from(clazz: ClassSpec, environment: GenerationEnvironment): BindingSpec {
      val optional = OptionalAware(clazz)
      val views = ArrayList<ViewSpec>()

      val bindings = createBindingTargets(clazz, optional, environment)
      val listeners = createListenerTargets(clazz, optional, environment)

      bindings.flatMapTo(views) {
        createViewSpecsForBindingTarget(it)
      }

      listeners.flatMapTo(views) {
        createViewSpecsForListenerTarget(it)
      }

      return BindingSpec(bindings, listeners, views)
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

    private fun createViewSpecsForBindingTarget(target: BindTargetSpec): Collection<ViewSpec> {
      return target.annotation.ids.map {
        ViewSpec(it, target.optional, target.clazz, ViewOwner.Field(target.field))
      }
    }

    private fun createViewSpecsForListenerTarget(target: ListenerTargetSpec): Collection<ViewSpec> {
      return target.annotation.ids.map {
        ViewSpec(it, target.optional, target.clazz, ViewOwner.Method(target.method))
      }
    }
  }
}

