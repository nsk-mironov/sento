package io.sento.compiler.common

import org.objectweb.asm.Opcodes

internal val Int.isInterface: Boolean
  get() = this and Opcodes.ACC_INTERFACE != 0

internal val Int.isAnnotation: Boolean
  get() = this and Opcodes.ACC_ANNOTATION != 0

internal val Int.isAbstract: Boolean
  get() = this and Opcodes.ACC_ABSTRACT != 0
