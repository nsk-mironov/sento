package io.sento.compiler.bindings

import io.sento.compiler.api.GeneratedContent
import io.sento.compiler.api.GenerationEnvironment

internal interface FieldBindingGenerator<A : Annotation> {
  public fun bind(context: FieldBindingContext<A>, environment: GenerationEnvironment): List<GeneratedContent> {
    return emptyList()
  }

  public fun unbind(context: FieldBindingContext<A>, environment: GenerationEnvironment): List<GeneratedContent> {
    return emptyList()
  }
}
