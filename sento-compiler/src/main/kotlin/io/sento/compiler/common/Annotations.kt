package io.sento.compiler.common

import com.google.common.reflect.AbstractInvocationHandler
import io.sento.compiler.model.AnnotationSpec
import java.lang.reflect.Method
import java.lang.reflect.Proxy

internal object Annotations {
  public fun <A : Annotation> create(clazz: Class<A>, spec: AnnotationSpec): A {
    return clazz.cast(Proxy.newProxyInstance(clazz.classLoader, arrayOf(clazz), object : AbstractInvocationHandler() {
      override fun handleInvocation(proxy: Any, method: Method, args: Array<out Any>): Any? {
        return resolve(method.returnType, spec.values[method.name] ?: return null)
      }

      override fun toString(): String {
        return "@${clazz.name} (${spec.values})"
      }

      private fun resolve(type: Class<*>, value: Any): Any {
        return when {
          type.isArray -> resolveArray(type.componentType, value)
          type.isAnnotation -> resolveAnnotation(type, value)
          else -> resolveValue(type, value)
        }
      }

      private fun resolveArray(type: Class<*>, value: Any): Any {
        val array = java.lang.reflect.Array.newInstance(type, 0).cast<Array<Any?>>()

        val list = value.cast<Array<Any>>().orEmpty().map {
          resolve(type, it)
        }

        return list.toArrayList().toArray(array)
      }

      private fun resolveAnnotation(type: Class<*>, value: Any): Any {
        return create(type.asSubclass(Annotation::class.java), value.cast())
      }

      private fun resolveValue(type: Class<*>, value: Any): Any {
        return value
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
}
