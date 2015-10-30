package com.github.vmironov.sento.compiler.generators

import com.github.vmironov.sento.compiler.ClassRegistry
import com.github.vmironov.sento.compiler.specs.ClassSpec

internal interface BytecodeGenerator {
  public fun shouldGenerateBytecode(clazz: ClassSpec, registry: ClassRegistry): Boolean

  public fun onGenerateBytecode(clazz: ClassSpec, registry: ClassRegistry): ByteArray
}
