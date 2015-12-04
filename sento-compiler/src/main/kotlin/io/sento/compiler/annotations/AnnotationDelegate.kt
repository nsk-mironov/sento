package io.sento.compiler.annotations

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
internal annotation class AnnotationDelegate(val value: String)
