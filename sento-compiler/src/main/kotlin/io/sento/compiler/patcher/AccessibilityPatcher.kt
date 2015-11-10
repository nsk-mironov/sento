package io.sento.compiler.patcher

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

public open class AccessibilityPatcher : ClassPatcher {
  override fun patch(bytes: ByteArray): ByteArray {
    val reader = ClassReader(bytes)
    val writer = ClassWriter(0)

    reader.accept(object : ClassVisitor(Opcodes.ASM5, writer) {
      override fun visitField(access: Int, name: String, desc: String, signature: String?, value: Any?): FieldVisitor? {
        return super.visitField(onPatchFieldFlags(access, name, desc, signature, value), name, desc, signature, value)
      }

      override fun visitMethod(access: Int, name: String, desc: String, signature: String?, exceptions: Array<out String>?): MethodVisitor? {
        return super.visitMethod(onPatchMethodFlags(access, name, desc, signature, exceptions), name, desc, signature, exceptions)
      }
    }, ClassReader.SKIP_FRAMES)

    return writer.toByteArray()
  }

  protected open fun onPatchFieldFlags(access: Int, name: String, desc: String, signature: String?, value: Any?): Int {
    return access
  }

  protected open fun onPatchMethodFlags(access: Int, name: String, desc: String, signature: String?, exceptions: Array<out String>?): Int {
    return access
  }
}
