package io.sento.compiler.bindings

import org.objectweb.asm.Type

public data class ArgumentSpec (
    public val index: Int,
    public val type: Type
)
