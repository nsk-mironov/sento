package io.sento.compiler.model

import io.sento.compiler.reflection.ClassSpec

internal data class ViewSpec(
    public val id: Int,
    public val optional: Boolean,
    public val clazz: ClassSpec,
    public val description: String
)
