package io.sento.compiler.model

import io.sento.compiler.common.TypeFactory
import org.objectweb.asm.Type

internal class BindingSpec(
    public val clazz: ClassSpec,
    public val generatedType: Type,
    public val originalType: Type,
    public val factory: TypeFactory
) {
  public companion object {
    public fun from(clazz: ClassSpec): BindingSpec {
      val originalType = clazz.type
      val generatedType = Type.getObjectType("${originalType.internalName}\$\$SentoBinding")
      val factory = TypeFactory(generatedType)

      return BindingSpec(clazz, generatedType, originalType, factory)
    }
  }
}
