package io.sento.compiler.common

import org.objectweb.asm.Type

internal fun Type.toSourceFilePath(): String {
  return if (className.contains('.')) {
    "${className.substring(className.lastIndexOf('.') + 1)}.java"
  } else {
    "$className.java"
  }
}

internal fun Type.toClassFilePath(): String {
  return "$internalName.class"
}
