package io.sento.compiler.common

import com.google.common.reflect.AbstractInvocationHandler
import io.sento.compiler.model.AnnotationSpec
import java.lang.reflect.Method
import java.lang.reflect.Proxy

internal object Annotations {
  public inline fun <reified A : Annotation> create(values: Map<String, Any?>): A {
    return create(A::class.java, values)
  }

  public fun <A : Annotation> create(clazz: Class<A>, values: Map<String, Any?>): A {
    return clazz.cast(Proxy.newProxyInstance(clazz.classLoader, arrayOf(clazz), object : AbstractInvocationHandler() {
      override fun handleInvocation(proxy: Any, method: Method, args: Array<out Any>): Any? {
        return values[method.name]
      }

      override fun toString(): String {
        return "${clazz.name} $values"
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
}
