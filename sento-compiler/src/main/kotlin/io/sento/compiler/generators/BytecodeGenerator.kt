package io.sento.compiler.generators

import io.sento.compiler.ClassRegistry
import io.sento.compiler.specs.ClassSpec

internal interface BytecodeGenerator {
  public fun shouldGenerateBytecode(clazz: ClassSpec, registry: ClassRegistry): Boolean

  public fun onGenerateBytecode(clazz: ClassSpec, registry: ClassRegistry): ByteArray
}
