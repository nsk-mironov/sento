package io.sento.compiler.model

import io.sento.compiler.bindings.methods.MethodBindingGenerator
import io.sento.compiler.reflection.AnnotationSpec
import io.sento.compiler.reflection.MethodSpec

internal data class MethodTargetSpec (
    public val method: MethodSpec,
    public val annotation: AnnotationSpec,
    public val generator: MethodBindingGenerator,
    public val optional: Boolean
)
