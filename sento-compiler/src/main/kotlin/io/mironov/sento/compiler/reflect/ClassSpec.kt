package io.mironov.sento.compiler.reflect

import io.mironov.sento.compiler.annotations.AnnotationProxy
import io.mironov.sento.compiler.common.Opener
import io.mironov.sento.compiler.common.Types
import org.objectweb.asm.Type
import java.util.ArrayList
import java.util.Arrays

internal data class ClassSpec(
    val access: Int,
    val type: Type,
    val parent: Type,
    val interfaces: Collection<Type>,
    val annotations: Collection<AnnotationSpec>,
    val fields: Collection<FieldSpec>,
    val methods: Collection<MethodSpec>,
    val opener: Opener
) {
  internal class Builder(val access: Int, val type: Type, val parent: Type, val opener: Opener) {
    private val interfaces = ArrayList<Type>()
    private val annotations = ArrayList<AnnotationSpec>()
    private val fields = ArrayList<FieldSpec>()
    private val methods = ArrayList<MethodSpec>()

    fun interfaces(values: Collection<Type>): Builder = apply {
      interfaces.addAll(values)
    }

    fun annotation(annotation: AnnotationSpec): Builder = apply {
      annotations.add(annotation)
    }

    fun field(field: FieldSpec): Builder = apply {
      fields.add(field)
    }

    fun method(method: MethodSpec): Builder = apply {
      methods.add(method)
    }

    fun build(): ClassSpec {
      return ClassSpec(access, type, parent, interfaces, annotations, fields, methods, opener)
    }
  }

  fun getConstructor(vararg args: Type): MethodSpec? {
    return getDeclaredMethod("<init>", *args)
  }

  fun getDeclaredMethod(name: String, descriptor: String): MethodSpec? {
    return methods.firstOrNull {
      it.name == name && it.type.descriptor == descriptor
    }
  }

  fun getDeclaredMethod(name: String, vararg args: Type): MethodSpec? {
    return methods.firstOrNull {
      it.name == name && Arrays.equals(it.arguments, args)
    }
  }

  fun getDeclaredField(name: String): FieldSpec? {
    return fields.firstOrNull {
      it.name == name
    }
  }

  inline fun <reified A : Any> getAnnotation(): A? {
    return getAnnotation(A::class.java)
  }

  fun <A> getAnnotation(annotation: Class<A>): A? {
    return AnnotationProxy.create(annotation, annotations.firstOrNull {
      it.type == Types.getAnnotationType(annotation)
    } ?: return null)
  }
}
