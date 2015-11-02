package io.sento.compiler.bindings.views

import io.sento.OnClick
import io.sento.compiler.api.GenerationEnvironment
import io.sento.compiler.bindings.MethodBindingContext
import io.sento.compiler.bindings.SimpleMethodBindingGenerator

public class OnClickBindingGenerator : SimpleMethodBindingGenerator<OnClick>() {
  override fun onBind(context: MethodBindingContext<OnClick>, environment: GenerationEnvironment) {
    super.onBind(context, environment)
  }
}
