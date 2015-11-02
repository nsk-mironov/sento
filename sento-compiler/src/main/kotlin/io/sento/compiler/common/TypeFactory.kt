package io.sento.compiler.common

import org.objectweb.asm.Type
import java.util.concurrent.atomic.AtomicInteger

public class TypeFactory(private val type: Type) {
  private val counter = AtomicInteger(0)

  public fun newAnonymousType(): Type {
    return Type.getObjectType("${type.internalName}\$\$${counter.andIncrement}")
  }
}
