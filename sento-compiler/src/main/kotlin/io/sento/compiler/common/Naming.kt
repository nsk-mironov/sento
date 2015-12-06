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

  public fun getSyntheticAccessor(owner: ClassSpec, method: MethodSpec): Method {
    return Methods.get("sento\$accessor\$${method.name}", method.returns, *arrayOf(owner.type, *method.arguments))
  }

  public fun getSentoBindingType(spec: ClassSpec): Type {
    return Type.getObjectType("${spec.type.internalName}\$\$SentoBinding");
  }

  public fun getAnonymousType(type: Type): Type {
    return Type.getObjectType("${type.internalName}\$\$${anonymous.getOrPut(type) { AtomicInteger() }.andIncrement}")
  }

  public fun getSyntheticFieldNameForViewTarget(target: ViewSpec): String {
    return "sento\$view\$id_${target.id}"
  }

  public fun getSyntheticFieldNameForMethodTarget(target: ListenerTargetSpec): String {
    return "sento\$listener\$${target.method.name}\$${target.annotation.type.simpleName}"
  }
}
