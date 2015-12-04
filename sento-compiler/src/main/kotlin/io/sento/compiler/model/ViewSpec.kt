package io.sento.compiler.model

internal data class ViewSpec(
    public val id: Int,
    public val optional: Boolean,
    public val owner: String
)
