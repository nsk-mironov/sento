package io.sento.compiler.bindings

import io.sento.compiler.ContentGenerator
import io.sento.compiler.GeneratedContent
import io.sento.compiler.GenerationEnvironment
import io.sento.compiler.common.Methods
import io.sento.compiler.common.Types
import io.sento.compiler.common.newMethod
import io.sento.compiler.reflection.ClassSpec
import org.objectweb.asm.Opcodes.ACC_FINAL
import org.objectweb.asm.Opcodes.ACC_PRIVATE
import org.objectweb.asm.Opcodes.ACC_PUBLIC
import org.objectweb.asm.Opcodes.ACC_STATIC
import org.objectweb.asm.Opcodes.ACC_SUPER
import org.objectweb.asm.Opcodes.V1_6

internal class SentoFactoryContentGenerator(private val bindings: Collection<ClassSpec>) : ContentGenerator {
  override fun generate(environment: GenerationEnvironment): Collection<GeneratedContent> {
    return listOf(GeneratedContent(Types.getClassFilePath(Types.FACTORY), environment.newClass {
      visit(V1_6, ACC_PUBLIC + ACC_FINAL + ACC_SUPER, Types.FACTORY.internalName, null, Types.OBJECT.internalName, null)
      visitField(ACC_PRIVATE + ACC_FINAL + ACC_STATIC, "BINDINGS", Types.MAP.descriptor, "Ljava/util/Map<Ljava/lang/Class;Lio/sento/Binding;>;", null)

      newMethod(ACC_PRIVATE, Methods.getConstructor()) {
        loadThis()
        invokeConstructor(Types.OBJECT, Methods.getConstructor())
      }

      newMethod(ACC_PUBLIC + ACC_STATIC, Methods.get("createBinding", Types.BINDING, Types.CLASS), "(Ljava/lang/Class<*>;)Lio/sento/Binding<Ljava/lang/Object;>;") {
        getStatic(Types.FACTORY, "BINDINGS", Types.MAP)
        loadArg(0)

        invokeInterface(Types.MAP, Methods.get("get", Types.OBJECT, Types.OBJECT))
        checkCast(Types.BINDING)
      }

      newMethod(ACC_STATIC, Methods.getStaticConstructor())  {
        newInstance(Types.IDENTITY_MAP)
        dup()

        invokeConstructor(Types.IDENTITY_MAP, Methods.getConstructor())
        putStatic(Types.FACTORY, "BINDINGS", Types.MAP)

        bindings.forEach {
          getStatic(Types.FACTORY, "BINDINGS", Types.MAP)
          push(it.type)

          newInstance(environment.naming.getSentoBindingType(it))
          dup()

          invokeConstructor(environment.naming.getSentoBindingType(it), Methods.getConstructor())
          invokeInterface(Types.MAP, Methods.get("put", Types.OBJECT, Types.OBJECT, Types.OBJECT))
          pop()
        }
      }
    }))
  }
}
