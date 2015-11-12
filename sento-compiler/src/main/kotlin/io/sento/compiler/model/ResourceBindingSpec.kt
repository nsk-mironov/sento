package io.sento.compiler.model

import io.sento.ResourceBinding
import io.sento.ResourceBindings
import io.sento.compiler.ClassRegistry
import io.sento.compiler.common.Types
import org.objectweb.asm.Type

internal data class ResourceBindingSpec(
    public val annotation: ClassSpec,
    public val type: Type,
    public val getter: MethodSpec
) {
  public companion object {
    public fun create(annotation: ClassSpec, binding: ResourceBinding, registry: ClassRegistry): ResourceBindingSpec {
      val type = Types.getClassType(binding.type)
      val resources = registry.resolve(Types.TYPE_RESOURCES)

      return ResourceBindingSpec(annotation, type, resources.method(binding.getter)!!)
    }

    public fun create(annotation: ClassSpec, binding: ResourceBindings, registry: ClassRegistry): Collection<ResourceBindingSpec> {
      return binding.value.map {
        create(annotation, it, registry)
      }
    }
  }
}
