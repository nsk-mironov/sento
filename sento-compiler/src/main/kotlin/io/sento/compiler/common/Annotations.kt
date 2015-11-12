package io.sento.compiler.common

import com.google.common.reflect.AbstractInvocationHandler
import io.sento.compiler.model.AnnotationSpec
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.util.ArrayList

internal object Annotations {
  public inline fun <reified A : Annotation> create(spec: AnnotationSpec): A {
    return create(A::class.java, spec)
  }

  public fun <A : Annotation> create(clazz: Class<A>, spec: AnnotationSpec): A {
    return clazz.cast(Proxy.newProxyInstance(clazz.classLoader, arrayOf(clazz), object : AbstractInvocationHandler() {
      override fun handleInvocation(proxy: Any, method: Method, args: Array<out Any>): Any? {
        val value = spec.values[method.name]

        if (method.returnType.isArray) {
          val array = value.castSafe<Array<*>>().orEmpty()
          val list = ArrayList<Any?>()

          array.forEach {
            list.add(if (method.returnType.componentType.isAnnotation) {
              create(method.returnType.componentType.asSubclass(Annotation::class.java), it.cast<AnnotationSpec>())
            } else {
              it
            })
          }

          val returns = method.returnType.componentType.asSubclass(Any::class.java)
          val result = java.lang.reflect.Array.newInstance(returns, list.size).cast<Array<Any?>>()

          return list.toArray(result)
        }

        return value
      }

      override fun toString(): String {
        return "${clazz.name} ${spec.values}"
      }
    }))
  }

  public fun ids(annotation: AnnotationSpec): IntArray {
    val ids = annotation.value<IntArray>("value")
    val id = annotation.value<Int>("value")

    if (id != null) {
      return intArrayOf(id)
    }

    if (ids != null) {
      return ids
    }

    return IntArray(0)
  }

  public fun id(annotation: AnnotationSpec): Int {
    return annotation.value<Int>("value") ?: throw NoSuchFieldException("value")
  }

  private inline fun <reified T : Any> Any?.cast(): T {
    return this as T
  }

  private inline fun <reified T : Any> Any?.castSafe(): T? {
    return this as? T
  }
}
