package io.sento.compiler.common

import io.sento.compiler.reflection.ClassSpec
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

  public fun getConstructor(): Method {
    return Method("<init>", Type.VOID_TYPE, emptyArray())
  }

  public fun getConstructor(first: Type, vararg args: Type): Method {
    return Method("<init>", Type.VOID_TYPE, arrayOf(first) + args)
  }

  public fun getConstructor(first: ClassSpec, vararg args: ClassSpec): Method {
    return Method("<init>", Type.VOID_TYPE, arrayOf(first.type) + args.map { it.type })
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
