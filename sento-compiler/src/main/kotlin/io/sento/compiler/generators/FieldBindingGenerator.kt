package io.sento.compiler.generators

import io.sento.compiler.GenerationEnvironment

public interface FieldBindingGenerator<A : Annotation> {
  public fun bind(context: FieldBindingContext<A>, environment: GenerationEnvironment) {
    // nothing to do by default
  }

  public fun unbind(context: FieldBindingContext<A>, environment: GenerationEnvironment) {
    // nothing to do by default
  }
}
