package io.sento.compiler.model

import io.sento.compiler.common.Opener
import io.sento.compiler.common.Annotations
import org.objectweb.asm.Type
import java.util.ArrayList
import java.util.Arrays

internal data class ClassSpec(
    public val access: Int,
    public val type: Type,
    public val parent: Type,
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
      return ClassSpec(access, type, parent, annotations, fields, methods, opener)
    }
  }

  public fun field(name: String): FieldSpec? {
    return fields.firstOrNull {
      it.name == name
    }
  }

  public fun method(name: String): MethodSpec? {
    return methods.firstOrNull {
      it.name == name
    }
  }

  public fun method(name: String, descriptor: String): MethodSpec? {
    return methods.firstOrNull {
      it.name == name && it.type.descriptor == descriptor
    }
  }

  public fun method(name: String, vararg args: Type): MethodSpec? {
    return methods.firstOrNull {
      it.name == name && Arrays.equals(it.type.argumentTypes, args)
    }
  }

  public inline fun <reified A : Annotation> getAnnotation(): A? {
    return getAnnotation(A::class.java)
  }

  public fun <A : Annotation> getAnnotation(annotation: Class<A>): A? {
    return Annotations.create(annotation, annotations.firstOrNull {
      it.type == Type.getType(annotation)
    } ?: return null)
  }
}
