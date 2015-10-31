package io.sento.compiler.generators

import io.sento.compiler.GenerationEnvironment
import io.sento.compiler.specs.ClassSpec

internal interface BytecodeGenerator {
  public fun shouldGenerateBytecode(clazz: ClassSpec, environment: GenerationEnvironment): Boolean

  public fun onGenerateBytecode(clazz: ClassSpec, environment: GenerationEnvironment): ByteArray
}
