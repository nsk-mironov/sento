package io.sento.annotations

import com.google.common.reflect.AbstractInvocationHandler
import io.sento.compiler.model.AnnotationSpec
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.util.HashMap

internal object AnnotationProxy {
  public fun <A> create(clazz: Class<A>, spec: AnnotationSpec): A {
    return clazz.cast(Proxy.newProxyInstance(clazz.classLoader, arrayOf(clazz), object : AbstractInvocationHandler() {
      private val cache = HashMap<String, Any?>()

      init {
        for ((key, value) in spec.values) {
          if (value != null) {
            cache.put(key, resolve(clazz.getMethod(key).returnType, value))
          }
        }
      }

      override fun handleInvocation(proxy: Any, method: Method, args: Array<out Any>): Any? {
        return cache[method.name]
      }

      override fun toString(): String {
        return "@${clazz.canonicalName}(${cache.map { "${it.key}=${it.value}" }.joinToString(", ")})"
      }
    }))
  }

  private fun resolve(type: Class<*>, value: Any): Any {
    return when {
      isArray(type) -> resolveArray(type.componentType, value)
      isAnnotation(type) -> resolveAnnotation(type, value)
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
    return create(type, value.cast())
  }

  private fun resolveValue(type: Class<*>, value: Any): Any {
    return value
  }

  private fun isArray(type: Class<*>): Boolean {
    return type.isArray
  }

  private fun isAnnotation(type: Class<*>): Boolean {
    return type.isAnnotation || type.getAnnotation(AnnotationDelegate::class.java) != null
  }

  private inline fun <reified T : Any> Any?.cast(): T {
    return this as T
  }
}
