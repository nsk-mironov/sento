package io.sento.compiler.bindings

import io.sento.Bind
import io.sento.BindArray
import io.sento.BindBool
import io.sento.BindColor
import io.sento.BindDimen
import io.sento.BindDrawable
import io.sento.BindInteger
import io.sento.BindString
import io.sento.MethodBinding
import io.sento.compiler.ContentGenerator
import io.sento.compiler.GeneratedContent
import io.sento.compiler.GenerationEnvironment
import io.sento.compiler.bindings.resources.BindArrayBindingGenerator
import io.sento.compiler.bindings.resources.BindBoolBindingGenerator
import io.sento.compiler.bindings.resources.BindColorBindingGenerator
import io.sento.compiler.bindings.resources.BindDimenBindingGenerator
import io.sento.compiler.bindings.resources.BindDrawableBindingGenerator
import io.sento.compiler.bindings.resources.BindIntegerBindingGenerator
import io.sento.compiler.bindings.resources.BindStringBindingGenerator
import io.sento.compiler.common.Types
import io.sento.compiler.common.toClassFilePath
import io.sento.compiler.common.toSourceFilePath
import io.sento.compiler.model.SentoBindingSpec
import io.sento.compiler.model.ClassSpec
import io.sento.compiler.model.FieldSpec
import io.sento.compiler.model.MethodSpec
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

import org.objectweb.asm.Opcodes.*
import java.util.ArrayList
import java.util.HashMap

internal class SentoBindingContentGenerator(private val clazz: ClassSpec) : ContentGenerator {
  public companion object {
    public const val EXTRA_BINDING_SPEC = "EXTRA_BINDING_SPEC"

    private val GENERATORS_FIELDS = HashMap<Type, FieldBindingGenerator<out Annotation>>().apply {
      put(Type.getType(Bind::class.java), ViewBindingGenerator())
      put(Type.getType(BindArray::class.java), BindArrayBindingGenerator())
      put(Type.getType(BindBool::class.java), BindBoolBindingGenerator())
      put(Type.getType(BindColor::class.java), BindColorBindingGenerator())
      put(Type.getType(BindDimen::class.java), BindDimenBindingGenerator())
      put(Type.getType(BindDrawable::class.java), BindDrawableBindingGenerator())
      put(Type.getType(BindInteger::class.java), BindIntegerBindingGenerator())
      put(Type.getType(BindString::class.java), BindStringBindingGenerator())
    }
  }

  override fun onGenerateContent(environment: GenerationEnvironment): List<GeneratedContent> {
    if (!shouldGenerateBindingClass(clazz, environment)) {
      return emptyList()
    }

    val binding = SentoBindingSpec.from(clazz)
    val result = ArrayList<GeneratedContent>()
    val writer = ClassWriter(0)

    writer.visitHeader(binding, environment)
    writer.visitConstructor(binding, environment)

    writer.visitBindMethod(binding, environment).apply {
      result.addAll(this)
    }

    writer.visitUnbindMethod(binding, environment).apply {
      result.addAll(this)
    }

    writer.visitEnd()

    result.add(GeneratedContent(binding.originalType.toClassFilePath(), onGenerateTargetClass(clazz, environment)))
    result.add(GeneratedContent(binding.generatedType.toClassFilePath(), writer.toByteArray(), HashMap<String, Any>().apply {
      put(EXTRA_BINDING_SPEC, binding)
    }))

    return result
  }

  private fun onGenerateTargetClass(clazz: ClassSpec, environment: GenerationEnvironment): ByteArray {
    val writer = ClassWriter(0)

    clazz.toClassReader().accept(object : ClassVisitor(Opcodes.ASM5, writer) {
      override fun visitField(access: Int, name: String, desc: String, signature: String?, value: Any?): FieldVisitor? {
        return super.visitField(if (shouldGenerateBindingForField(clazz.field(name), environment)) {
          access and ACC_PRIVATE.inv() and ACC_PROTECTED.inv() and ACC_FINAL.inv() or ACC_PUBLIC
        } else {
          access
        }, name, desc, signature, value)
      }

      override fun visitMethod(access: Int, name: String, desc: String, signature: String?, exceptions: Array<out String>?): MethodVisitor? {
        return super.visitMethod(if (shouldGenerateBindingForMethod(clazz.method(name), environment)) {
          access and ACC_PRIVATE.inv() and ACC_PROTECTED.inv() and ACC_FINAL.inv() or ACC_PUBLIC
        } else {
          access
        }, name, desc, signature, exceptions)
      }
    }, ClassReader.SKIP_FRAMES)

    return writer.toByteArray()
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
      GENERATORS_FIELDS.containsKey(it.type)
    }
  }

  private fun shouldGenerateBindingForMethod(method: MethodSpec?, environment: GenerationEnvironment): Boolean {
    return method != null && method.annotations.any {
      environment.registry.annotation(it.type)?.annotations?.any {
        it.type == Type.getType(MethodBinding::class.java)
      } ?: false
    }
  }

  private fun ClassWriter.visitHeader(binding: SentoBindingSpec, environment: GenerationEnvironment) = apply {
    val name = binding.generatedType.internalName
    val signature = "L${Types.TYPE_OBJECT.internalName};L${Types.TYPE_BINDING.internalName}<L${Types.TYPE_OBJECT.internalName};>;"
    val superName = Types.TYPE_OBJECT.internalName
    val interfaces = arrayOf(Types.TYPE_BINDING.internalName)
    val source = binding.generatedType.toSourceFilePath()

    visit(Opcodes.V1_6, ACC_PUBLIC + ACC_SUPER, name, signature, superName, interfaces)
    visitSource(source, null)
  }

  private fun ClassWriter.visitConstructor(binding: SentoBindingSpec, environment: GenerationEnvironment) {
    val visitor = visitMethod(ACC_PUBLIC, "<init>", "()V", null, null)

    val start = Label()
    val end = Label()

    visitor.visitCode()
    visitor.visitLabel(start)
    visitor.visitVarInsn(ALOAD, 0)
    visitor.visitMethodInsn(INVOKESPECIAL, Types.TYPE_OBJECT.internalName, "<init>", "()V", false)
    visitor.visitInsn(RETURN)
    visitor.visitLabel(end)
    visitor.visitLocalVariable("this", binding.generatedType.descriptor, "L${binding.generatedType.internalName}<TT;>;", start, end, 0)
    visitor.visitMaxs(1, 1)
    visitor.visitEnd()
  }

  private fun ClassWriter.visitBindMethod(binding: SentoBindingSpec, environment: GenerationEnvironment): List<GeneratedContent> {
    val descriptor = "(L${Types.TYPE_OBJECT.internalName};L${Types.TYPE_OBJECT.internalName};L${Types.TYPE_FINDER.internalName};)V"
    val signature = "<S:L${Types.TYPE_OBJECT.internalName};>(L${Types.TYPE_OBJECT.internalName};TS;L${Types.TYPE_FINDER.internalName}<-TS;>;)V"

    val visitor = visitMethod(ACC_PUBLIC, "bind", descriptor, signature, null)
    val result = ArrayList<GeneratedContent>()

    val start = Label()
    val end = Label()

    visitor.visitCode()
    visitor.visitLabel(start)

    binding.clazz.fields.forEach { field ->
      field.annotations.forEach { annotation ->
        val generator = GENERATORS_FIELDS[annotation.type]
        val value = annotation.resolve<Annotation>()

        if (generator != null) {
          val variables = mapOf("this" to 0, "target" to 1, "source" to 2, "finder" to 3)
          val context = FieldBindingContext(field, binding.clazz, value, visitor, variables, binding.factory, environment)

          result.addAll(generator.bind(context, environment))
        }
      }
    }

    for (method in binding.clazz.methods) {
      for (annotation in method.annotations) {
        val spec = environment.registry.annotation(annotation.type)?.getAnnotation<MethodBinding>()

        if (spec != null) {
          val variables = mapOf("this" to 0, "target" to 1, "source" to 2, "finder" to 3)
          val context = MethodBindingContext(method, binding.clazz, spec, annotation, visitor, variables, binding.factory, environment)
          val generator = MethodBindingGenerator()

          result.addAll(generator.bind(context, environment))
        }
      }
    }

    visitor.visitInsn(RETURN)
    visitor.visitLabel(end)

    visitor.visitLocalVariable("this", binding.generatedType.descriptor, null, start, end, 0)
    visitor.visitLocalVariable("target", Types.TYPE_OBJECT.descriptor, null, start, end, 1)
    visitor.visitLocalVariable("source", Types.TYPE_OBJECT.descriptor, "TS;", start, end, 2)
    visitor.visitLocalVariable("finder", Types.TYPE_FINDER.descriptor, "L${Types.TYPE_FINDER.internalName}<-TS;>;", start, end, 3)

    visitor.visitMaxs(5, 4)
    visitor.visitEnd()

    return result
  }

  private fun ClassWriter.visitUnbindMethod(binding: SentoBindingSpec, environment: GenerationEnvironment): List<GeneratedContent> {
    val visitor = visitMethod(ACC_PUBLIC, "unbind", "(L${Types.TYPE_OBJECT.internalName};)V", null, null)
    val result = ArrayList<GeneratedContent>()

    val start = Label()
    val end = Label()

    visitor.visitCode()
    visitor.visitLabel(start)

    binding.clazz.fields.forEach { field ->
      field.annotations.forEach { annotation ->
        val generator = GENERATORS_FIELDS[annotation.type]
        val value = annotation.resolve<Annotation>()

        if (generator != null) {
          val variables = mapOf("this" to 0, "target" to 1)
          val context = FieldBindingContext(field, binding.clazz, value, visitor, variables, binding.factory, environment)

          result.addAll(generator.unbind(context, environment))
        }
      }
    }

    visitor.visitInsn(RETURN)
    visitor.visitLabel(end)
    visitor.visitLocalVariable("this", binding.generatedType.descriptor, null, start, end, 0)
    visitor.visitLocalVariable("target", Types.TYPE_OBJECT.descriptor, null, start, end, 1)
    visitor.visitMaxs(2, 2)
    visitor.visitEnd()

    return result
  }
}
