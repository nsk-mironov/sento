package io.sento.compiler.common

import org.objectweb.asm.ClassWriter
import org.objectweb.asm.commons.GeneratorAdapter
import org.objectweb.asm.commons.Method

internal inline fun ClassWriter.method(access: Int, method: Method, signature: String? = null, body: GeneratorAdapter.() -> Unit) {
  GeneratorAdapter(access, method, signature, null, this).apply {
    body().apply {
      returnValue()
      endMethod()
    }
  }
}
