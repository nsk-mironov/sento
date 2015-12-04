package io.sento.compiler.reflection

import io.sento.compiler.annotations.AnnotationProxy
import io.sento.compiler.common.Opener
import io.sento.compiler.common.Types
import org.objectweb.asm.Type
import java.util.ArrayList
import java.util.Arrays

internal data class ClassSpec(
    public val access: Int,
    public val type: Type,
    public val parent: Type,
    public val interfaces: Collection<Type>,
    public val annotations: Collection<AnnotationSpec>,
    public val fields: Collection<FieldSpec>,
    public val methods: Collection<MethodSpec>,
    public val opener: Opener
) {
  internal class Builder(val access: Int, val type: Type, val parent: Type, val opener: Opener) {
    private val interfaces = ArrayList<Type>()
    private val annotations = ArrayList<AnnotationSpec>()
    private val fields = ArrayList<FieldSpec>()
    private val methods = ArrayList<MethodSpec>()

    public fun interfaces(values: Collection<Type>): Builder = apply {
      interfaces.addAll(values)
    }

    public fun annotation(annotation: AnnotationSpec): Builder = apply {
      annotations.add(annotation)
    }

    public fun field(field: FieldSpec): Builder = apply {
      fields.add(field)
    }

    public fun method(method: MethodSpec): Builder = apply {
      methods.add(method)
    }

    public fun build(): ClassSpec {
      return ClassSpec(access, type, parent, interfaces, annotations, fields, methods, opener)
    }
  }

  public fun getConstructor(descriptor: String): MethodSpec? {
    return getDeclaredMethod("<init>", descriptor)
  }

  public fun getConstructor(vararg args: Type): MethodSpec? {
    return getDeclaredMethod("<init>", *args)
  }

  public fun getDeclaredMethod(name: String, descriptor: String): MethodSpec? {
    return methods.firstOrNull {
      it.name == name && it.type.descriptor == descriptor
    }
  }

  public fun getDeclaredMethod(name: String, vararg args: Type): MethodSpec? {
    return methods.firstOrNull {
      it.name == name && Arrays.equals(it.arguments, args)
    }
  }

  public fun getDeclaredField(name: String): FieldSpec? {
    return fields.firstOrNull {
      it.name == name
    }
  }

  public inline fun <reified A : Any> getAnnotation(): A? {
    return getAnnotation(A::class.java)
  }

  public fun <A> getAnnotation(annotation: Class<A>): A? {
    return AnnotationProxy.create(annotation, annotations.firstOrNull {
      it.type == Types.getAnnotationType(annotation)
    } ?: return null)
  }
}
