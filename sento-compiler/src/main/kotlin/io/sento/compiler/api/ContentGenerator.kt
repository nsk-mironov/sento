package io.sento.compiler.api

internal interface ContentGenerator {
  public fun onGenerateContent(environment: GenerationEnvironment): List<GeneratedContent>
}
