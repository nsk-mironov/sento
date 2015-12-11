package io.sento.compiler.bindings

import io.sento.compiler.ContentGenerator
import io.sento.compiler.GeneratedContent
import io.sento.compiler.GenerationEnvironment
import io.sento.compiler.common.Methods
import io.sento.compiler.common.Types
import io.sento.compiler.common.isPrivate
import io.sento.compiler.common.newMethod
import io.sento.compiler.model.BindingSpec
import io.sento.compiler.model.ViewOwner
import io.sento.compiler.reflection.ClassSpec
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
import org.slf4j.LoggerFactory
import java.util.ArrayList
import java.util.HashMap

internal class SentoBindingContentGenerator(
    private val environment: GenerationEnvironment,
    private val clazz: ClassSpec
) : ContentGenerator {
  public companion object {
    private val logger = LoggerFactory.getLogger(SentoBindingContentGenerator::class.java)

    private const val ARGUMENT_TARGET = 0
    private const val ARGUMENT_SOURCE = 1
    private const val ARGUMENT_FINDER = 2

    public const val EXTRA_BINDING_SPEC = "EXTRA_BINDING_SPEC"
  }

  override fun generate(): Collection<GeneratedContent> {
    val binding = BindingSpec.from(clazz, environment)
    val result = ArrayList<GeneratedContent>()

    if (!binding.bindings.isEmpty() || !binding.listeners.isEmpty()) {
      logger.info("Generating SentoBinding for '{}' class:", clazz.type.className)

      val bytes = environment.newClass {
        visitHeader(environment)
        visitConstructor(environment)

        visitBindMethod(binding, environment)
        visitUnbindMethod(binding, environment)
      }

      result.add(GeneratedContent(Types.getClassFilePath(clazz.type), AccessibilityPatcher(environment, binding).patch(clazz)))
      result.add(GeneratedContent(Types.getClassFilePath(environment.naming.getSentoBindingType(clazz)), bytes, HashMap<String, Any>().apply {
        put(EXTRA_BINDING_SPEC, clazz)
      }))

      binding.listeners.flatMapTo(result) {
        ListenerBinder().generate(it, environment)
      }
    }

    return result
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

  private fun ClassWriter.visitBindMethod(binding: BindingSpec, environment: GenerationEnvironment) {
    val descriptor = Methods.get("bind", Types.VOID, Types.OBJECT, Types.OBJECT, Types.FINDER)
    val signature = "<S:Ljava/lang/Object;>(Ljava/lang/Object;TS;Lio/sento/Finder<-TS;>;)V"

    newMethod(ACC_PUBLIC, descriptor, signature) {
      val variables = HashMap<String, Int>()

      variables.put("target", newLocal(clazz.type).apply {
        loadArg(ARGUMENT_TARGET)
        checkCast(clazz.type)
        storeLocal(this)
      })

      binding.views.distinctBy { it.id }.forEach {
        variables.put("view${it.id}", newLocal(Types.VIEW).apply {
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

        loadLocal(variables["view${it.id}"]!!)
        loadArg(ARGUMENT_SOURCE)
        push(it.owner.name)

        invokeInterface(Types.FINDER, Methods.get("require", Types.VOID, Types.INT, Types.VIEW, Types.OBJECT, Types.STRING))
      }

      ViewBinder().bind(binding.bindings, VariablesContext(variables), this, environment)
      ShadowBinder().bind(binding.views.filter { it.owner is ViewOwner.Method }, VariablesContext(variables), this, environment)
      ListenerBinder().bind(binding.listeners, VariablesContext(variables), this, environment)
    }
  }

  private fun ClassWriter.visitUnbindMethod(binding: BindingSpec, environment: GenerationEnvironment) {
    newMethod(ACC_PUBLIC, Methods.get("unbind", Types.VOID, Types.OBJECT)) {
      val variables = mapOf("target" to newLocal(clazz.type).apply {
        loadArg(ARGUMENT_TARGET)
        checkCast(clazz.type)
        storeLocal(this)
      })

      ListenerBinder().unbind(binding.listeners, VariablesContext(variables), this, environment)
      ShadowBinder().unbind(binding.views.filter { it.owner is ViewOwner.Method }, VariablesContext(variables), this, environment)
      ViewBinder().unbind(binding.bindings, VariablesContext(variables), this, environment)
    }
  }

  private inner class AccessibilityPatcher(val environment: GenerationEnvironment, val binding: BindingSpec) {
    public fun patch(spec: ClassSpec): ByteArray {
      val fields = binding.bindings.map { it.field.name }.toHashSet()
      val writer = environment.newClassWriter()

      val bytes = spec.opener.open()
      val reader = ClassReader(bytes)

      reader.accept(object : ClassVisitor(ASM5, writer) {
        override fun visitField(access: Int, name: String, desc: String, signature: String?, value: Any?): FieldVisitor? {
          return super.visitField(if (fields.contains(name)) {
            access and ACC_PRIVATE.inv() and ACC_FINAL.inv() or ACC_PROTECTED
          } else {
            access
          }, name, desc, signature, value)
        }
      }, ClassReader.SKIP_FRAMES)

      binding.views.filter { it.owner is ViewOwner.Method }.distinctBy { it.id }.forEach {
        writer.visitField(ACC_PROTECTED + ACC_SYNTHETIC, environment.naming.getSyntheticFieldNameForViewTarget(it), Types.VIEW.descriptor, null, null)
      }

      binding.listeners.distinctBy { it.method.name to it.annotation.type }.forEach {
        writer.visitField(ACC_PROTECTED + ACC_SYNTHETIC, environment.naming.getSyntheticFieldNameForMethodTarget(it), it.listener.listener.type.descriptor, null, null)
      }

      binding.listeners.filter { it.method.access.isPrivate }.forEach {
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
