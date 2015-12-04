package io.sento.compiler.model

import io.sento.compiler.bindings.methods.MethodBindingGenerator

internal data class MethodTargetSpec (
    public val method: MethodSpec,
    public val annotation: AnnotationSpec,
    public val generator: MethodBindingGenerator,
    public val optional: Boolean
)
