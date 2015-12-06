package io.sento.compiler.common

import io.sento.compiler.reflection.MethodSpec
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.commons.Method

internal fun ClassWriter.newMethod(access: Int, method: Method, signature: String? = null, body: GeneratorAdapter.() -> Unit) {
  GeneratorAdapter(this, access, method).apply {
    body().apply {
      returnValue()
      endMethod()
    }
  }
}

internal fun ClassWriter.newMethod(access: Int, method: MethodSpec, body: GeneratorAdapter.() -> Unit) {
  GeneratorAdapter(this, access, Methods.get(method)).apply {
    body().apply {
      returnValue()
      endMethod()
    }
  }
}

