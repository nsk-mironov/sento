package io.sento.compiler.bindings

import io.sento.compiler.api.GeneratedContent
import io.sento.compiler.api.GenerationEnvironment

internal open class SimpleMethodBindingGenerator<A : Annotation> : MethodBindingGenerator<A> {
  override final fun bind(context: MethodBindingContext<A>, environment: GenerationEnvironment): List<GeneratedContent> {
    return super.bind(context, environment).apply {
      onBind(context, environment)
    }
  }

  override final fun unbind(context: MethodBindingContext<A>, environment: GenerationEnvironment): List<GeneratedContent> {
    return super.unbind(context, environment).apply {
      onUnbind(context, environment)
    }
  }

  protected open fun onBind(context: MethodBindingContext<A>, environment: GenerationEnvironment) {
    // nothing to do by default
  }

  protected open fun onUnbind(context: MethodBindingContext<A>, environment: GenerationEnvironment) {
    // nothing to do by default
  }
}