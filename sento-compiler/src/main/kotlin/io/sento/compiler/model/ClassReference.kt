package io.sento.compiler.model

import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

internal data class ClassReference(
    public val type: Type,
    public val parent: Type,
    public val access: Int
) {
  public val isInterface: Boolean
    get() = access and Opcodes.ACC_INTERFACE != 0

  public val isAnnotation: Boolean
    get() = access and Opcodes.ACC_ANNOTATION != 0
}
