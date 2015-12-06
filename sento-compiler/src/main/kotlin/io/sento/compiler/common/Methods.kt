package io.sento.compiler.common

import io.sento.compiler.reflection.MethodSpec
import org.objectweb.asm.Type
import org.objectweb.asm.commons.Method

internal object Methods {
  public fun get(spec: MethodSpec): Method {
    return Method(spec.name, spec.type.descriptor)
  }

  public fun get(name: String, returns: Type, vararg args: Type): Method {
    return Method(name, returns, args)
  }

  public fun getConstructor(vararg args: Type): Method {
    return Method("<init>", Type.VOID_TYPE, args)
  }

  public fun getStaticConstructor(): Method {
    return Method("<clinit>", Type.VOID_TYPE, emptyArray())
  }

  public fun asJavaDeclaration(spec: MethodSpec): String {
    return "${spec.name}(${spec.arguments.map { it.className }.joinToString(", ")})"
  }

  public fun equalsByJavaDeclaration(left: MethodSpec, right: MethodSpec): Boolean {
    return left.name == right.name && left.type.descriptor == right.type.descriptor
  }
}
