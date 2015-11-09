package io.sento.compiler.bindings.fields

import io.sento.compiler.GeneratedContent
import io.sento.compiler.GenerationEnvironment

internal interface FieldBindingGenerator {
  public fun bind(context: FieldBindingContext, environment: GenerationEnvironment): List<GeneratedContent> {
    return emptyList()
  }

  public fun unbind(context: FieldBindingContext, environment: GenerationEnvironment): List<GeneratedContent> {
    return emptyList()
  }
}
