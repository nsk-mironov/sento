package io.mironov.sento.compiler.generator

import io.mironov.sento.compiler.ContentGenerator
import io.mironov.sento.compiler.GeneratedContent
import io.mironov.sento.compiler.GenerationEnvironment
import io.mironov.sento.compiler.common.Methods
import io.mironov.sento.compiler.common.Types
import io.mironov.sento.compiler.model.BindingSpec
import org.objectweb.asm.Label
import org.objectweb.asm.Opcodes.ACC_FINAL
import org.objectweb.asm.Opcodes.ACC_PRIVATE
import org.objectweb.asm.Opcodes.ACC_PUBLIC
import org.objectweb.asm.Opcodes.ACC_STATIC
import org.objectweb.asm.Opcodes.ACC_SUPER
import org.objectweb.asm.Type
import org.objectweb.asm.commons.GeneratorAdapter
import org.objectweb.asm.commons.TableSwitchGenerator

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
      visitField(ACC_PRIVATE + ACC_FINAL + ACC_STATIC, "BINDINGS", Types.MAP, null)

      newMethod(ACC_PUBLIC + ACC_STATIC, Methods.get("createBinding", Types.BINDING, Types.CLASS), null) {
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
        switch(bindings.toList()) {
          invokeStatic(it.clazz, environment.naming.getSyntheticBindMethodSpec().apply {
            for (index in 0..arguments.size - 1) {
              loadArg(index)
            }
          })
        }
      }

      newMethod(environment.naming.getUnbindMethodSpec()) {
        switch(bindings.toList()) {
          invokeStatic(it.clazz, environment.naming.getSyntheticUnbindMethodSpec().apply {
            for (index in 0..arguments.size - 1) {
              loadArg(index)
            }
          })
        }
      }
    })
  }

  private fun GeneratorAdapter.switch(bindings: List<BindingSpec>, generator: GeneratorAdapter.(BindingSpec) -> Unit) {
    loadThis()
    getField(BINDING, "handle", Types.INT)

    tableSwitch(IntArray(bindings.size) { it }, object : TableSwitchGenerator {
      override fun generateCase(key: Int, end: Label) {
        generator(bindings[key]).apply {
          goTo(end)
        }
      }

      override fun generateDefault() {
        throwException(Type.getType(IllegalArgumentException::class.java), "Unknown handle")
      }
    })
  }
}
