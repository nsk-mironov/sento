package io.sento.compiler.generators

import io.sento.BindString
import io.sento.compiler.GenerationEnvironment

public class BindStringBindingGenerator : FieldBindingGenerator<BindString> {
  override fun bind(context: FieldBindingContext<BindString>, environment: GenerationEnvironment) {
    // TODO: implement me!
  }

  override fun unbind(context: FieldBindingContext<BindString>, environment: GenerationEnvironment) {
    // nothing to do
  }
}
