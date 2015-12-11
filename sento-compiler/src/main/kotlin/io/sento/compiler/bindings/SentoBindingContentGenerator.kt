package io.sento.compiler.bindings

import io.sento.compiler.ClassWriter
import io.sento.compiler.ContentGenerator
import io.sento.compiler.GeneratedContent
import io.sento.compiler.GenerationEnvironment
import io.sento.compiler.common.GeneratorAdapter
import io.sento.compiler.common.Methods
import io.sento.compiler.common.Types
import io.sento.compiler.common.isPrivate
import io.sento.compiler.common.newMethod
import io.sento.compiler.model.BindingSpec
import io.sento.compiler.model.ViewOwner
import io.sento.compiler.reflection.ClassSpec
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.Opcodes.ACC_FINAL
import org.objectweb.asm.Opcodes.ACC_PRIVATE
import org.objectweb.asm.Opcodes.ACC_PROTECTED
import org.objectweb.asm.Opcodes.ACC_PUBLIC
import org.objectweb.asm.Opcodes.ACC_STATIC
import org.objectweb.asm.Opcodes.ACC_SUPER
import org.objectweb.asm.Opcodes.ACC_SYNTHETIC
import org.objectweb.asm.Opcodes.ASM5
import org.slf4j.LoggerFactory
import java.util.ArrayList

internal class SentoBindingContentGenerator(private val clazz: ClassSpec) : ContentGenerator {
  public companion object {
    private val logger = LoggerFactory.getLogger(SentoBindingContentGenerator::class.java)

    private const val ARGUMENT_TARGET = 0
    private const val ARGUMENT_SOURCE = 1
    private const val ARGUMENT_FINDER = 2

    public const val EXTRA_BINDING_SPEC = "EXTRA_BINDING_SPEC"
  }

  override fun generate(environment: GenerationEnvironment): Collection<GeneratedContent> {
    val binding = BindingSpec.from(clazz, environment)
    val result = ArrayList<GeneratedContent>()

    if (!binding.bindings.isEmpty() || !binding.listeners.isEmpty()) {
      logger.info("Generating SentoBinding for '{}' class:", clazz.type.className)

      result.add(GeneratedContent.from(environment.naming.getBindingType(clazz), mapOf(EXTRA_BINDING_SPEC to clazz), environment.newClass {
        visitHeader(environment)
        visitConstructor(environment)

        visitBindMethod(binding, environment)
        visitUnbindMethod(binding, environment)
      }))

      result.add(GeneratedContent.from(clazz.type, mapOf(), environment.newClass {
        onCreatePatchedClassForBinding(this, binding, environment)
        onCreateSyntheticFieldsForListeners(this, binding, environment)
        onCreateSyntheticFieldsForViews(this, binding, environment)
        onCreateSyntheticMethodsForListeners(this, binding, environment)
      }))

      binding.listeners.flatMapTo(result) {
        ListenerBinder().generate(it, environment)
      }
    }

    return result
  }

  private fun ClassWriter.visitHeader(environment: GenerationEnvironment) = apply {
    visit(ACC_PUBLIC + ACC_SUPER, environment.naming.getBindingType(clazz), null, Types.OBJECT, arrayOf(Types.BINDING))
  }

  private fun ClassWriter.visitConstructor(environment: GenerationEnvironment) {
    newMethod(ACC_PUBLIC, Methods.getConstructor()) {
      loadThis()
      invokeConstructor(Types.OBJECT, Methods.getConstructor())
    }
  }

  private fun ClassWriter.visitBindMethod(binding: BindingSpec, environment: GenerationEnvironment) {
    val descriptor = Methods.get("bind", Types.VOID, Types.OBJECT, Types.OBJECT, Types.FINDER)
    val signature = "<S:Ljava/lang/Object;>(Ljava/lang/Object;TS;Lio/sento/Finder<-TS;>;)V"

    newMethod(ACC_PUBLIC, descriptor, signature) {
      val variables = VariablesContext()

      variables.variable("target", newLocal(clazz.type).apply {
        loadArg(ARGUMENT_TARGET)
        checkCast(clazz.type)
        storeLocal(this)
      })

      binding.views.distinctBy { it.id }.forEach {
        variables.variable("view${it.id}", newLocal(Types.VIEW).apply {
          loadArg(ARGUMENT_FINDER)
          push(it.id)

          loadArg(ARGUMENT_SOURCE)
          invokeInterface(Types.FINDER, Methods.get("find", Types.VIEW, Types.INT, Types.OBJECT))

          storeLocal(this)
        })
      }

      binding.views.filter { !it.optional }.distinctBy { it.id }.forEach {
        loadArg(ARGUMENT_FINDER)
        push(it.id)

        loadLocal(variables.view(it.id))
        loadArg(ARGUMENT_SOURCE)
        push(it.owner.name)

        invokeInterface(Types.FINDER, Methods.get("require", Types.VOID, Types.INT, Types.VIEW, Types.OBJECT, Types.STRING))
      }

      ViewBinder().bind(binding.bindings, variables, this, environment)
      onBindSyntheticViewFields(this, binding, variables, environment)
      ListenerBinder().bind(binding.listeners, variables, this, environment)
    }
  }

  private fun ClassWriter.visitUnbindMethod(binding: BindingSpec, environment: GenerationEnvironment) {
    newMethod(ACC_PUBLIC, Methods.get("unbind", Types.VOID, Types.OBJECT)) {
      val variables = VariablesContext()

      variables.variable("target", newLocal(clazz.type).apply {
        loadArg(ARGUMENT_TARGET)
        checkCast(clazz.type)
        storeLocal(this)
      })

      ListenerBinder().unbind(binding.listeners, variables, this, environment)
      onUnbindSyntheticViewFields(this, binding, variables, environment)
      ViewBinder().unbind(binding.bindings, variables, this, environment)
    }
  }

  private fun onBindSyntheticViewFields(adapter: GeneratorAdapter, binding: BindingSpec, variables: VariablesContext, environment: GenerationEnvironment) {
    binding.views.filter { it.owner is ViewOwner.Method }.distinctBy { it.id }.forEach {
      adapter.loadLocal(variables.target())
      adapter.loadLocal(variables.view(it.id))
      adapter.putField(it.clazz, environment.naming.getSyntheticFieldName(it), Types.VIEW)
    }
  }

  private fun onUnbindSyntheticViewFields(adapter: GeneratorAdapter, binding: BindingSpec, variables: VariablesContext, environment: GenerationEnvironment) {
    binding.views.filter { it.owner is ViewOwner.Method }.distinctBy { it.id }.forEach {
      adapter.loadLocal(variables.target())
      adapter.pushNull()
      adapter.putField(it.clazz, environment.naming.getSyntheticFieldName(it), Types.VIEW)
    }
  }

  private fun onCreatePatchedClassForBinding(writer: ClassWriter, binding: BindingSpec, environment: GenerationEnvironment) {
    ClassReader(clazz.opener.open()).accept(object : ClassVisitor(ASM5, writer) {
      override fun visitField(access: Int, name: String, desc: String, signature: String?, value: Any?): FieldVisitor? {
        return super.visitField(onPatchFieldAccessFlags(binding, access, name), name, desc, signature, value)
      }
    }, ClassReader.SKIP_FRAMES)
  }

  private fun onCreateSyntheticFieldsForViews(writer: ClassWriter, binding: BindingSpec, environment: GenerationEnvironment) {
    binding.views.filter { it.owner is ViewOwner.Method }.distinctBy { it.id }.forEach {
      writer.visitField(ACC_PROTECTED + ACC_SYNTHETIC, environment.naming.getSyntheticFieldName(it), Types.VIEW)
    }
  }

  private fun onCreateSyntheticFieldsForListeners(writer: ClassWriter, binding: BindingSpec, environment: GenerationEnvironment) {
    binding.listeners.distinctBy { it.method.name to it.annotation.type }.forEach {
      writer.visitField(ACC_PROTECTED + ACC_SYNTHETIC, environment.naming.getSyntheticFieldName(it), it.listener.listener.type)
    }
  }

  private fun onCreateSyntheticMethodsForListeners(writer: ClassWriter, binding: BindingSpec, environment: GenerationEnvironment) {
    binding.listeners.filter { it.method.access.isPrivate }.forEach {
      writer.newMethod(ACC_PUBLIC + ACC_STATIC + ACC_SYNTHETIC, environment.naming.getSyntheticAccessor(clazz, it.method)) {
        val args = it.method.arguments

        for (count in 0..args.size) {
          loadArg(count)
        }

        invokeVirtual(clazz, it.method)
      }
    }
  }

  private fun onPatchFieldAccessFlags(binding: BindingSpec, access: Int, name: String): Int {
    return if (!binding.bindings.any { it.field.name == name }) access else {
      access and ACC_PRIVATE.inv() and ACC_FINAL.inv() or ACC_PROTECTED
    }
  }
}
