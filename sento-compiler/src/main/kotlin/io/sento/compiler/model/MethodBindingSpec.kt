package io.sento.compiler.model

import io.sento.MethodBinding

internal data class MethodBindingSpec(
    public val annotation: ClassSpec,
    public val binding: MethodBinding
)
