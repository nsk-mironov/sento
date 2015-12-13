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

internal class SentoFactoryContentGenerator(private val bindings: Collection<BindingSpec>) : ContentGenerator {
  override fun generate(environment: GenerationEnvironment): Collection<GeneratedContent> {
    return listOf(GeneratedContent.from(Types.FACTORY, mapOf(), environment.newClass {
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

      newMethod(ACC_STATIC, Methods.getStaticConstructor())  {
        newInstance(Types.IDENTITY_MAP, Methods.getConstructor())
        putStatic(Types.FACTORY, "BINDINGS", Types.MAP)

        bindings.forEach {
          getStatic(Types.FACTORY, "BINDINGS", Types.MAP)
          push(it.clazz.type)

          newInstance(environment.naming.getBindingType(it.clazz), Methods.getConstructor())
          invokeInterface(Types.MAP, Methods.get("put", Types.OBJECT, Types.OBJECT, Types.OBJECT))
          pop()
        }
      }
    }))
  }
}
