package io.sento.compiler.model

import io.sento.compiler.reflect.ClassSpec

internal data class ViewSpec(
    val id: Int,
    val optional: Boolean,
    val clazz: ClassSpec,
    val owner: ViewOwner
)
