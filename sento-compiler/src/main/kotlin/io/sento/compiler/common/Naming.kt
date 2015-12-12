package io.sento.compiler.common

import io.sento.compiler.model.ListenerTargetSpec
import io.sento.compiler.model.ViewSpec
import io.sento.compiler.reflection.ClassSpec
import io.sento.compiler.reflection.MethodSpec
import org.objectweb.asm.Opcodes.ACC_PUBLIC
import org.objectweb.asm.Opcodes.ACC_STATIC
import org.objectweb.asm.Opcodes.ACC_SYNTHETIC
import org.objectweb.asm.Type
import org.objectweb.asm.commons.Method
import java.util.HashMap
import java.util.concurrent.atomic.AtomicInteger

internal class Naming {
  private val anonymous = HashMap<Type, AtomicInteger>()

  private companion object {
    private val METHOD_BIND_DESCRIPTOR = Type.getMethodType(Types.VOID, Types.OBJECT, Types.OBJECT, Types.FINDER)
    private val METHOD_BIND_SIGNATURE = "<S:Ljava/lang/Object;>(Ljava/lang/Object;TS;Lio/sento/Finder<-TS;>;)V"

    private val METHOD_BIND_SYNTHETIC_SPEC = MethodSpec(ACC_PUBLIC + ACC_STATIC + ACC_SYNTHETIC, "sento\$bind", METHOD_BIND_DESCRIPTOR, METHOD_BIND_SIGNATURE, emptyList())
    private val METHOD_BIND_SPEC = MethodSpec(ACC_PUBLIC, "bind", METHOD_BIND_DESCRIPTOR, METHOD_BIND_SIGNATURE, emptyList())

    private val METHOD_UNBIND_DESCRIPTOR = Type.getMethodType(Types.VOID, Types.OBJECT)
    private val METHOD_UNBIND_SIGNATURE = null

    private val METHOD_UNBIND_SYNTHETIC_SPEC = MethodSpec(ACC_PUBLIC + ACC_STATIC + ACC_SYNTHETIC, "sento\$bind", METHOD_UNBIND_DESCRIPTOR, METHOD_UNBIND_SIGNATURE, emptyList())
    private val METHOD_UNBIND_SPEC = MethodSpec(ACC_PUBLIC, "bind", METHOD_UNBIND_DESCRIPTOR, METHOD_UNBIND_SIGNATURE, emptyList())
  }

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

  public fun getBindMethodSpec(): MethodSpec {
    return METHOD_BIND_SPEC
  }

  public fun getUnbindMethodSpec(): MethodSpec {
    return METHOD_UNBIND_SPEC
  }

  public fun getSyntheticBindMethodSpec(): MethodSpec {
    return METHOD_BIND_SYNTHETIC_SPEC
  }

  public fun getSyntheticUnbindMethodSpec(): MethodSpec {
    return METHOD_UNBIND_SYNTHETIC_SPEC
  }

  public fun getSyntheticFieldName(target: ViewSpec): String {
    return "sento\$view\$id_${target.id}"
  }

  public fun getSyntheticFieldName(target: ListenerTargetSpec): String {
    return "sento\$listener\$${target.method.name}\$${target.annotation.type.simpleName}"
  }
}
