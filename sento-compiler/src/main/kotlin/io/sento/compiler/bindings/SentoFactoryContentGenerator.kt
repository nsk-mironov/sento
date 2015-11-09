package io.sento.compiler.bindings

import io.sento.compiler.ContentGenerator
import io.sento.compiler.GeneratedContent
import io.sento.compiler.GenerationEnvironment
import io.sento.compiler.common.Types
import io.sento.compiler.common.toClassFilePath
import io.sento.compiler.common.toSourceFilePath
import io.sento.compiler.model.SentoBindingSpec

import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Label
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Opcodes.*

internal class SentoFactoryContentGenerator(private val bindings: Collection<SentoBindingSpec>) : ContentGenerator {
  override fun onGenerateContent(environment: GenerationEnvironment): List<GeneratedContent> {
    return listOf(onCreateSentoFactory(environment))
  }

  private fun onCreateSentoFactory(environment: GenerationEnvironment): GeneratedContent {
    return GeneratedContent(Types.TYPE_FACTORY.toClassFilePath(), ClassWriter(0).run {
      visitHeader(environment)
      visitFields(environment)

      visitConstructor(environment)
      visitStaticConstructor(environment)

      visitCreateBindingMethod(environment)
      visitEnd()

      toByteArray()
    })
  }

  private fun ClassWriter.visitHeader(environment: GenerationEnvironment) {
    visit(Opcodes.V1_6, ACC_PUBLIC + ACC_FINAL + ACC_SUPER, Types.TYPE_FACTORY.internalName, null, Types.TYPE_OBJECT.internalName, null)
    visitSource(Types.TYPE_FACTORY.toSourceFilePath(), null)
  }

  private fun ClassWriter.visitFields(environment: GenerationEnvironment) {
    val descriptor = Types.TYPE_MAP.descriptor
    val signature = "L${Types.TYPE_MAP.internalName}<L${Types.TYPE_CLASS.internalName};L${Types.TYPE_BINDING.internalName};>;"

    visitField(ACC_PRIVATE + ACC_FINAL + ACC_STATIC, "BINDINGS", descriptor, signature, null).visitEnd()
  }

  private fun ClassWriter.visitConstructor(environment: GenerationEnvironment) {
    val visitor = visitMethod(ACC_PRIVATE, "<init>", "()V", null, null)
    
    val start = Label()
    val end = Label()
    
    visitor.visitCode()
    visitor.visitLabel(start)
    visitor.visitVarInsn(ALOAD, 0)
    visitor.visitMethodInsn(INVOKESPECIAL, Types.TYPE_OBJECT.internalName, "<init>", "()V", false)
    visitor.visitInsn(RETURN)
    visitor.visitLabel(end)
    visitor.visitLocalVariable("this", Types.TYPE_FACTORY.descriptor, null, start, end, 0)
    visitor.visitMaxs(1, 1)
    visitor.visitEnd()
  }

  private fun ClassWriter.visitCreateBindingMethod(environment: GenerationEnvironment) {
    val descriptor = "(L${Types.TYPE_CLASS.internalName};)L${Types.TYPE_BINDING.internalName};"
    val signature = "(L${Types.TYPE_CLASS.internalName}<*>;)L${Types.TYPE_BINDING.internalName}<L${Types.TYPE_OBJECT.internalName};>;"

    val visitor = visitMethod(ACC_PUBLIC + ACC_STATIC, "createBinding", descriptor, signature, null)

    val start = Label()
    val end = Label()

    visitor.visitCode()
    visitor.visitLabel(start)
    visitor.visitFieldInsn(GETSTATIC, Types.TYPE_FACTORY.internalName, "BINDINGS", Types.TYPE_MAP.descriptor)
    visitor.visitVarInsn(ALOAD, 0)
    visitor.visitMethodInsn(INVOKEINTERFACE, Types.TYPE_MAP.internalName, "get", "(L${Types.TYPE_OBJECT.internalName};)L${Types.TYPE_OBJECT.internalName};", true)
    visitor.visitTypeInsn(CHECKCAST, Types.TYPE_BINDING.internalName)
    visitor.visitInsn(ARETURN)
    visitor.visitLabel(end)
    visitor.visitLocalVariable("clazz", Types.TYPE_CLASS.descriptor, "L${Types.TYPE_CLASS.internalName}<*>;", start, end, 0)
    visitor.visitMaxs(2, 1)
    visitor.visitEnd()
  }

  private fun ClassWriter.visitStaticConstructor(environment: GenerationEnvironment) {
    val visitor = visitMethod(ACC_STATIC, "<clinit>", "()V", null, null)

    val start = Label()
    val end = Label()

    visitor.visitCode()
    visitor.visitLabel(start)
    visitor.visitTypeInsn(NEW, "java/util/IdentityHashMap")
    visitor.visitInsn(DUP)
    visitor.visitMethodInsn(INVOKESPECIAL, "java/util/IdentityHashMap", "<init>", "()V", false)
    visitor.visitFieldInsn(PUTSTATIC, Types.TYPE_FACTORY.internalName, "BINDINGS", Types.TYPE_MAP.descriptor)

    bindings.forEach {
      visitor.visitFieldInsn(GETSTATIC, Types.TYPE_FACTORY.internalName, "BINDINGS", Types.TYPE_MAP.descriptor)
      visitor.visitLdcInsn(it.originalType)
      visitor.visitTypeInsn(NEW, it.generatedType.internalName)
      visitor.visitInsn(DUP)
      visitor.visitMethodInsn(INVOKESPECIAL, it.generatedType.internalName, "<init>", "()V", false)
      visitor.visitMethodInsn(INVOKEINTERFACE, Types.TYPE_MAP.internalName, "put", "(L${Types.TYPE_OBJECT.internalName};L${Types.TYPE_OBJECT.internalName};)L${Types.TYPE_OBJECT.internalName};", true)
      visitor.visitInsn(POP)
    }

    visitor.visitLabel(end)
    visitor.visitInsn(RETURN)
    visitor.visitMaxs(4, 0)
    visitor.visitEnd()
  }
}
