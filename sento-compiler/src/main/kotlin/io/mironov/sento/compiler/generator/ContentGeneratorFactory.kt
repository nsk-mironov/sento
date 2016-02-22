package io.mironov.sento.compiler.generator

import io.mironov.sento.compiler.ContentGenerator
import io.mironov.sento.compiler.GenerationEnvironment
import io.mironov.sento.compiler.model.BindingSpec

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
