package io.sento.compiler.model

import io.sento.compiler.reflection.ClassSpec
import org.objectweb.asm.Type

internal class SentoBindingSpec(
    public val binding: Type,
    public val target: Type
) {
  public companion object {
    public fun create(clazz: ClassSpec): SentoBindingSpec {
      return SentoBindingSpec(Type.getObjectType("${clazz.type.internalName}\$\$SentoBinding"), clazz.type)
    }
  }
}
