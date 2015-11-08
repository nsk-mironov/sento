package io.sento.compiler.model

import io.sento.MethodBinding
import org.objectweb.asm.Type

internal data class MethodBindingSpec(
    public val annotation: Type,
    public val binding: MethodBinding
)
