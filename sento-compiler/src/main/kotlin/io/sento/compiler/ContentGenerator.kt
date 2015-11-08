package io.sento.compiler

internal interface ContentGenerator {
  public fun onGenerateContent(environment: GenerationEnvironment): List<GeneratedContent>
}
