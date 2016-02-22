package io.mironov.sento.compiler.common

import io.mironov.sento.compiler.annotations.Metadata
import io.mironov.sento.compiler.annotations.data
import io.mironov.sento.compiler.annotations.kind
import io.mironov.sento.compiler.annotations.strings
import io.mironov.sento.compiler.reflect.AnnotationSpec
import io.mironov.sento.compiler.reflect.ClassSpec
import io.mironov.sento.compiler.reflect.FieldSpec
import io.mironov.sento.compiler.reflect.MethodSpec
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

  fun isOptional(field: FieldSpec): Boolean {
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

  fun isOptional(method: MethodSpec): Boolean {
    return method.annotations.any(IS_ANNOTATION_OPTIONAL)
  }

  private fun createKotlinMetaData(): ClassData?  {
    val annotation = spec.getAnnotation<Metadata>() ?: return null

    if (annotation.kind != Metadata.KIND_CLASS) {
      return null
    }

    val strings = annotation.strings
    val data = annotation.data

    return JvmProtoBufUtil.readClassDataFrom(data, strings)
  }
}
