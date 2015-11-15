package io.sento.annotations

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.ANNOTATION_CLASS)
annotation class ResourceBindings(vararg val value: ResourceBinding)
