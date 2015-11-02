package io.sento.compiler.bindings

import io.sento.compiler.api.GeneratedContent
import io.sento.compiler.api.GenerationEnvironment

internal interface MethodBindingGenerator<A : Annotation> {
  public fun bind(context: MethodBindingContext<A>, environment: GenerationEnvironment): List<GeneratedContent> {
    return emptyList()
  }

  public fun unbind(context: MethodBindingContext<A>, environment: GenerationEnvironment): List<GeneratedContent> {
    return emptyList()
  }
}
