package io.sento.compiler

import io.sento.compiler.common.Types
import org.objectweb.asm.ClassWriter

internal class ClassRegistryAwareClassWriter(private val registry: ClassRegistry) : ClassWriter(ClassWriter.COMPUTE_FRAMES + ClassWriter.COMPUTE_MAXS) {
  override fun getCommonSuperClass(left: String, right: String): String {
    // TODO: Implement me!!!
    return Types.OBJECT.internalName
  }
}
