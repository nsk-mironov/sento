package io.sento.compiler.generator

import io.sento.compiler.ContentGenerator
import io.sento.compiler.GeneratedContent
import io.sento.compiler.GenerationEnvironment
import io.sento.compiler.common.Methods
import io.sento.compiler.common.Types
import io.sento.compiler.model.BindingSpec
import org.objectweb.asm.Opcodes.ACC_FINAL
import org.objectweb.asm.Opcodes.ACC_PRIVATE
import org.objectweb.asm.Opcodes.ACC_PUBLIC
import org.objectweb.asm.Opcodes.ACC_STATIC
import org.objectweb.asm.Opcodes.ACC_SUPER
import org.objectweb.asm.Type

internal class SentoFactoryContentGenerator(private val bindings: Collection<BindingSpec>) : ContentGenerator {
  private companion object {
    private val BINDING = Type.getObjectType("io/sento/SentoFactory\$\$HandleAwareBinding")
  }

  override fun generate(environment: GenerationEnvironment): Collection<GeneratedContent> {
    return listOf(onCreateFactoryGeneratedContent(environment), onCreateBindingGeneratedContent(environment))
  }

  private fun onCreateFactoryGeneratedContent(environment: GenerationEnvironment): GeneratedContent {
    return GeneratedContent.from(Types.FACTORY, mapOf(), environment.newClass {
      visit(ACC_PUBLIC + ACC_FINAL + ACC_SUPER, Types.FACTORY)
      visitField(ACC_PRIVATE + ACC_FINAL + ACC_STATIC, "BINDINGS", Types.MAP, "Ljava/util/Map<Ljava/lang/Class;Lio/sento/Binding;>;")

      newMethod(ACC_PRIVATE, Methods.getConstructor()) {
        loadThis()
        invokeConstructor(Types.OBJECT, Methods.getConstructor())
      }

      newMethod(ACC_PUBLIC + ACC_STATIC, Methods.get("createBinding", Types.BINDING, Types.CLASS), "(Ljava/lang/Class<*>;)Lio/sento/Binding;") {
        getStatic(Types.FACTORY, "BINDINGS", Types.MAP)
        loadArg(0)

        invokeInterface(Types.MAP, Methods.get("get", Types.OBJECT, Types.OBJECT))
        checkCast(Types.BINDING)
      }

      newMethod(ACC_STATIC, Methods.getStaticConstructor()) {
        newInstance(Types.IDENTITY_MAP, Methods.getConstructor())
        putStatic(Types.FACTORY, "BINDINGS", Types.MAP)

        bindings.forEachIndexed { index, spec ->
          getStatic(Types.FACTORY, "BINDINGS", Types.MAP)
          push(spec.clazz.type)

          newInstance(BINDING, Methods.getConstructor(Types.INT)) {
            push(index)
          }

          invokeInterface(Types.MAP, Methods.get("put", Types.OBJECT, Types.OBJECT, Types.OBJECT))
          pop()
        }
      }
    })
  }

  private fun onCreateBindingGeneratedContent(environment: GenerationEnvironment): GeneratedContent {
    return GeneratedContent.from(BINDING, mapOf(), environment.newClass {
      visit(ACC_PUBLIC + ACC_SUPER, BINDING, null, Types.OBJECT, arrayOf(Types.BINDING))
      visitField(ACC_PRIVATE + ACC_FINAL, "handle", Types.INT)

      newMethod(ACC_PUBLIC, Methods.getConstructor(Types.INT)) {
        loadThis()
        invokeConstructor(Types.OBJECT, Methods.getConstructor())

        loadThis()
        loadArg(0)
        putField(BINDING, "handle", Types.INT)
      }

      newMethod(environment.naming.getBindMethodSpec()) {
        // empty for now
      }

      newMethod(environment.naming.getUnbindMethodSpec()) {
        // empty for now
      }
    })
  }
}
