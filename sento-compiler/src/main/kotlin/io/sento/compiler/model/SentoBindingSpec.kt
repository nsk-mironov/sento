package io.sento.compiler.model

import io.sento.compiler.common.TypeFactory
import org.objectweb.asm.Type

internal class SentoBindingSpec(
    public val clazz: ClassSpec,
    public val generatedType: Type,
    public val originalType: Type,
    public val factory: TypeFactory
) {
  public companion object {
    public fun create(clazz: ClassSpec): SentoBindingSpec {
      val originalType = clazz.type
      val generatedType = Type.getObjectType("${originalType.internalName}\$\$SentoBinding")
      val factory = TypeFactory(generatedType)

      return SentoBindingSpec(clazz, generatedType, originalType, factory)
    }
  }
}
