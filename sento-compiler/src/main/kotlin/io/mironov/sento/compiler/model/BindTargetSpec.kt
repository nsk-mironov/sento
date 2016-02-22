package io.mironov.sento.compiler.model

import io.mironov.sento.compiler.GenerationEnvironment
import io.mironov.sento.compiler.SentoException
import io.mironov.sento.compiler.common.Types
import io.mironov.sento.compiler.common.isInterface
import io.mironov.sento.compiler.common.simpleName
import io.mironov.sento.compiler.reflect.AnnotationSpec
import io.mironov.sento.compiler.reflect.ClassSpec
import io.mironov.sento.compiler.reflect.FieldSpec
import org.objectweb.asm.Type

internal data class BindTargetSpec private constructor(
    val clazz: ClassSpec,
    val field: FieldSpec,
    val annotation: AnnotationSpec,
    val views: Collection<ViewSpec>
) {
  companion object {
    fun create(clazz: ClassSpec, field: FieldSpec, annotation: AnnotationSpec, optional: Boolean, environment: GenerationEnvironment) : BindTargetSpec {
      if (field.type.sort == Type.ARRAY) {
        throw SentoException("Unable to generate @{0} binding for ''{1}#{2}\'' field - arrays are not supported, but ''{3}'' was found.",
            annotation.type.simpleName, clazz.type.className, field.name, field.type.className)
      }

      val isView = environment.registry.isSubclassOf(field.type, Types.VIEW)
      val isInterface = environment.registry.reference(field.type).isInterface

      if (!isInterface && !isView) {
        throw SentoException("Unable to generate @{0} binding for ''{1}#{2}\'' field - it must be a subclass of ''{3}'' or an interface, but ''{4}'' was found.",
            annotation.type.simpleName, clazz.type.className, field.name, Types.VIEW.className, field.type.className)
      }

      return BindTargetSpec(clazz, field, annotation, listOf(annotation.value<Int>("value")).map {
        ViewSpec(it, optional, clazz, ViewOwner.from(field))
      })
    }
  }
}
