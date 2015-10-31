package io.sento.compiler.api

import io.sento.compiler.specs.ClassSpec

internal interface ContentGenerator {
  public fun onGenerateContent(clazz: ClassSpec, environment: GenerationEnvironment): List<GeneratedContent>
}
