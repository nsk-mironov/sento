package io.sento.compiler.bindings

import io.sento.Bind
import io.sento.ListenerBinding
import io.sento.ResourceBindings
import io.sento.compiler.ContentGenerator
import io.sento.compiler.GenerationEnvironment
import io.sento.compiler.bindings.fields.FieldBindingGenerator
import io.sento.compiler.bindings.fields.ResourceBindingGenerator
import io.sento.compiler.bindings.fields.ViewBindingGenerator
import io.sento.compiler.bindings.methods.MethodBindingGenerator
import io.sento.compiler.bindings.methods.ListenerBindingGenerator
import io.sento.compiler.common.Types
import io.sento.compiler.common.isAnnotation
import io.sento.compiler.model.ClassSpec
import io.sento.compiler.model.ListenerBindingSpec
import io.sento.compiler.model.ResourceBindingSpec
import io.sento.compiler.model.SentoBindingSpec
import org.objectweb.asm.Type
import java.util.HashMap

internal class SentoContentGeneratorFactory private constructor(
    private val fields: Map<Type, FieldBindingGenerator>,
    private val methods: Map<Type, MethodBindingGenerator>
) {
  public companion object {
    public fun from(environment: GenerationEnvironment): SentoContentGeneratorFactory {
      return SentoContentGeneratorFactory(createFieldBindings(environment), createMethodBindings(environment))
    }

    private fun createFieldBindings(environment: GenerationEnvironment): Map<Type, FieldBindingGenerator> {
      return HashMap<Type, FieldBindingGenerator>().apply {
        put(Types.get<Bind>(), ViewBindingGenerator())

        environment.registry.references.forEach {
          if (it.access.isAnnotation && !Types.isSystemClass(it.type)) {
            val spec = environment.registry.resolve(it)
            val binding = spec.getAnnotation<ResourceBindings>()

            if (binding != null && !binding.value.isEmpty()) {
              put(spec.type, ResourceBindingGenerator(ResourceBindingSpec.create(spec, binding, environment)))
            }
          }
        }
      }
    }

    private fun createMethodBindings(environment: GenerationEnvironment): Map<Type, MethodBindingGenerator> {
      return HashMap<Type, MethodBindingGenerator>().apply {
        environment.registry.references.forEach {
          if (it.access.isAnnotation && !Types.isSystemClass(it.type)) {
            val spec = environment.registry.resolve(it)
            val binding = spec.getAnnotation<ListenerBinding>()

            if (binding != null) {
              put(it.type, ListenerBindingGenerator(ListenerBindingSpec.create(spec, binding, environment)))
            }
          }
        }
      }
    }
  }

  public fun createBinding(clazz: ClassSpec): ContentGenerator {
    return SentoBindingContentGenerator(fields, methods, clazz)
  }

  public fun createFactory(bindings: Collection<SentoBindingSpec>): ContentGenerator {
    return SentoFactoryContentGenerator(bindings)
  }
}
