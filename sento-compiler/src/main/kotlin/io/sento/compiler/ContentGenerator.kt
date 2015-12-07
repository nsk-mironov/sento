package io.sento.compiler

internal interface ContentGenerator {
  public fun generate(): Collection<GeneratedContent>
}
