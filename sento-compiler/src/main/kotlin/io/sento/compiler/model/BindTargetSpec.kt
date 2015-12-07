package io.sento.compiler.model

import io.sento.compiler.bindings.ViewBinder
import io.sento.compiler.reflection.AnnotationSpec
import io.sento.compiler.reflection.ClassSpec
import io.sento.compiler.reflection.FieldSpec

internal data class BindTargetSpec(
    public val clazz: ClassSpec,
    public val field: FieldSpec,
    public val annotation: AnnotationSpec,
    public val generator: ViewBinder,
    public val optional: Boolean
)
