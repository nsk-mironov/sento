package io.sento.compiler.common

import com.google.common.reflect.AbstractInvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

internal object AnnotationProxy {
  public fun <A : Annotation> create(clazz: Class<A>, values: Map<String, Any?>): A {
    return clazz.cast(Proxy.newProxyInstance(clazz.classLoader, arrayOf(clazz), object : AbstractInvocationHandler() {
      override fun handleInvocation(proxy: Any, method: Method, args: Array<out Any>): Any? {
        return values[method.name]
      }
    }))
  }
}
