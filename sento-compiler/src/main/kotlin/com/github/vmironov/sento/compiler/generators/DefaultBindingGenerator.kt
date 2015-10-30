package com.github.vmironov.sento.compiler.generators

import com.github.vmironov.sento.Bind
import com.github.vmironov.sento.Binding
import com.github.vmironov.sento.Finder
import com.github.vmironov.sento.OnClick
import com.github.vmironov.sento.compiler.SentoRegistry
import com.github.vmironov.sento.compiler.specs.AnnotationSpec
import com.github.vmironov.sento.compiler.specs.ClassSpec
import com.github.vmironov.sento.compiler.specs.FieldSpec
import com.github.vmironov.sento.compiler.specs.MethodSpec
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Label
import org.objectweb.asm.Type

import org.objectweb.asm.Opcodes.*

public class DefaultBindingGenerator : BindingGenerator {
  private val annotations = listOf<Type>(
      Type.getType(Bind::class.java),
      Type.getType(OnClick::class.java)
  )

  override fun shouldAcceptClassAnnotation(annotation: AnnotationSpec): Boolean {
    return false
  }

  override fun shouldAcceptClass(clazz: ClassSpec): Boolean {
    return true
  }

  override fun shouldAcceptClassField(field: FieldSpec): Boolean {
    return field.annotations.any {
      annotations.contains(it.type)
    }
  }

  override fun shouldAcceptClassMethod(method: MethodSpec): Boolean {
    return method.annotations.any {
      annotations.contains(it.type)
    }
  }

  override fun shouldGenerateBinding(clazz: ClassSpec, registry: SentoRegistry): Boolean {
    return !clazz.fields.isEmpty() || !clazz.methods.isEmpty()
  }

  override fun onGenerate(clazz: ClassSpec, registry: SentoRegistry): ByteArray {
    return with (ClassWriter(0)) {
      visitHeader(clazz)
      visitConstructor(clazz)

      visitBindMethod(clazz)
      visitUnbindMethod(clazz)
      visitBindBridge(clazz)
      visitUnbindBridge(clazz)
      visitEnd()

      toByteArray()
    }
  }

  private fun ClassWriter.visitHeader(clazz: ClassSpec) = apply {
    val name = clazz.bindingType.internalName
    val signature = "L${TYPE_OBJECT.internalName};L${TYPE_BINDING.internalName}<L${clazz.targetType.internalName};>;"
    val superName = TYPE_OBJECT.internalName
    val interfaces = arrayOf(TYPE_BINDING.internalName)
    val source = clazz.bindingType.toSource()

    visit(50, ACC_PUBLIC + ACC_SUPER, name, signature, superName, interfaces)
    visitSource(source, null)
  }

  private fun ClassWriter.visitConstructor(clazz: ClassSpec) {
    val visitor = visitMethod(ACC_PUBLIC, "<init>", "()V", null, null)

    val start = Label()
    val end = Label()

    visitor.visitCode()
    visitor.visitLabel(start)
    visitor.visitVarInsn(ALOAD, 0)
    visitor.visitMethodInsn(INVOKESPECIAL, TYPE_OBJECT.internalName, "<init>", "()V", false)
    visitor.visitInsn(RETURN)
    visitor.visitLabel(end)
    visitor.visitLocalVariable("this", clazz.bindingType.descriptor, null, start, end, 0)
    visitor.visitMaxs(1, 1)
    visitor.visitEnd()
  }

  private fun ClassWriter.visitBindMethod(clazz: ClassSpec) {
    val visitor = visitMethod(ACC_PUBLIC, "bind", "(L${clazz.targetType.internalName};L${TYPE_OBJECT.internalName};L${TYPE_FINDER.internalName};)V", "<S:L${TYPE_OBJECT.internalName};>(L${clazz.targetType.internalName};TS;L${TYPE_FINDER.internalName}<-TS;>;)V", null)

    val start = Label()
    val end = Label()

    visitor.visitCode()
    visitor.visitLabel(start)

    clazz.fields.forEach {
      val annotation = it.getAnnotation(Bind::class.java)

      if (annotation != null) {
        visitor.visitVarInsn(ALOAD, 1)
        visitor.visitVarInsn(ALOAD, 3)
        visitor.visitLdcInsn(annotation.value)
        visitor.visitVarInsn(ALOAD, 2)
        visitor.visitMethodInsn(INVOKEINTERFACE, TYPE_FINDER.internalName, "find", "(IL${TYPE_OBJECT.internalName};)Landroid/view/View;", true)
        visitor.visitTypeInsn(CHECKCAST, it.type.internalName)
        visitor.visitFieldInsn(PUTFIELD, clazz.targetType.internalName, it.name, it.type.descriptor)
      }
    }

    visitor.visitInsn(RETURN)
    visitor.visitLabel(end)
    visitor.visitLocalVariable("this", clazz.bindingType.descriptor, null, start, end, 0)
    visitor.visitLocalVariable("target", clazz.targetType.descriptor, null, start, end, 1)
    visitor.visitLocalVariable("source", TYPE_OBJECT.descriptor, "TS;", start, end, 2)
    visitor.visitLocalVariable("finder", TYPE_FINDER.descriptor, "L${TYPE_FINDER.internalName}<-TS;>;", start, end, 3)
    visitor.visitMaxs(4, 4)
    visitor.visitEnd()
  }
  
  private fun ClassWriter.visitUnbindMethod(clazz: ClassSpec) {
    val visitor = visitMethod(ACC_PUBLIC, "unbind", "(L${clazz.targetType.internalName};)V", null, null)

    val start = Label()
    val end = Label()

    visitor.visitCode()
    visitor.visitLabel(start)
    
    clazz.fields.forEach {
      if (it.getAnnotation(Bind::class.java) != null) {
        visitor.visitVarInsn(ALOAD, 1)
        visitor.visitInsn(ACONST_NULL)
        visitor.visitFieldInsn(PUTFIELD, clazz.targetType.internalName, it.name, it.type.descriptor)
      }
    }
    
    visitor.visitInsn(RETURN)
    visitor.visitLabel(end)
    visitor.visitLocalVariable("this", clazz.bindingType.descriptor, null, start, end, 0)
    visitor.visitLocalVariable("target", clazz.targetType.descriptor, null, start, end, 1)
    visitor.visitMaxs(2, 2)
    visitor.visitEnd()
  }

  private fun ClassWriter.visitBindBridge(clazz: ClassSpec) {
    val visitor = visitMethod(ACC_PUBLIC + ACC_BRIDGE + ACC_SYNTHETIC, "bind", "(L${TYPE_OBJECT.internalName};L${TYPE_OBJECT.internalName};L${TYPE_FINDER.internalName};)V", null, null)

    val start = Label()
    val end = Label()

    visitor.visitCode()
    visitor.visitLabel(start)
    visitor.visitVarInsn(ALOAD, 0)
    visitor.visitVarInsn(ALOAD, 1)
    visitor.visitTypeInsn(CHECKCAST, clazz.targetType.internalName)
    visitor.visitVarInsn(ALOAD, 2)
    visitor.visitVarInsn(ALOAD, 3)
    visitor.visitMethodInsn(INVOKEVIRTUAL, clazz.bindingType.internalName, "bind", "(L${clazz.targetType.internalName};L${TYPE_OBJECT.internalName};L${TYPE_FINDER.internalName};)V", false)
    visitor.visitInsn(RETURN)
    visitor.visitLabel(end)
    visitor.visitLocalVariable("this", clazz.bindingType.descriptor, null, start, end, 0)
    visitor.visitMaxs(4, 4)
    visitor.visitEnd()
  }

  private fun ClassWriter.visitUnbindBridge(clazz: ClassSpec) {
    val visitor = visitMethod(ACC_PUBLIC + ACC_BRIDGE + ACC_SYNTHETIC, "unbind", "(L${TYPE_OBJECT.internalName};)V", null, null)

    val start = Label()
    val end = Label()

    visitor.visitCode()
    visitor.visitLabel(start)
    visitor.visitVarInsn(ALOAD, 0)
    visitor.visitVarInsn(ALOAD, 1)
    visitor.visitTypeInsn(CHECKCAST, clazz.targetType.internalName)
    visitor.visitMethodInsn(INVOKEVIRTUAL, clazz.bindingType.internalName, "unbind", "(L${clazz.targetType.internalName};)V", false)
    visitor.visitInsn(RETURN)
    visitor.visitLabel(end)
    visitor.visitLocalVariable("this", clazz.bindingType.descriptor, null, start, end, 0)
    visitor.visitMaxs(2, 2)
    visitor.visitEnd()
  }

  private fun Type.toSource(): String {
    val className = className

    return if (className.contains('.')) {
      "${className.substring(className.lastIndexOf('.') + 1)}.java"
    } else {
      "$className.java"
    }
  }

  private companion object {
    private val TYPE_OBJECT = Type.getType(Any::class.java)
    private val TYPE_BINDING = Type.getType(Binding::class.java)
    private val TYPE_FINDER = Type.getType(Finder::class.java)
  }

  private val ClassSpec.bindingType: Type
    get() = Type.getObjectType("${type.internalName}\$\$SentoBinding")

  private val ClassSpec.targetType: Type
    get() = type
}
