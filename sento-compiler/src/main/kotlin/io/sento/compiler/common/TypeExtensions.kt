package io.sento.compiler.common

import org.objectweb.asm.Type

internal fun Type.toClassFilePath(): String {
  return "$internalName.class"
}
