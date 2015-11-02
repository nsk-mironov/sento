package io.sento.compiler.common

import org.objectweb.asm.Type

public fun Type.toSourceFilePath(): String {
  return if (className.contains('.')) {
    "${className.substring(className.lastIndexOf('.') + 1)}.java"
  } else {
    "$className.java"
  }
}

public fun Type.toClassFilePath(): String {
  return "$internalName.class"
}
