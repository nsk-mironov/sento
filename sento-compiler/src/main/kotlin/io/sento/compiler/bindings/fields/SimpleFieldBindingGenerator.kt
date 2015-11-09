package io.sento.compiler.bindings.fields

import io.sento.compiler.GeneratedContent
import io.sento.compiler.GenerationEnvironment

internal open class SimpleFieldBindingGenerator : FieldBindingGenerator {
  override final fun bind(context: FieldBindingContext, environment: GenerationEnvironment): List<GeneratedContent> {
    return super.bind(context, environment).apply {
      onBind(context, environment)
    }
  }

  override final fun unbind(context: FieldBindingContext, environment: GenerationEnvironment): List<GeneratedContent> {
    return super.unbind(context, environment).apply {
      onUnbind(context, environment)
    }
  }

  protected open fun onBind(context: FieldBindingContext, environment: GenerationEnvironment) {
    // nothing to do by default
  }

  protected open fun onUnbind(context: FieldBindingContext, environment: GenerationEnvironment) {
    // nothing to do by default
  }
}
