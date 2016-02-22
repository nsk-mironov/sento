package io.mironov.sento.compiler.model

import io.mironov.sento.compiler.reflect.ClassSpec

internal data class ViewSpec(
    val id: Int,
    val optional: Boolean,
    val clazz: ClassSpec,
    val owner: ViewOwner
)
