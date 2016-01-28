package io.sento.compiler.generator

import io.sento.compiler.ContentGenerator
import io.sento.compiler.GenerationEnvironment
import io.sento.compiler.model.BindingSpec

internal class ContentGeneratorFactory private constructor() {
  companion object {
    fun from(environment: GenerationEnvironment): ContentGeneratorFactory {
      return ContentGeneratorFactory()
    }
  }

  fun createBinding(binding: BindingSpec): ContentGenerator {
    return SentoBindingContentGenerator(binding)
  }

  fun createFactory(bindings: Collection<BindingSpec>): ContentGenerator {
    return SentoFactoryContentGenerator(bindings)
  }
}
