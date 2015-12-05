package io.sento.compiler.bindings

import io.sento.compiler.ContentGenerator
import io.sento.compiler.GenerationEnvironment
import io.sento.compiler.annotations.Bind
import io.sento.compiler.annotations.ListenerClass
import io.sento.compiler.bindings.fields.ViewBindingGenerator
import io.sento.compiler.bindings.methods.ListenerBindingGenerator
import io.sento.compiler.common.Types
import io.sento.compiler.common.isAnnotation
import io.sento.compiler.common.simpleName
import io.sento.compiler.model.ListenerClassSpec
import io.sento.compiler.reflection.ClassSpec
import org.objectweb.asm.Type
import org.slf4j.LoggerFactory
import java.util.HashMap

internal class ContentGeneratorFactory private constructor(
    private val fields: Map<Type, ViewBindingGenerator>,
    private val methods: Map<Type, ListenerBindingGenerator>
) {
  public companion object {
    private val logger = LoggerFactory.getLogger(ContentGeneratorFactory::class.java)

    public fun from(environment: GenerationEnvironment): ContentGeneratorFactory {
      return ContentGeneratorFactory(createViewBindings(environment), createMethodBindings(environment))
    }

    private fun createViewBindings(environment: GenerationEnvironment): Map<Type, ViewBindingGenerator> {
      return HashMap<Type, ViewBindingGenerator>().apply {
        put(Types.getAnnotationType(Bind::class.java), ViewBindingGenerator())
      }
    }

    private fun createMethodBindings(environment: GenerationEnvironment): Map<Type, ListenerBindingGenerator> {
      return HashMap<Type, ListenerBindingGenerator>().apply {
        environment.registry.references.forEach {
          if (it.access.isAnnotation && !Types.isSystemClass(it.type)) {
            val spec = environment.registry.resolve(it)
            val listener = spec.getAnnotation<ListenerClass>()

            if (listener != null) {
              logger.info("New ListenerBinding found - @{} with listener {}", spec.type.simpleName, listener)
              logger.info("Creating a ListenerBindingGenerator for @{}", spec.type.simpleName)

              put(it.type, ListenerBindingGenerator(ListenerClassSpec.create(spec, listener, environment)))
            }
          }
        }
      }
    }
  }

  public fun createBinding(clazz: ClassSpec): ContentGenerator {
    return SentoBindingContentGenerator(fields, methods, clazz)
  }

  public fun createFactory(bindings: Collection<ClassSpec>): ContentGenerator {
    return SentoFactoryContentGenerator(bindings)
  }
}
