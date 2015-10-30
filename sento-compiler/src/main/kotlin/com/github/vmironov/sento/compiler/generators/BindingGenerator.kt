package com.github.vmironov.sento.compiler.generators

import com.github.vmironov.sento.compiler.SentoRegistry
import com.github.vmironov.sento.compiler.specs.AnnotationSpec
import com.github.vmironov.sento.compiler.specs.ClassSpec
import com.github.vmironov.sento.compiler.specs.FieldSpec
import com.github.vmironov.sento.compiler.specs.MethodSpec

internal interface BindingGenerator {
  public fun shouldAcceptClassAnnotation(annotation: AnnotationSpec): Boolean

  public fun shouldAcceptClassField(field: FieldSpec): Boolean

  public fun shouldAcceptClassMethod(method: MethodSpec): Boolean

  public fun shouldAcceptClass(clazz: ClassSpec): Boolean

  public fun shouldGenerateBinding(clazz: ClassSpec, registry: SentoRegistry): Boolean

  public fun onGenerate(clazz: ClassSpec, registry: SentoRegistry): ByteArray
}
