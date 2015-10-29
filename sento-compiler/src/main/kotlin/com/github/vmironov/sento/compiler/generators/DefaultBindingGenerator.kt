package com.github.vmironov.sento.compiler.generators

import android.view.View
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
    val bindingType = Type.getObjectType("${clazz.type.internalName}\$\$SentoBinding")
    val targetType = clazz.type

    val writer = ClassWriter(0)

    writer.visitHeader(clazz, bindingType, targetType)
    writer.visitConstructor(clazz, bindingType, targetType)
    writer.visitBindMethod(clazz, bindingType, targetType)
    writer.visitUnbindMethod(clazz, bindingType, targetType)
    writer.visitBindBridge(clazz, bindingType, targetType)
    writer.visitUnbindBridge(clazz, bindingType, targetType)
    writer.visitEnd()

    return writer.toByteArray()
  }

  private fun ClassWriter.visitHeader(clazz: ClassSpec, bindingType: Type, targetType: Type) = apply {
    val name = bindingType.internalName
    val signature = "L${TYPE_OBJECT.internalName};L${TYPE_BINDING.internalName}<L${targetType.internalName};>;"
    val superName = TYPE_OBJECT.internalName
    val interfaces = arrayOf(TYPE_BINDING.internalName)
    val source = bindingType.toSource()

    visit(50, ACC_PUBLIC + ACC_SUPER, name, signature, superName, interfaces)
    visitSource(source, null)
  }

  private fun ClassWriter.visitConstructor(clazz: ClassSpec, bindingType: Type, targetType: Type) {
    val visitor = visitMethod(ACC_PUBLIC, "<init>", "()V", null, null)

    val start = Label()
    val end = Label()

    visitor.visitCode()
    visitor.visitLabel(start)
    visitor.visitVarInsn(ALOAD, 0)
    visitor.visitMethodInsn(INVOKESPECIAL, TYPE_OBJECT.internalName, "<init>", "()V", false)
    visitor.visitInsn(RETURN)
    visitor.visitLabel(end)
    visitor.visitLocalVariable("this", bindingType.descriptor, null, start, end, 0)
    visitor.visitMaxs(1, 1)
    visitor.visitEnd()
  }

  private fun ClassWriter.visitBindMethod(clazz: ClassSpec, bindingType: Type, targetType: Type) {
    val visitor = visitMethod(ACC_PUBLIC, "bind", "(L${targetType.internalName};L${TYPE_OBJECT.internalName};L${TYPE_FINDER.internalName};)V", "<S:L${TYPE_OBJECT.internalName};>(L${targetType.internalName};TS;L${TYPE_FINDER.internalName}<-TS;>;)V", null)

    val start = Label()
    val end = Label()

    visitor.visitCode()
    visitor.visitLabel(start)

    clazz.fields.forEach {
      visitor.visitVarInsn(ALOAD, 1)
      visitor.visitVarInsn(ALOAD, 3)
      visitor.visitLdcInsn(it.annotations[0].values["value"])
      visitor.visitVarInsn(ALOAD, 2)
      visitor.visitMethodInsn(INVOKEINTERFACE, TYPE_FINDER.internalName, "find", "(IL${TYPE_OBJECT.internalName};)Landroid/view/View;", true)
      visitor.visitTypeInsn(CHECKCAST, it.type.internalName)
      visitor.visitFieldInsn(PUTFIELD, targetType.internalName, it.name, it.type.descriptor)
      visitor.visitLabel(Label())
    }

    visitor.visitInsn(RETURN)
    visitor.visitLabel(end)
    visitor.visitLocalVariable("this", "L${bindingType.internalName};", null, start, end, 0)
    visitor.visitLocalVariable("target", "L${targetType.internalName};", null, start, end, 1)
    visitor.visitLocalVariable("source", "L${TYPE_OBJECT.internalName};", "TS;", start, end, 2)
    visitor.visitLocalVariable("finder", "L${TYPE_FINDER.internalName};", "L${TYPE_FINDER.internalName}<-TS;>;", start, end, 3)
    visitor.visitMaxs(4, 4)
    visitor.visitEnd()
  }
  
  private fun ClassWriter.visitUnbindMethod(clazz: ClassSpec, bindingType: Type, targetType: Type) {
    val visitor = visitMethod(ACC_PUBLIC, "unbind", "(L${targetType.internalName};)V", null, null)

    val start = Label()
    val end = Label()

    visitor.visitCode()
    visitor.visitLabel(start)
    
    clazz.fields.forEach {
      visitor.visitVarInsn(ALOAD, 1)
      visitor.visitInsn(ACONST_NULL)
      visitor.visitFieldInsn(PUTFIELD, targetType.internalName, it.name, it.type.descriptor)
      visitor.visitLabel(Label())
    }
    
    visitor.visitInsn(RETURN)
    visitor.visitLabel(end)
    visitor.visitLocalVariable("this", "L${bindingType.internalName};", null, start, end, 0)
    visitor.visitLocalVariable("target", "L${targetType.internalName};", null, start, end, 1)
    visitor.visitMaxs(2, 2)
    visitor.visitEnd()
  }

  private fun ClassWriter.visitBindBridge(clazz: ClassSpec, bindingType: Type, targetType: Type) {
    val visitor = visitMethod(ACC_PUBLIC + ACC_BRIDGE + ACC_SYNTHETIC, "bind", "(L${TYPE_OBJECT.internalName};L${TYPE_OBJECT.internalName};L${TYPE_FINDER.internalName};)V", null, null)

    val start = Label()
    val end = Label()

    visitor.visitCode()
    visitor.visitLabel(start)
    visitor.visitVarInsn(ALOAD, 0)
    visitor.visitVarInsn(ALOAD, 1)
    visitor.visitTypeInsn(CHECKCAST, targetType.internalName)
    visitor.visitVarInsn(ALOAD, 2)
    visitor.visitVarInsn(ALOAD, 3)
    visitor.visitMethodInsn(INVOKEVIRTUAL, bindingType.internalName, "bind", "(L${targetType.internalName};L${TYPE_OBJECT.internalName};L${TYPE_FINDER.internalName};)V", false)
    visitor.visitInsn(RETURN)
    visitor.visitLabel(end)
    visitor.visitLocalVariable("this", bindingType.descriptor, null, start, end, 0)
    visitor.visitMaxs(4, 4)
    visitor.visitEnd()
  }

  private fun ClassWriter.visitUnbindBridge(clazz: ClassSpec, bindingType: Type, targetType: Type) {
    val visitor = visitMethod(ACC_PUBLIC + ACC_BRIDGE + ACC_SYNTHETIC, "unbind", "(L${TYPE_OBJECT.internalName};)V", null, null)

    val start = Label()
    val end = Label()

    visitor.visitCode()
    visitor.visitLabel(start)
    visitor.visitVarInsn(ALOAD, 0)
    visitor.visitVarInsn(ALOAD, 1)
    visitor.visitTypeInsn(CHECKCAST, targetType.internalName)
    visitor.visitMethodInsn(INVOKEVIRTUAL, bindingType.internalName, "unbind", "(L${targetType.internalName};)V", false)
    visitor.visitInsn(RETURN)
    visitor.visitLabel(end)
    visitor.visitLocalVariable("this", bindingType.descriptor, null, start, end, 0)
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
}
