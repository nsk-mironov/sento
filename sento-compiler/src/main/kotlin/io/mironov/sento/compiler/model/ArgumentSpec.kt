package io.mironov.sento.compiler.model

import org.objectweb.asm.Type

data class ArgumentSpec (
    val index: Int,
    val type: Type
)
