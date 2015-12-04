package io.sento.compiler.bindings

import io.sento.compiler.ContentGenerator
import io.sento.compiler.GeneratedContent
import io.sento.compiler.GenerationEnvironment
import io.sento.compiler.common.Methods
import io.sento.compiler.common.Types
import io.sento.compiler.common.body
import io.sento.compiler.model.SentoBindingSpec
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes.ACC_FINAL
import org.objectweb.asm.Opcodes.ACC_PRIVATE
import org.objectweb.asm.Opcodes.ACC_PUBLIC
import org.objectweb.asm.Opcodes.ACC_STATIC
import org.objectweb.asm.Opcodes.ACC_SUPER
import org.objectweb.asm.Opcodes.V1_6
import org.objectweb.asm.commons.GeneratorAdapter

internal class SentoFactoryContentGenerator(private val bindings: Collection<SentoBindingSpec>) : ContentGenerator {
  override fun generate(environment: GenerationEnvironment): Collection<GeneratedContent> {
    return listOf(GeneratedContent(Types.getClassFilePath(Types.FACTORY), environment.createClass {
      visitHeader(environment)
      visitFields(environment)

      visitConstructor(environment)
      visitStaticConstructor(environment)

      visitCreateBindingMethod(environment)
    }))
  }

  private fun ClassWriter.visitHeader(environment: GenerationEnvironment) {
    visit(V1_6, ACC_PUBLIC + ACC_FINAL + ACC_SUPER, Types.FACTORY.internalName, null, Types.OBJECT.internalName, null)
  }

  private fun ClassWriter.visitFields(environment: GenerationEnvironment) {
    visitField(ACC_PRIVATE + ACC_FINAL + ACC_STATIC, "BINDINGS", Types.MAP.descriptor, "Ljava/util/Map<Ljava/lang/Class;Lio/sento/Binding;>;", null)
  }

  private fun ClassWriter.visitConstructor(environment: GenerationEnvironment) {
    GeneratorAdapter(ACC_PRIVATE, Methods.getConstructor(), null, null, this).body {
      loadThis()
      invokeConstructor(Types.OBJECT, Methods.getConstructor())
    }
  }

  private fun ClassWriter.visitCreateBindingMethod(environment: GenerationEnvironment) {
    val method = Methods.get("createBinding", Types.BINDING, Types.CLASS)
    val signature = "(Ljava/lang/Class<*>;)Lio/sento/Binding<Ljava/lang/Object;>;"

    GeneratorAdapter(ACC_PUBLIC + ACC_STATIC, method, signature, null, this).body {
      getStatic(Types.FACTORY, "BINDINGS", Types.MAP)
      loadArg(0)

      invokeInterface(Types.MAP, Methods.get("get", Types.OBJECT, Types.OBJECT))
      checkCast(Types.BINDING)
    }
  }

  private fun ClassWriter.visitStaticConstructor(environment: GenerationEnvironment) {
    GeneratorAdapter(ACC_STATIC, Methods.getStaticConstructor(), null, null, this).body {
      newInstance(Types.IDENTITY_MAP)
      dup()

      invokeConstructor(Types.IDENTITY_MAP, Methods.getConstructor())
      putStatic(Types.FACTORY, "BINDINGS", Types.MAP)

      bindings.forEach {
        getStatic(Types.FACTORY, "BINDINGS", Types.MAP)
        push(it.target)

        newInstance(it.binding)
        dup()

        invokeConstructor(it.binding, Methods.getConstructor())
        invokeInterface(Types.MAP, Methods.get("put", Types.OBJECT, Types.OBJECT, Types.OBJECT))
        pop()
      }
    }
  }
}
