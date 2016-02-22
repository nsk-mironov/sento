package io.mironov.sento.compiler.common

import io.mironov.sento.compiler.model.ListenerTargetSpec
import io.mironov.sento.compiler.model.ViewSpec
import io.mironov.sento.compiler.reflect.ClassSpec
import io.mironov.sento.compiler.reflect.MethodSpec
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
    private val METHOD_BIND_SPEC = MethodSpec(ACC_PUBLIC, "bind", Type.getMethodType(Types.VOID, Types.OBJECT, Types.OBJECT, Types.FINDER))
    private val METHOD_BIND_SYNTHETIC_SPEC = MethodSpec(ACC_PUBLIC + ACC_STATIC + ACC_SYNTHETIC, "sento\$bind", METHOD_BIND_SPEC.type)

    private val METHOD_UNBIND_SPEC = MethodSpec(ACC_PUBLIC, "unbind", Type.getMethodType(Types.VOID, Types.OBJECT))
    private val METHOD_UNBIND_SYNTHETIC_SPEC = MethodSpec(ACC_PUBLIC + ACC_STATIC + ACC_SYNTHETIC, "sento\$unbind", METHOD_UNBIND_SPEC.type)
  }

  fun getBindingType(spec: ClassSpec): Type {
    return Type.getObjectType("${spec.type.internalName}\$SentoBinding");
  }

  fun getAnonymousType(type: Type): Type {
    return Type.getObjectType("${type.internalName}\$${anonymous.getOrPut(type) { AtomicInteger() }.andIncrement}")
  }

  fun getSyntheticAccessor(owner: ClassSpec, method: MethodSpec): Method {
    return getSyntheticAccessor(owner, method, getSyntheticAccessorName(owner, method))
  }

  fun getSyntheticAccessor(owner: ClassSpec, method: MethodSpec, name: String): Method {
    return Methods.get(name, method.returns, *arrayOf(owner.type, *method.arguments))
  }

  fun getSyntheticAccessorName(owner: ClassSpec, method: MethodSpec): String {
    return "sento\$accessor\$${method.name}"
  }

  fun getBindMethodSpec(): MethodSpec {
    return METHOD_BIND_SPEC
  }

  fun getUnbindMethodSpec(): MethodSpec {
    return METHOD_UNBIND_SPEC
  }

  fun getSyntheticBindMethodSpec(): MethodSpec {
    return METHOD_BIND_SYNTHETIC_SPEC
  }

  fun getSyntheticUnbindMethodSpec(): MethodSpec {
    return METHOD_UNBIND_SYNTHETIC_SPEC
  }

  fun getSyntheticFieldName(target: ViewSpec): String {
    return "sento\$view\$id_${target.id}"
  }

  fun getSyntheticFieldName(target: ListenerTargetSpec): String {
    return "sento\$listener\$${target.method.name}\$${target.annotation.type.simpleName}"
  }
}
