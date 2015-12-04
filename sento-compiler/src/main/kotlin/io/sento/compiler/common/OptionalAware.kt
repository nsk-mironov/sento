package io.sento.compiler.common

import io.sento.compiler.reflection.AnnotationSpec
import io.sento.compiler.reflection.ClassSpec
import io.sento.compiler.reflection.FieldSpec
import io.sento.compiler.reflection.MethodSpec
import kotlin.jvm.internal.KotlinClass
import kotlin.reflect.jvm.internal.impl.serialization.ClassData
import kotlin.reflect.jvm.internal.impl.serialization.jvm.JvmProtoBufUtil

internal class OptionalAware(private val spec: ClassSpec) {
  private companion object {
    private val IS_ANNOTATION_OPTIONAL: (spec: AnnotationSpec) -> Boolean = {
      it.type == Types.OPTIONAL
    }

    private val IS_ANNOTATION_NULLABLE: (spec: AnnotationSpec) -> Boolean = {
      it.type.className.endsWith(".Nullable")
    }

    private val IS_ANNOTATION_NOT_NULL: (spec: AnnotationSpec) -> Boolean = {
      it.type.className.endsWith(".NotNull")
    }
  }

  private val metadata by lazy(LazyThreadSafetyMode.NONE) {
    createKotlinMetaData()
  }

  public fun isOptional(field: FieldSpec): Boolean {
    if (field.annotations.any(IS_ANNOTATION_NOT_NULL)) {
      return false
    }

    if (field.annotations.any(IS_ANNOTATION_NULLABLE)) {
      return true
    }

    if (field.annotations.any(IS_ANNOTATION_OPTIONAL)) {
      return true
    }

    val resolver = metadata?.nameResolver ?: return false
    val proto = metadata?.classProto ?: return false

    return proto.propertyList.any {
      resolver.getName(it.name).asString() == field.name && it.returnType.nullable
    }
  }

  public fun isOptional(method: MethodSpec): Boolean {
    return method.annotations.any(IS_ANNOTATION_OPTIONAL)
  }

  private fun createKotlinMetaData(): ClassData?  {
    val annotation = spec.getAnnotation<KotlinClass>() ?: return null

    val strings = annotation.strings
    val data = annotation.data

    return JvmProtoBufUtil.readClassDataFrom(data, strings)
  }
}
