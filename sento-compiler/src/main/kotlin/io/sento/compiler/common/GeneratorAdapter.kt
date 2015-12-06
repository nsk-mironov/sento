package io.sento.compiler.common

import io.sento.compiler.reflection.ClassSpec
import io.sento.compiler.reflection.FieldSpec
import io.sento.compiler.reflection.MethodSpec
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.commons.Method

internal class GeneratorAdapter(visitor: ClassVisitor, access: Int, method: Method) : org.objectweb.asm.commons.GeneratorAdapter(Opcodes.ASM5, visitor.visitMethod(access, method.name, method.descriptor, null, null), access, method.name, method.descriptor) {
  public fun pushNull() {
    visitInsn(Opcodes.ACONST_NULL)
  }

  public fun checkCast(spec: ClassSpec) {
    checkCast(spec.type)
  }

  public fun getField(owner: ClassSpec, field: FieldSpec) {
    getField(owner.type, field.name, field.type)
  }

  public fun getField(owner: ClassSpec, name: String, type: ClassSpec) {
    getField(owner.type, name, type.type)
  }

  public fun getField(owner: ClassSpec, name: String, type: Type) {
    getField(owner.type, name, type)
  }

  public fun putField(owner: ClassSpec, field: FieldSpec) {
    putField(owner.type, field.name, field.type)
  }

  public fun putField(owner: ClassSpec, name: String, type: ClassSpec) {
    putField(owner.type, name, type.type)
  }

  public fun putField(owner: ClassSpec, name: String, type: Type) {
    putField(owner.type, name, type)
  }

  public fun invokeStatic(owner: ClassSpec, method: Method) {
    invokeStatic(owner.type, method)
  }

  public fun invokeStatic(owner: ClassSpec, method: MethodSpec) {
    invokeStatic(owner.type, Methods.get(method))
  }

  public fun invokeVirtual(owner: ClassSpec, method: Method) {
    invokeVirtual(owner.type, method)
  }

  public fun invokeVirtual(owner: ClassSpec, method: MethodSpec) {
    invokeVirtual(owner.type, Methods.get(method))
  }
}
