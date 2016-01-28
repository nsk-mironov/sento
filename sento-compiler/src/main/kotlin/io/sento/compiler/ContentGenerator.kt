package io.sento.compiler

internal interface ContentGenerator {
  fun generate(environment: GenerationEnvironment): Collection<GeneratedContent>
}
