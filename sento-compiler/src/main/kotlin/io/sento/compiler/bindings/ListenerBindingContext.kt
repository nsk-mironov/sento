package io.sento.compiler.bindings

import io.sento.compiler.common.GeneratorAdapter
import io.sento.compiler.model.ListenerBindingSpec

internal class ListenerBindingContext(
    public val binding: ListenerBindingSpec,
    public val adapter: GeneratorAdapter,
    public val variables: VariablesContext
)
