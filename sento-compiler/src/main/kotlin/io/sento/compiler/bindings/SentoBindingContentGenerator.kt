package io.sento.compiler.bindings

import io.sento.compiler.ContentGenerator
import io.sento.compiler.GeneratedContent
import io.sento.compiler.GenerationEnvironment
import io.sento.compiler.bindings.fields.FieldBindingContext
import io.sento.compiler.bindings.fields.FieldBindingGenerator
import io.sento.compiler.bindings.methods.MethodBindingContext
import io.sento.compiler.bindings.methods.MethodBindingGenerator
import io.sento.compiler.common.Types
import io.sento.compiler.model.SentoBindingSpec
import io.sento.compiler.model.ClassSpec
import io.sento.compiler.model.FieldSpec
import io.sento.compiler.model.MethodSpec
import io.sento.compiler.patcher.AccessibilityPatcher
import io.sento.compiler.patcher.ClassPatcher
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Type

import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.commons.GeneratorAdapter
import org.objectweb.asm.commons.Method
import java.util.ArrayList
import java.util.HashMap

internal class SentoBindingContentGenerator(
    private val fields: Map<Type, FieldBindingGenerator>,
    private val methods: Map<Type, MethodBindingGenerator>,
    private val clazz: ClassSpec
) : ContentGenerator {
  public companion object {
    public const val EXTRA_BINDING_SPEC = "EXTRA_BINDING_SPEC"
  }

  override fun generate(environment: GenerationEnvironment): Collection<GeneratedContent> {
    if (!shouldGenerateBindingClass(clazz, environment)) {
      return emptyList()
    }

    val binding = SentoBindingSpec.create(clazz)
    val result = ArrayList<GeneratedContent>()

    val bytes = environment.createClass {
      visitHeader(binding, environment)
      visitConstructor(binding, environment)

      visitBindMethod(binding, environment).apply {
        result.addAll(this)
      }

      visitUnbindMethod(binding, environment).apply {
        result.addAll(this)
      }
    }

    result.add(GeneratedContent(Types.getClassFilePath(binding.originalType), onGenerateTargetClass(clazz, environment)))
    result.add(GeneratedContent(Types.getClassFilePath(binding.generatedType), bytes, HashMap<String, Any>().apply {
      put(EXTRA_BINDING_SPEC, binding)
    }))

    return result
  }

  private fun onGenerateTargetClass(clazz: ClassSpec, environment: GenerationEnvironment): ByteArray {
    return createAccessibilityPatcher(environment).patch(clazz.opener.open())
  }

  private fun shouldGenerateBindingClass(clazz: ClassSpec, environment: GenerationEnvironment): Boolean {
    return !Types.isSystemClass(clazz.type) && (clazz.fields.any {
      shouldGenerateBindingForField(it, environment)
    } || clazz.methods.any {
      shouldGenerateBindingForMethod(it, environment)
    })
  }

  private fun shouldGenerateBindingForField(field: FieldSpec?, environment: GenerationEnvironment): Boolean {
    return field != null && field.annotations.any {
      fields.containsKey(it.type)
    }
  }

  private fun shouldGenerateBindingForMethod(method: MethodSpec?, environment: GenerationEnvironment): Boolean {
    return method != null && method.annotations.any {
      methods.containsKey(it.type)
    }
  }

  private fun createAccessibilityPatcher(environment: GenerationEnvironment): ClassPatcher {
    return object : AccessibilityPatcher() {
      override fun onPatchFieldFlags(access: Int, name: String, desc: String, signature: String?, value: Any?): Int {
        return if (shouldGenerateBindingForField(clazz.field(name), environment)) {
          access and ACC_PRIVATE.inv() and ACC_PROTECTED.inv() and ACC_FINAL.inv() or ACC_PUBLIC
        } else {
          access
        }
      }

      override fun onPatchMethodFlags(access: Int, name: String, desc: String, signature: String?, exceptions: Array<out String>?): Int {
        return if (shouldGenerateBindingForMethod(clazz.method(name, desc), environment)) {
          access and ACC_PRIVATE.inv() and ACC_PROTECTED.inv() and ACC_FINAL.inv() or ACC_PUBLIC
        } else {
          access
        }
      }
    }
  }

  private fun ClassWriter.visitHeader(binding: SentoBindingSpec, environment: GenerationEnvironment) = apply {
    val name = binding.generatedType.internalName
    val signature = "L${Types.TYPE_OBJECT.internalName};L${Types.TYPE_BINDING.internalName}<L${Types.TYPE_OBJECT.internalName};>;"
    val superName = Types.TYPE_OBJECT.internalName
    val interfaces = arrayOf(Types.TYPE_BINDING.internalName)

    visit(V1_6, ACC_PUBLIC + ACC_SUPER, name, signature, superName, interfaces)
  }

  private fun ClassWriter.visitConstructor(binding: SentoBindingSpec, environment: GenerationEnvironment) {
    GeneratorAdapter(ACC_PUBLIC, Method.getMethod("void <init> ()"), null, null, this).apply {
      loadThis()
      invokeConstructor(Types.TYPE_OBJECT, Method.getMethod("void <init> ()"))

      returnValue()
      endMethod()
    }
  }

  private fun ClassWriter.visitBindMethod(binding: SentoBindingSpec, environment: GenerationEnvironment): List<GeneratedContent> {
    return ArrayList<GeneratedContent>().apply {
      val descriptor = Method.getMethod("void bind (Object, Object, io.sento.Finder)")
      val signature = "<S:Ljava/lang/Object;>(Ljava/lang/Object;TS;Lio/sento/Finder<-TS;>;)V"

      GeneratorAdapter(ACC_PUBLIC, descriptor, signature, null, this@visitBindMethod).apply {
        for (field in binding.clazz.fields) {
          for (annotation in field.annotations) {
            fields[annotation.type]?.let {
              val variables = mapOf("target" to 0, "source" to 1, "finder" to 2)
              val context = FieldBindingContext(field, binding.clazz, annotation, this, variables, binding.factory, environment)

              addAll(it.bind(context, environment))
            }
          }
        }

        for (method in binding.clazz.methods) {
          for (annotation in method.annotations) {
            methods[annotation.type]?.let {
              val variables = mapOf("target" to 0, "source" to 1, "finder" to 2)
              val context = MethodBindingContext(method, binding.clazz, annotation, this, variables, binding.factory, environment)

              addAll(it.bind(context, environment))
            }
          }
        }

        returnValue()
        endMethod()
      }
    }
  }

  private fun ClassWriter.visitUnbindMethod(binding: SentoBindingSpec, environment: GenerationEnvironment): List<GeneratedContent> {
    return ArrayList<GeneratedContent>().apply {
      GeneratorAdapter(ACC_PUBLIC, Method.getMethod("void unbind (Object)"), null, null, this@visitUnbindMethod).apply {
        for (field in binding.clazz.fields) {
          for (annotation in field.annotations) {
            fields[annotation.type]?.let {
              val variables = mapOf("target" to 0)
              val context = FieldBindingContext(field, binding.clazz, annotation, this, variables, binding.factory, environment)

              addAll(it.unbind(context, environment))
            }
          }
        }

        for (method in binding.clazz.methods) {
          for (annotation in method.annotations) {
            methods[annotation.type]?.let {
              val variables = mapOf("target" to 0)
              val context = MethodBindingContext(method, binding.clazz, annotation, this, variables, binding.factory, environment)

              addAll(it.unbind(context, environment))
            }
          }
        }

        returnValue()
        endMethod()
      }
    }
  }
}
