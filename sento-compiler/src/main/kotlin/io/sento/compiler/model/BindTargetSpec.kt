package io.sento.compiler.model

import io.sento.compiler.GenerationEnvironment
import io.sento.compiler.SentoException
import io.sento.compiler.common.Types
import io.sento.compiler.common.isInterface
import io.sento.compiler.common.simpleName
import io.sento.compiler.reflect.AnnotationSpec
import io.sento.compiler.reflect.ClassSpec
import io.sento.compiler.reflect.FieldSpec
import org.objectweb.asm.Type

internal data class BindTargetSpec private constructor(
    public val clazz: ClassSpec,
    public val field: FieldSpec,
    public val annotation: AnnotationSpec,
    public val optional: Boolean
) {
  public companion object {
    public fun create(clazz: ClassSpec, field: FieldSpec, annotation: AnnotationSpec, optional: Boolean, environment: GenerationEnvironment) : BindTargetSpec {
      if (field.type.sort == Type.ARRAY) {
        throw SentoException("Unable to generate @{0} binding for ''{1}#{2}\'' field - arrays are not supported, but ''{3}'' was found.",
            annotation.type.simpleName, clazz.type.className, field.name, field.type.className)
      }

      val isView = environment.registry.isSubclassOf(field.type, Types.VIEW)
      val isInterface = environment.registry.reference(field.type).access.isInterface

      if (!isInterface && !isView) {
        throw SentoException("Unable to generate @{0} binding for ''{1}#{2}\'' field - it must be a subclass of ''{3}'' or an interface, but ''{4}'' was found.",
            annotation.type.simpleName, clazz.type.className, field.name, Types.VIEW.className, field.type.className)
      }

      return BindTargetSpec(clazz, field, annotation, optional)
    }
  }
}
