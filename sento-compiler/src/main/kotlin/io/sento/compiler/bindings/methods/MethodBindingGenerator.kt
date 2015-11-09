package io.sento.compiler.bindings.methods

import io.sento.compiler.GeneratedContent
import io.sento.compiler.GenerationEnvironment

internal interface MethodBindingGenerator {
  public fun bind(context: MethodBindingContext, environment: GenerationEnvironment): List<GeneratedContent> {
    return emptyList()
  }

  public fun unbind(context: MethodBindingContext, environment: GenerationEnvironment): List<GeneratedContent> {
    return emptyList()
  }
}
