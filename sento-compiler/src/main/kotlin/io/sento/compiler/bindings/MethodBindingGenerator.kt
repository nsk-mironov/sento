package io.sento.compiler.bindings

import io.sento.compiler.GeneratedContent
import io.sento.compiler.GenerationEnvironment

internal interface MethodBindingGenerator<A : Annotation> {
  public fun bind(context: MethodBindingContext<A>, environment: GenerationEnvironment): List<GeneratedContent> {
    return emptyList()
  }

  public fun unbind(context: MethodBindingContext<A>, environment: GenerationEnvironment): List<GeneratedContent> {
    return emptyList()
  }
}
