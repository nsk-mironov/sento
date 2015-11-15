package io.sento.annotations

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.ANNOTATION_CLASS)
annotation class ListenerBinding(val owner: String, val listener: String, val setter: String)
