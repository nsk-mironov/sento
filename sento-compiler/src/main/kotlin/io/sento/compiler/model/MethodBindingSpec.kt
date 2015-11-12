package io.sento.compiler.model

import io.sento.MethodBinding
import io.sento.compiler.ClassRegistry
import io.sento.compiler.common.Types
import io.sento.compiler.common.isAbstract
import org.objectweb.asm.Type

internal data class MethodBindingSpec(
    public val annotation: ClassSpec,
    public val owner: Type,
    public val listener: Type,
    public val setter: MethodSpec,
    public val callback: MethodSpec
) {
  public companion object {
    public fun create(annotation: ClassSpec, binding: MethodBinding, registry: ClassRegistry): MethodBindingSpec {
      val owner = Types.getClassType(binding.owner)
      val listener = Types.getClassType(binding.listener)

      val setter = registry.resolve(owner).method(binding.setter)!!
      val callback = registry.resolve(listener).methods.first {
        it.access.isAbstract
      }

      return MethodBindingSpec(annotation, owner, listener, setter, callback)
    }
  }
}
