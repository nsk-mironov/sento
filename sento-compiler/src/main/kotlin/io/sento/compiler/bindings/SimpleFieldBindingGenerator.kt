package io.sento.compiler.bindings

import io.sento.compiler.GeneratedContent
import io.sento.compiler.GenerationEnvironment

internal open class SimpleFieldBindingGenerator<A : Annotation> : FieldBindingGenerator<A> {
  override final fun bind(context: FieldBindingContext<A>, environment: GenerationEnvironment): List<GeneratedContent> {
    return super.bind(context, environment).apply {
      onBind(context, environment)
    }
  }

  override final fun unbind(context: FieldBindingContext<A>, environment: GenerationEnvironment): List<GeneratedContent> {
    return super.unbind(context, environment).apply {
      onUnbind(context, environment)
    }
  }

  protected open fun onBind(context: FieldBindingContext<A>, environment: GenerationEnvironment) {
    // nothing to do by default
  }

  protected open fun onUnbind(context: FieldBindingContext<A>, environment: GenerationEnvironment) {
    // nothing to do by default
  }
}