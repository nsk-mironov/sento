package io.sento.compiler.bindings

import io.sento.compiler.ContentGenerator
import io.sento.compiler.GeneratedContent
import io.sento.compiler.GenerationEnvironment
import io.sento.compiler.common.Types
import io.sento.compiler.common.toClassFilePath
import io.sento.compiler.common.toSourceFilePath
import io.sento.compiler.model.SentoBindingSpec

import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.Type
import org.objectweb.asm.commons.GeneratorAdapter
import org.objectweb.asm.commons.Method

internal class SentoFactoryContentGenerator(private val bindings: Collection<SentoBindingSpec>) : ContentGenerator {
  override fun generate(environment: GenerationEnvironment): Collection<GeneratedContent> {
    return listOf(onCreateSentoFactory(environment))
  }

  private fun onCreateSentoFactory(environment: GenerationEnvironment): GeneratedContent {
    return GeneratedContent(Types.TYPE_FACTORY.toClassFilePath(), environment.createClass {
      visitHeader(environment)
      visitFields(environment)

      visitConstructor(environment)
      visitStaticConstructor(environment)

      visitCreateBindingMethod(environment)
    })
  }

  private fun ClassWriter.visitHeader(environment: GenerationEnvironment) {
    visit(Opcodes.V1_6, ACC_PUBLIC + ACC_FINAL + ACC_SUPER, Types.TYPE_FACTORY.internalName, null, Types.TYPE_OBJECT.internalName, null)
  }

  private fun ClassWriter.visitFields(environment: GenerationEnvironment) {
    val descriptor = Types.TYPE_MAP.descriptor
    val signature = "L${Types.TYPE_MAP.internalName}<L${Types.TYPE_CLASS.internalName};L${Types.TYPE_BINDING.internalName};>;"

    visitField(ACC_PRIVATE + ACC_FINAL + ACC_STATIC, "BINDINGS", descriptor, signature, null).visitEnd()
  }

  private fun ClassWriter.visitConstructor(environment: GenerationEnvironment) {
    GeneratorAdapter(ACC_PRIVATE, Method.getMethod("void <init> ()"), null, null, this).apply {
      loadThis()
      invokeConstructor(Type.getType(Any::class.java), Method.getMethod("void <init> ()"))

      returnValue()
      endMethod()
    }
  }

  private fun ClassWriter.visitCreateBindingMethod(environment: GenerationEnvironment) {
    val method = "io.sento.Binding createBinding(java.lang.Class)"
    val signature = "(Ljava/lang/Class<*>;)Lio/sento/Binding<Ljava/lang/Object;>;"

    GeneratorAdapter(ACC_PUBLIC + ACC_STATIC, Method.getMethod(method), signature, null, this).apply {
      getStatic(Types.TYPE_FACTORY, "BINDINGS", Types.TYPE_MAP)
      loadArg(0)

      invokeInterface(Types.TYPE_MAP, Method.getMethod("Object get(Object)"))
      checkCast(Types.TYPE_BINDING)

      returnValue()
      endMethod()
    }
  }

  private fun ClassWriter.visitStaticConstructor(environment: GenerationEnvironment) {
    GeneratorAdapter(ACC_STATIC, Method.getMethod("void <clinit> ()"), null, null, this).apply {
      newInstance(Types.TYPE_IDENTITY_MAP)
      dup()

      invokeConstructor(Types.TYPE_IDENTITY_MAP, Method.getMethod("void <init> ()"))
      putStatic(Types.TYPE_FACTORY, "BINDINGS", Types.TYPE_MAP)

      bindings.forEach {
        getStatic(Types.TYPE_FACTORY, "BINDINGS", Types.TYPE_MAP)
        push(it.originalType)

        newInstance(it.generatedType)
        dup()

        invokeConstructor(it.generatedType, Method.getMethod("void <init> ()"))
        invokeInterface(Types.TYPE_MAP, Method.getMethod("Object put(Object, Object)"))
        pop()
      }

      returnValue()
      endMethod()
    }
  }
}
