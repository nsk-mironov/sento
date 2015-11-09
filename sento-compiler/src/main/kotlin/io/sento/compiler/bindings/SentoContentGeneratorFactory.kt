package io.sento.compiler.bindings

import io.sento.MethodBinding
import io.sento.compiler.ClassRegistry
import io.sento.compiler.ContentGenerator
import io.sento.compiler.GenerationEnvironment
import io.sento.compiler.common.Types
import io.sento.compiler.model.ClassSpec
import io.sento.compiler.model.MethodBindingSpec
import io.sento.compiler.model.SentoBindingSpec
import java.util.ArrayList

internal class SentoContentGeneratorFactory private constructor(

) {
  public companion object {
    public fun from(environment: GenerationEnvironment): SentoContentGeneratorFactory {
      return SentoContentGeneratorFactory()
    }

    private fun resolveMethodBindings(registry: ClassRegistry): Collection<MethodBindingSpec> {
      return ArrayList<MethodBindingSpec>().apply {
        registry.references.forEach {
          if (it.isAnnotation && !Types.isSystemClass(it.type)) {
            val spec = registry.resolve(it)
            val binding = spec.getAnnotation<MethodBinding>()

            if (binding != null) {
              add(MethodBindingSpec(spec, binding))
            }
          }
        }
      }
    }
  }

  public fun createBinding(clazz: ClassSpec): ContentGenerator {
    return SentoBindingContentGenerator(clazz)
  }

  public fun createFactory(bindings: Collection<SentoBindingSpec>): ContentGenerator {
    return SentoFactoryContentGenerator(bindings)
  }
}
