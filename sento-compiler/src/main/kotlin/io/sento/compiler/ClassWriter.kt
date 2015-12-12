package io.sento.compiler

import io.sento.compiler.common.GeneratorAdapter
import io.sento.compiler.common.Methods
import io.sento.compiler.common.Types
import io.sento.compiler.reflection.MethodSpec
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.Opcodes.V1_6
import org.objectweb.asm.Type
import org.objectweb.asm.commons.Method

internal class ClassWriter(private val environment: GenerationEnvironment) : org.objectweb.asm.ClassWriter((ClassWriter.COMPUTE_FRAMES + ClassWriter.COMPUTE_MAXS)) {
  override fun getCommonSuperClass(left: String, right: String): String {
    return Types.OBJECT.internalName
  }

  public fun visit(access: Int, name: Type, signature: String? = null, parent: Type = Types.OBJECT, interfaces: Array<out Type> = emptyArray()) {
    visit(V1_6, access, name.internalName, signature, parent.internalName, interfaces.map { it.internalName }.toTypedArray())
  }

  public fun visitField(access: Int, name: String, type: Type, signature: String? = null): FieldVisitor {
    return visitField(access, name, type.descriptor, signature, null)
  }

  public fun newMethod(access: Int, method: Method, signature: String? = null, body: GeneratorAdapter.() -> Unit) {
    GeneratorAdapter(this, access, method, signature).apply {
      body().apply {
        returnValue()
        endMethod()
      }
    }
  }

  public fun newMethod(access: Int, method: MethodSpec, body: GeneratorAdapter.() -> Unit) {
    GeneratorAdapter(this, access, Methods.get(method), method.signature).apply {
      body().apply {
        returnValue()
        endMethod()
      }
    }
  }

  public fun newMethod(method: MethodSpec, body: GeneratorAdapter.() -> Unit) {
    newMethod(method.access, method, body)
  }
}
