package io.sento.compiler.generators

import io.sento.compiler.GenerationEnvironment

public interface FieldBindingGenerator<A : Annotation> {
  public fun bind(context: FieldBindingContext<A>, environment: GenerationEnvironment)

  public fun unbind(context: FieldBindingContext<A>, environment: GenerationEnvironment)
}
