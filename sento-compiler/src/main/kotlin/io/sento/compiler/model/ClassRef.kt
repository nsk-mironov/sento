package io.sento.compiler.model

import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

internal data class ClassRef(
    public val type: Type,
    public val parent: Type,
    public val access: Int
) {
  public val isInterface: Boolean
    get() = access and Opcodes.ACC_INTERFACE != 0
}
