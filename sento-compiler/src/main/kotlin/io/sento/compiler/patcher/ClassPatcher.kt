package io.sento.compiler.patcher

internal interface ClassPatcher {
  public fun patch(bytes: ByteArray): ByteArray
}
