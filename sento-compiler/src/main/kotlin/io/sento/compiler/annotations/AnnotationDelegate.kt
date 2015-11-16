package io.sento.compiler.annotations

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class AnnotationDelegate(val value: String)
