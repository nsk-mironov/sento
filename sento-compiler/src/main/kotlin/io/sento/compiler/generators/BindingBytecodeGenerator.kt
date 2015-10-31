package io.sento.compiler.generators

import io.sento.Bind
import io.sento.compiler.ClassRegistry
import io.sento.compiler.GenerationEnvironment
import io.sento.compiler.common.Types
import io.sento.compiler.specs.ClassSpec
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Label
import org.objectweb.asm.Type

import org.objectweb.asm.Opcodes.*
import java.util.HashMap

internal class BindingBytecodeGenerator : BytecodeGenerator {
  private val generators = HashMap<Type, FieldBindingGenerator<out Annotation>>().apply {
    put(Type.getType(Bind::class.java), BindViewBindingGenerator())
  }

  override fun shouldGenerateBytecode(clazz: ClassSpec, environment: GenerationEnvironment): Boolean {
    return clazz.fields.any {
      it.annotations.any {
        generators.containsKey(it.type)
      }
    } || clazz.methods.any {
      it.annotations.any {
        generators.containsKey(it.type)
      }
    }
  }

  override fun onGenerateBytecode(clazz: ClassSpec, environment: GenerationEnvironment): ByteArray {
    return with (ClassWriter(0)) {
      visitHeader(clazz, environment)
      visitConstructor(clazz, environment)

      visitBindMethod(clazz, environment)
      visitUnbindMethod(clazz, environment)
      visitBindBridge(clazz, environment)
      visitUnbindBridge(clazz, environment)
      visitEnd()

      toByteArray()
    }
  }

  private fun ClassWriter.visitHeader(clazz: ClassSpec, environment: GenerationEnvironment) = apply {
    val name = clazz.generatedType.internalName
    val signature = "L${Types.TYPE_OBJECT.internalName};L${Types.TYPE_BINDING.internalName}<L${clazz.originalType.internalName};>;"
    val superName = Types.TYPE_OBJECT.internalName
    val interfaces = arrayOf(Types.TYPE_BINDING.internalName)
    val source = clazz.generatedType.toSource()

    visit(50, ACC_PUBLIC + ACC_SUPER, name, signature, superName, interfaces)
    visitSource(source, null)
  }

  private fun ClassWriter.visitConstructor(clazz: ClassSpec, environment: GenerationEnvironment) {
    val visitor = visitMethod(ACC_PUBLIC, "<init>", "()V", null, null)

    val start = Label()
    val end = Label()

    visitor.visitCode()
    visitor.visitLabel(start)
    visitor.visitVarInsn(ALOAD, 0)
    visitor.visitMethodInsn(INVOKESPECIAL, Types.TYPE_OBJECT.internalName, "<init>", "()V", false)
    visitor.visitInsn(RETURN)
    visitor.visitLabel(end)
    visitor.visitLocalVariable("this", clazz.generatedType.descriptor, null, start, end, 0)
    visitor.visitMaxs(1, 1)
    visitor.visitEnd()
  }

  private fun ClassWriter.visitBindMethod(clazz: ClassSpec, environment: GenerationEnvironment) {
    val visitor = visitMethod(ACC_PUBLIC, "bind", "(L${clazz.originalType.internalName};L${Types.TYPE_OBJECT.internalName};L${Types.TYPE_FINDER.internalName};)V", "<S:L${Types.TYPE_OBJECT.internalName};>(L${clazz.originalType.internalName};TS;L${Types.TYPE_FINDER.internalName}<-TS;>;)V", null)

    val start = Label()
    val end = Label()

    visitor.visitCode()
    visitor.visitLabel(start)

    clazz.fields.forEach { field ->
      field.annotations.forEach { annotation ->
        val generator = generators.get(annotation.type)
        val value = annotation.resolve<Annotation>()

        if (generator != null) {
          val variables = mapOf("this" to 0, "target" to 1, "source" to 2, "finder" to 3)
          val context = FieldBindingContext(field, clazz, value, visitor, variables)

          generator.bind(context, environment)
        }
      }
    }

    visitor.visitInsn(RETURN)
    visitor.visitLabel(end)
    visitor.visitLocalVariable("this", clazz.generatedType.descriptor, null, start, end, 0)
    visitor.visitLocalVariable("target", clazz.originalType.descriptor, null, start, end, 1)
    visitor.visitLocalVariable("source", Types.TYPE_OBJECT.descriptor, "TS;", start, end, 2)
    visitor.visitLocalVariable("finder", Types.TYPE_FINDER.descriptor, "L${Types.TYPE_FINDER.internalName}<-TS;>;", start, end, 3)
    visitor.visitMaxs(4, 4)
    visitor.visitEnd()
  }

  private fun ClassWriter.visitUnbindMethod(clazz: ClassSpec, environment: GenerationEnvironment) {
    val visitor = visitMethod(ACC_PUBLIC, "unbind", "(L${clazz.originalType.internalName};)V", null, null)

    val start = Label()
    val end = Label()

    visitor.visitCode()
    visitor.visitLabel(start)

    clazz.fields.forEach { field ->
      field.annotations.forEach { annotation ->
        val generator = generators.get(annotation.type)
        val value = annotation.resolve<Annotation>()

        if (generator != null) {
          val variables = mapOf("this" to 0, "target" to 1)
          val context = FieldBindingContext(field, clazz, value, visitor, variables)

          generator.unbind(context, environment)
        }
      }
    }

    visitor.visitInsn(RETURN)
    visitor.visitLabel(end)
    visitor.visitLocalVariable("this", clazz.generatedType.descriptor, null, start, end, 0)
    visitor.visitLocalVariable("target", clazz.originalType.descriptor, null, start, end, 1)
    visitor.visitMaxs(2, 2)
    visitor.visitEnd()
  }

  private fun ClassWriter.visitBindBridge(clazz: ClassSpec, environment: GenerationEnvironment) {
    val visitor = visitMethod(ACC_PUBLIC + ACC_BRIDGE + ACC_SYNTHETIC, "bind", "(L${Types.TYPE_OBJECT.internalName};L${Types.TYPE_OBJECT.internalName};L${Types.TYPE_FINDER.internalName};)V", null, null)

    val start = Label()
    val end = Label()

    visitor.visitCode()
    visitor.visitLabel(start)
    visitor.visitVarInsn(ALOAD, 0)
    visitor.visitVarInsn(ALOAD, 1)
    visitor.visitTypeInsn(CHECKCAST, clazz.originalType.internalName)
    visitor.visitVarInsn(ALOAD, 2)
    visitor.visitVarInsn(ALOAD, 3)
    visitor.visitMethodInsn(INVOKEVIRTUAL, clazz.generatedType.internalName, "bind", "(L${clazz.originalType.internalName};L${Types.TYPE_OBJECT.internalName};L${Types.TYPE_FINDER.internalName};)V", false)
    visitor.visitInsn(RETURN)
    visitor.visitLabel(end)
    visitor.visitLocalVariable("this", clazz.generatedType.descriptor, null, start, end, 0)
    visitor.visitMaxs(4, 4)
    visitor.visitEnd()
  }

  private fun ClassWriter.visitUnbindBridge(clazz: ClassSpec, environment: GenerationEnvironment) {
    val visitor = visitMethod(ACC_PUBLIC + ACC_BRIDGE + ACC_SYNTHETIC, "unbind", "(L${Types.TYPE_OBJECT.internalName};)V", null, null)

    val start = Label()
    val end = Label()

    visitor.visitCode()
    visitor.visitLabel(start)
    visitor.visitVarInsn(ALOAD, 0)
    visitor.visitVarInsn(ALOAD, 1)
    visitor.visitTypeInsn(CHECKCAST, clazz.originalType.internalName)
    visitor.visitMethodInsn(INVOKEVIRTUAL, clazz.generatedType.internalName, "unbind", "(L${clazz.originalType.internalName};)V", false)
    visitor.visitInsn(RETURN)
    visitor.visitLabel(end)
    visitor.visitLocalVariable("this", clazz.generatedType.descriptor, null, start, end, 0)
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

  private val ClassSpec.generatedType: Type
    get() = Type.getObjectType("${type.internalName}\$\$SentoBinding")

  private val ClassSpec.originalType: Type
    get() = type
}
