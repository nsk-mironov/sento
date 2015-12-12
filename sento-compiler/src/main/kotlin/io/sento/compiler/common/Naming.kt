package io.sento.compiler.common

import io.sento.compiler.model.ListenerTargetSpec
import io.sento.compiler.model.ViewSpec
import io.sento.compiler.reflection.ClassSpec
import io.sento.compiler.reflection.MethodSpec
import org.objectweb.asm.Type
import org.objectweb.asm.commons.Method
import java.util.HashMap
import java.util.concurrent.atomic.AtomicInteger

internal class Naming {
  private val anonymous = HashMap<Type, AtomicInteger>()

  public fun getBindingType(spec: ClassSpec): Type {
    return Type.getObjectType("${spec.type.internalName}\$\$SentoBinding");
  }

  public fun getAnonymousType(type: Type): Type {
    return Type.getObjectType("${type.internalName}\$\$${anonymous.getOrPut(type) { AtomicInteger() }.andIncrement}")
  }

  public fun getSyntheticAccessor(owner: ClassSpec, method: MethodSpec): Method {
    return getSyntheticAccessor(owner, method, getSyntheticAccessorName(owner, method))
  }

  public fun getSyntheticAccessor(owner: ClassSpec, method: MethodSpec, name: String): Method {
    return Methods.get(name, method.returns, *arrayOf(owner.type, *method.arguments))
  }

  public fun getSyntheticAccessorName(owner: ClassSpec, method: MethodSpec): String {
    return "sento\$accessor\$${method.name}"
  }

  public fun getSyntheticFieldName(target: ViewSpec): String {
    return "sento\$view\$id_${target.id}"
  }

  public fun getSyntheticFieldName(target: ListenerTargetSpec): String {
    return "sento\$listener\$${target.method.name}\$${target.annotation.type.simpleName}"
  }
}
