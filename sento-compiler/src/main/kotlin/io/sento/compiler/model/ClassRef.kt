package io.sento.compiler.model

import org.objectweb.asm.Type

internal data class ClassRef(
    public val type: Type,
    public val parent: Type
)
