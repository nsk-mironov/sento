package io.sento.compiler.common

import org.objectweb.asm.Type

internal val Type.simpleName: String
  get() = if (className.contains(".")) {
    className.substring(className.lastIndexOf(".") + 1)
  } else {
    className
  }