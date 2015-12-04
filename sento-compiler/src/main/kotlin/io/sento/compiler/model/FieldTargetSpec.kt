package io.sento.compiler.model

import io.sento.compiler.bindings.fields.FieldBindingGenerator
import io.sento.compiler.reflection.AnnotationSpec
import io.sento.compiler.reflection.FieldSpec

internal data class FieldTargetSpec(
    public val field: FieldSpec,
    public val annotation: AnnotationSpec,
    public val generator: FieldBindingGenerator,
    public val optional: Boolean
)
