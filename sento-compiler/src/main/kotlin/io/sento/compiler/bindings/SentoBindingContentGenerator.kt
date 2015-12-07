package io.sento.compiler.bindings

import io.sento.compiler.ContentGenerator
import io.sento.compiler.GeneratedContent
import io.sento.compiler.GenerationEnvironment
import io.sento.compiler.annotations.ids
import io.sento.compiler.common.Methods
import io.sento.compiler.common.OptionalAware
import io.sento.compiler.common.Types
import io.sento.compiler.common.isPrivate
import io.sento.compiler.common.isStatic
import io.sento.compiler.common.isSynthetic
import io.sento.compiler.common.newMethod
import io.sento.compiler.model.BindTargetSpec
import io.sento.compiler.model.ListenerTargetSpec
import io.sento.compiler.model.ViewSpec
import io.sento.compiler.reflection.ClassSpec
import io.sento.compiler.reflection.FieldSpec
import io.sento.compiler.reflection.MethodSpec
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.Opcodes.ACC_FINAL
import org.objectweb.asm.Opcodes.ACC_PRIVATE
import org.objectweb.asm.Opcodes.ACC_PROTECTED
import org.objectweb.asm.Opcodes.ACC_PUBLIC
import org.objectweb.asm.Opcodes.ACC_STATIC
import org.objectweb.asm.Opcodes.ACC_SUPER
import org.objectweb.asm.Opcodes.ACC_SYNTHETIC
import org.objectweb.asm.Opcodes.ASM5
import org.objectweb.asm.Opcodes.V1_6
import org.objectweb.asm.Type
import org.slf4j.LoggerFactory
import java.util.ArrayList
import java.util.HashMap

internal class SentoBindingContentGenerator(
    private val environment: GenerationEnvironment,
    private val bindings: Set<Type>,
    private val listeners: Set<Type>,
    private val clazz: ClassSpec
) : ContentGenerator {
  public companion object {
    public const val EXTRA_BINDING_SPEC = "EXTRA_BINDING_SPEC"

    private const val ARGUMENT_TARGET = 0
    private const val ARGUMENT_SOURCE = 1
    private const val ARGUMENT_FINDER = 2
  }

  private val logger = LoggerFactory.getLogger(SentoBindingContentGenerator::class.java)
  private val optional = OptionalAware(clazz)

  private val bindableFieldTargets by lazy(LazyThreadSafetyMode.NONE) {
    clazz.fields.flatMap { field ->
      field.annotations.filter { bindings.contains(it.type) }.map {
        BindTargetSpec(clazz, field, it, optional.isOptional(field))
      }
    }
  }

  private val bindableMethodTargets by lazy(LazyThreadSafetyMode.NONE) {
    clazz.methods.flatMap { method ->
      method.annotations.filter { listeners.contains(it.type) }.map {
        ListenerTargetSpec.create(clazz, method, it, optional.isOptional(method), environment)
      }
    }
  }

  private val bindableViewTargetsForFields by lazy(LazyThreadSafetyMode.NONE) {
    bindableFieldTargets.flatMap { field ->
      field.annotation.ids.map { id ->
        ViewSpec(id, field.optional, clazz, "field '${field.field.name}'")
      }
    }
  }

  private val bindableViewTargetsForMethods by lazy(LazyThreadSafetyMode.NONE) {
    bindableMethodTargets.flatMap { method ->
      method.annotation.ids.map { id ->
        ViewSpec(id, method.optional, clazz, "method '${method.method.name}'")
      }
    }
  }

  private val bindableViewTargetsForAll by lazy(LazyThreadSafetyMode.NONE) {
    bindableViewTargetsForFields + bindableViewTargetsForMethods
  }

  override fun generate(): Collection<GeneratedContent> {
    return ArrayList<GeneratedContent>().apply {
      if (shouldGenerateBindingClass(clazz, environment)) {
        logger.info("Generating SentoBinding for '{}' class:", clazz.type.className)

        val bytes = environment.newClass {
          visitHeader(environment)
          visitConstructor(environment)
          visitBindMethod(bindableMethodTargets, environment)
          visitUnbindMethod(bindableMethodTargets, environment)
        }

        add(GeneratedContent(Types.getClassFilePath(clazz.type), AccessibilityPatcher(environment).patch(clazz)))
        add(GeneratedContent(Types.getClassFilePath(environment.naming.getSentoBindingType(clazz)), bytes, HashMap<String, Any>().apply {
          put(EXTRA_BINDING_SPEC, clazz)
        }))

        addAll(bindableMethodTargets.flatMap {
          ListenerBinder().generate(it, environment)
        })
      }
    }
  }

  private fun shouldGenerateBindingClass(clazz: ClassSpec, environment: GenerationEnvironment): Boolean {
    return !Types.isSystemClass(clazz.type) && (clazz.fields.any {
      shouldGenerateBindingForField(it, environment)
    } || clazz.methods.any {
      shouldGenerateBindingForMethod(it, environment)
    })
  }

  private fun shouldGenerateBindingForField(field: FieldSpec?, environment: GenerationEnvironment): Boolean {
    return field != null && !field.access.isStatic && !field.access.isSynthetic && field.annotations.any {
      bindings.contains(it.type)
    }
  }

  private fun shouldGenerateBindingForMethod(method: MethodSpec?, environment: GenerationEnvironment): Boolean {
    return method != null && !method.access.isStatic && !method.access.isSynthetic && method.annotations.any {
      listeners.contains(it.type)
    }
  }

  private fun ClassWriter.visitHeader(environment: GenerationEnvironment) = apply {
    val name = environment.naming.getSentoBindingType(clazz).internalName
    val signature = "L${Types.OBJECT.internalName};L${Types.BINDING.internalName}<L${Types.OBJECT.internalName};>;"
    val superName = Types.OBJECT.internalName
    val interfaces = arrayOf(Types.BINDING.internalName)

    visit(V1_6, ACC_PUBLIC + ACC_SUPER, name, signature, superName, interfaces)
  }

  private fun ClassWriter.visitConstructor(environment: GenerationEnvironment) {
    newMethod(ACC_PUBLIC, Methods.getConstructor()) {
      loadThis()
      invokeConstructor(Types.OBJECT, Methods.getConstructor())
    }
  }

  private fun ClassWriter.visitBindMethod(listeners: Collection<ListenerTargetSpec>, environment: GenerationEnvironment) {
    val descriptor = Methods.get("bind", Types.VOID, Types.OBJECT, Types.OBJECT, Types.FINDER)
    val signature = "<S:Ljava/lang/Object;>(Ljava/lang/Object;TS;Lio/sento/Finder<-TS;>;)V"

    newMethod(ACC_PUBLIC, descriptor, signature) {
      val variables = HashMap<String, Int>()

      variables.put("target", newLocal(clazz.type).apply {
        loadArg(ARGUMENT_TARGET)
        checkCast(clazz.type)
        storeLocal(this)
      })

      bindableViewTargetsForAll.distinctBy { it.id }.forEach {
        variables.put("view${it.id}", newLocal(Types.VIEW).apply {
          loadArg(ARGUMENT_FINDER)
          push(it.id)

          loadArg(ARGUMENT_SOURCE)
          invokeInterface(Types.FINDER, Methods.get("find", Types.VIEW, Types.INT, Types.OBJECT))

          storeLocal(this)
        })
      }

      bindableViewTargetsForAll.filter { !it.optional }.distinctBy { it.id }.forEach {
        loadArg(ARGUMENT_FINDER)
        push(it.id)

        loadLocal(variables["view${it.id}"]!!)
        loadArg(ARGUMENT_SOURCE)
        push(it.description)

        invokeInterface(Types.FINDER, Methods.get("require", Types.VOID, Types.INT, Types.VIEW, Types.OBJECT, Types.STRING))
      }

      ViewBinder().bind(bindableFieldTargets, VariablesContext(variables), this, environment)
      ShadowBinder().bind(bindableViewTargetsForMethods, VariablesContext(variables), this, environment)
      ListenerBinder().bind(listeners, VariablesContext(variables), this, environment)
    }
  }

  private fun ClassWriter.visitUnbindMethod(listeners: Collection<ListenerTargetSpec>, environment: GenerationEnvironment) {
    newMethod(ACC_PUBLIC, Methods.get("unbind", Types.VOID, Types.OBJECT)) {
      val variables = mapOf("target" to newLocal(clazz.type).apply {
        loadArg(ARGUMENT_TARGET)
        checkCast(clazz.type)
        storeLocal(this)
      })

      ListenerBinder().unbind(listeners, VariablesContext(variables), this, environment)
      ShadowBinder().unbind(bindableViewTargetsForMethods, VariablesContext(variables), this, environment)
      ViewBinder().unbind(bindableFieldTargets, VariablesContext(variables), this, environment)
    }
  }

  private inner class AccessibilityPatcher(val environment: GenerationEnvironment) {
    public fun patch(spec: ClassSpec): ByteArray {
      val writer = environment.newClassWriter()

      val bytes = spec.opener.open()
      val reader = ClassReader(bytes)

      reader.accept(object : ClassVisitor(ASM5, writer) {
        override fun visitField(access: Int, name: String, desc: String, signature: String?, value: Any?): FieldVisitor? {
          return super.visitField(if (shouldGenerateBindingForField(clazz.getDeclaredField(name), environment)) {
            access and ACC_PRIVATE.inv() and ACC_FINAL.inv() or ACC_PROTECTED
          } else {
            access
          }, name, desc, signature, value)
        }
      }, ClassReader.SKIP_FRAMES)

      bindableViewTargetsForMethods.distinctBy { it.id }.forEach {
        writer.visitField(ACC_PROTECTED + ACC_SYNTHETIC, environment.naming.getSyntheticFieldNameForViewTarget(it), Types.VIEW.descriptor, null, null)
      }

      bindableMethodTargets.distinctBy { it.method.name to it.annotation.type }.forEach {
        writer.visitField(ACC_PROTECTED + ACC_SYNTHETIC, environment.naming.getSyntheticFieldNameForMethodTarget(it), it.listener.listener.type.descriptor, null, null)
      }

      bindableMethodTargets.filter { it.method.access.isPrivate }.forEach {
        writer.newMethod(ACC_PUBLIC + ACC_STATIC + ACC_SYNTHETIC, environment.naming.getSyntheticAccessor(spec, it.method)) {
          for (count in 0..it.method.arguments.size) {
            loadArg(count)
          }
          invokeVirtual(spec, it.method)
        }
      }

      return writer.toByteArray()
    }
  }
}
