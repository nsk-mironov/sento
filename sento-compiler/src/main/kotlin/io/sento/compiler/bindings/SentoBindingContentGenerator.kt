package io.sento.compiler.bindings

import io.sento.compiler.ClassWriter
import io.sento.compiler.ContentGenerator
import io.sento.compiler.GeneratedContent
import io.sento.compiler.GenerationEnvironment
import io.sento.compiler.annotations.id
import io.sento.compiler.annotations.ids
import io.sento.compiler.common.GeneratorAdapter
import io.sento.compiler.common.Methods
import io.sento.compiler.common.Types
import io.sento.compiler.common.isPublic
import io.sento.compiler.model.BindingSpec
import io.sento.compiler.model.ViewOwner
import io.sento.compiler.model.ViewSpec
import io.sento.compiler.reflection.ClassSpec
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.Opcodes.ACC_FINAL
import org.objectweb.asm.Opcodes.ACC_PROTECTED
import org.objectweb.asm.Opcodes.ACC_PUBLIC
import org.objectweb.asm.Opcodes.ACC_SUPER
import org.objectweb.asm.Opcodes.ACC_SYNTHETIC
import org.objectweb.asm.Opcodes.ASM5
import java.util.ArrayList

internal class SentoBindingContentGenerator(private val clazz: ClassSpec) : ContentGenerator {
  public companion object {
    public const val EXTRA_BINDING_SPEC = "EXTRA_BINDING_SPEC"

    private const val ARGUMENT_TARGET = 0
    private const val ARGUMENT_SOURCE = 1
    private const val ARGUMENT_FINDER = 2
  }

  override fun generate(environment: GenerationEnvironment): Collection<GeneratedContent> {
    val binding = BindingSpec.from(clazz, environment)
    val result = ArrayList<GeneratedContent>()

    if (!binding.bindings.isEmpty() || !binding.listeners.isEmpty()) {
      result.add(onCreateBindingClassGeneratedContent(binding, environment))
      result.add(onCreatePatchedClassGeneratedContent(binding, environment))

      binding.listeners.flatMapTo(result) {
        ListenerBindingContentGenerator(it).generate(environment)
      }
    }

    return result
  }

  private fun onCreateBindingClassGeneratedContent(binding: BindingSpec, environment: GenerationEnvironment): GeneratedContent {
    return GeneratedContent.from(environment.naming.getBindingType(clazz), mapOf(EXTRA_BINDING_SPEC to clazz), environment.newClass {
      visit(ACC_PUBLIC + ACC_SUPER, environment.naming.getBindingType(clazz), null, Types.OBJECT, arrayOf(Types.BINDING))

      newMethod(ACC_PUBLIC, Methods.getConstructor()) {
        loadThis()
        invokeConstructor(Types.OBJECT, Methods.getConstructor())
      }

      newMethod(environment.naming.getBindMethodSpec()) {
        val method = environment.naming.getSyntheticBindMethodSpec()

        for (index in 0..method.arguments.size - 1) {
          loadArg(index)
        }

        invokeStatic(clazz, method)
      }

      newMethod(environment.naming.getUnbindMethodSpec()) {
        val method = environment.naming.getSyntheticUnbindMethodSpec()

        for (index in 0..method.arguments.size - 1) {
          loadArg(index)
        }

        invokeStatic(clazz, method)
      }
    })
  }

  private fun onCreatePatchedClassGeneratedContent(binding: BindingSpec, environment: GenerationEnvironment): GeneratedContent {
    return GeneratedContent.from(clazz.type, mapOf(), environment.newClass {
      onCreatePatchedClassForBinding(this, binding, environment)
      onCreateSyntheticFieldsForListeners(this, binding, environment)
      onCreateSyntheticFieldsForViews(this, binding, environment)
      onCreateSyntheticMethodsForListeners(this, binding, environment)

      newMethod(environment.naming.getSyntheticBindMethodSpec()) {
        VariablesContext().apply {
          onCreateLocalVariablesFromArgs(this@newMethod, binding, this, environment)
          onCreateLocalVariablesForViews(this@newMethod, binding, this, environment)
          onEnforceRequiredViewTargets(this@newMethod, binding, this, environment)

          onBindViewTargetFields(this@newMethod, binding, this, environment)
          onBindSyntheticViewFields(this@newMethod, binding, this, environment)

          onBindSyntheticListenerFields(this@newMethod, binding, this, environment)
          onBindSyntheticListenerTargets(this@newMethod, binding, this, environment)
        }
      }

      newMethod(environment.naming.getSyntheticUnbindMethodSpec()) {
        VariablesContext().apply {
          onCreateLocalVariablesFromArgs(this@newMethod, binding, this, environment)

          onUnbindSyntheticListenerTargets(this@newMethod, binding, this, environment)
          onUnbindSyntheticListenerFields(this@newMethod, binding, this, environment)

          onUnbindSyntheticViewFields(this@newMethod, binding, this, environment)
          onUnbindViewTargetFields(this@newMethod, binding, this, environment)
        }
      }
    })
  }

  private fun onCreateLocalVariablesFromArgs(adapter: GeneratorAdapter, binding: BindingSpec, variables: VariablesContext, environment: GenerationEnvironment) {
    variables.variable("target", adapter.newLocal(clazz.type).apply {
      adapter.loadArg(ARGUMENT_TARGET)
      adapter.checkCast(clazz.type)
      adapter.storeLocal(this)
    })
  }

  private fun onCreateLocalVariablesForViews(adapter: GeneratorAdapter, binding: BindingSpec, variables: VariablesContext, environment: GenerationEnvironment) {
    binding.views.distinctBy { it.id }.forEach {
      variables.variable("view${it.id}", adapter.newLocal(Types.VIEW).apply {
        adapter.loadArg(ARGUMENT_FINDER)
        adapter.push(it.id)

        adapter.loadArg(ARGUMENT_SOURCE)
        adapter.invokeInterface(Types.FINDER, Methods.get("find", Types.VIEW, Types.INT, Types.OBJECT))

        adapter.storeLocal(this)
      })
    }
  }

  private fun onEnforceRequiredViewTargets(adapter: GeneratorAdapter, binding: BindingSpec, variables: VariablesContext, environment: GenerationEnvironment) {
    binding.views.filter { !it.optional }.distinctBy { it.id }.forEach {
      adapter.loadArg(ARGUMENT_FINDER)
      adapter.push(it.id)

      adapter.loadLocal(variables.view(it.id))
      adapter.loadArg(ARGUMENT_SOURCE)
      adapter.push(it.owner.name)

      adapter.invokeInterface(Types.FINDER, Methods.get("require", Types.VOID, Types.INT, Types.VIEW, Types.OBJECT, Types.STRING))
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

  private fun onBindSyntheticListenerFields(adapter: GeneratorAdapter, binding: BindingSpec, variables: VariablesContext, environment: GenerationEnvironment) {
    binding.listeners.forEach {
      adapter.loadLocal(variables.target())
      adapter.newInstance(it.type, Methods.getConstructor(it.clazz)) {
        adapter.loadLocal(variables.target())
      }
      adapter.putField(it.clazz, environment.naming.getSyntheticFieldName(it), it.listener.listener)
    }
  }

  private fun onUnbindSyntheticListenerFields(adapter: GeneratorAdapter, binding: BindingSpec, variables: VariablesContext, environment: GenerationEnvironment) {
    binding.listeners.forEach {
      adapter.loadLocal(variables.target())
      adapter.pushNull()
      adapter.putField(it.clazz, environment.naming.getSyntheticFieldName(it), it.listener.listener)
    }
  }

  private fun onBindSyntheticListenerTargets(adapter: GeneratorAdapter, binding: BindingSpec, variables: VariablesContext, environment: GenerationEnvironment) {
    binding.listeners.forEach {
      for (id in it.annotation.ids) {
        adapter.newLabel().apply {
          val view = ViewSpec(id, it.optional, it.clazz, ViewOwner.Method(it.method))
          val name = environment.naming.getSyntheticFieldName(view)

          if (it.optional) {
            adapter.loadLocal(variables.target())
            adapter.getField(it.clazz, name, Types.VIEW)
            adapter.ifNull(this)
          }

          adapter.loadLocal(variables.target())
          adapter.getField(it.clazz, name, Types.VIEW)

          if (it.listener.owner.type != Types.VIEW) {
            adapter.checkCast(it.listener.owner)
          }

          adapter.loadLocal(variables.target())
          adapter.getField(it.clazz, environment.naming.getSyntheticFieldName(it), it.listener.listener)

          adapter.invokeVirtual(it.listener.owner, it.listener.setter)
          adapter.mark(this)
        }
      }
    }
  }

  private fun onUnbindSyntheticListenerTargets(adapter: GeneratorAdapter, binding: BindingSpec, variables: VariablesContext, environment: GenerationEnvironment) {
    binding.listeners.forEach {
      for (id in it.annotation.ids) {
        adapter.newLabel().apply {
          val view = ViewSpec(id, it.optional, it.clazz, ViewOwner.Method(it.method))
          val name = environment.naming.getSyntheticFieldName(view)

          if (it.optional) {
            adapter.loadLocal(variables.target())
            adapter.getField(it.clazz, name, Types.VIEW)
            adapter.ifNull(this)
          }

          adapter.loadLocal(variables.target())
          adapter.getField(it.clazz, name, Types.VIEW)

          if (it.listener.owner.type != Types.VIEW) {
            adapter.checkCast(it.listener.owner)
          }

          if (it.listener.setter != it.listener.unsetter) {
            adapter.loadLocal(variables.target())
            adapter.getField(it.clazz, environment.naming.getSyntheticFieldName(it), it.listener.listener)
          }

          if (it.listener.setter == it.listener.unsetter) {
            adapter.pushNull()
          }

          adapter.invokeVirtual(it.listener.owner, it.listener.unsetter)
          adapter.mark(this)
        }
      }
    }
  }

  private fun onBindViewTargetFields(adapter: GeneratorAdapter, binding: BindingSpec, variables: VariablesContext, environment: GenerationEnvironment) {
    binding.bindings.forEach {
      adapter.loadLocal(variables.target())
      adapter.loadLocal(variables.view(it.annotation.id))

      if (it.field.type != Types.VIEW) {
        adapter.checkCast(it.field.type)
      }

      adapter.putField(it.clazz, it.field)
    }
  }

  private fun onUnbindViewTargetFields(adapter: GeneratorAdapter, binding: BindingSpec, variables: VariablesContext, environment: GenerationEnvironment) {
    binding.bindings.forEach {
      adapter.loadLocal(variables.target())
      adapter.pushNull()
      adapter.putField(it.clazz, it.field)
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
    binding.listeners.filter { !it.method.access.isPublic }.forEach {
      writer.newSyntheticAccessor(clazz, it.method, environment.naming.getSyntheticAccessorName(clazz, it.method))
    }
  }

  private fun onPatchFieldAccessFlags(binding: BindingSpec, access: Int, name: String): Int {
    return if (!binding.bindings.any { it.field.name == name }) access else {
      access and ACC_FINAL.inv()
    }
  }
}
