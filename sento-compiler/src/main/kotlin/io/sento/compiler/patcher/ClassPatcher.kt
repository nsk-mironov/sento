package io.sento.compiler.patcher

import io.sento.compiler.model.ClassSpec

internal interface ClassPatcher {
  public fun patch(spec: ClassSpec): ByteArray
}
