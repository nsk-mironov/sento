package io.sento.compiler.common

import io.sento.compiler.reflection.MethodSpec
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.commons.GeneratorAdapter
import org.objectweb.asm.commons.Method

internal inline fun ClassWriter.newMethod(access: Int, method: Method, signature: String? = null, body: GeneratorAdapter.() -> Unit) {
  GeneratorAdapter(access, method, signature, null, this).apply {
    body().apply {
      returnValue()
      endMethod()
    }
  }
}

internal inline fun ClassWriter.newMethod(access: Int, method: MethodSpec, body: GeneratorAdapter.() -> Unit) {
  GeneratorAdapter(access, Methods.get(method), method.signature, null, this).apply {
    body().apply {
      returnValue()
      endMethod()
    }
  }
}

