package io.sento.compiler.model

import io.sento.compiler.bindings.fields.FieldBindingGenerator

internal data class FieldTargetSpec(
    public val field: FieldSpec,
    public val annotation: AnnotationSpec,
    public val generator: FieldBindingGenerator,
    public val optional: Boolean
)
