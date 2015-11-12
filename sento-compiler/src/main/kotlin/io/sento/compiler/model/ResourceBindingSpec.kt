package io.sento.compiler.model

import org.objectweb.asm.Type

internal data class ResourceBindingSpec(
    public val type: Type,
    public val getter: String
)
