package io.sento.compiler

import io.sento.Binding
import io.sento.Finder
import org.objectweb.asm.Type

internal object Types {
  public val TYPE_OBJECT = Type.getType(Any::class.java)
  public val TYPE_BINDING = Type.getType(Binding::class.java)
  public val TYPE_FINDER = Type.getType(Finder::class.java)
}
