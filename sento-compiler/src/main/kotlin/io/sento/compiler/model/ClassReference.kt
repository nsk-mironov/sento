package io.sento.compiler.model

import io.sento.compiler.common.Opener
import io.sento.compiler.visitors.ClassSpecVisitor
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import java.util.concurrent.atomic.AtomicReference

internal data class ClassReference(
    public val access: Int,
    public val type: Type,
    public val parent: Type,
    public val interfaces: Collection<Type>,
    public val opener: Opener
) {
  public val isInterface: Boolean
    get() = access and Opcodes.ACC_INTERFACE != 0

  public val isAnnotation: Boolean
    get() = access and Opcodes.ACC_ANNOTATION != 0

  public fun resolve(): ClassSpec {
    val reader = ClassReader(opener.open())
    val result = AtomicReference<ClassSpec>()

    reader.accept(ClassSpecVisitor(access, type, parent, interfaces, opener) {
      result.set(it)
    }, 0)

    return result.get()
  }
}
