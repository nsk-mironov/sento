package io.sento.compiler

internal interface ContentGenerator {
  public fun generate(environment: GenerationEnvironment): Collection<GeneratedContent>
}
