package io.sento.compiler.bindings

import io.sento.compiler.ContentGenerator
import io.sento.compiler.GenerationEnvironment
import io.sento.compiler.annotations.Bind
import io.sento.compiler.annotations.ListenerClass
import io.sento.compiler.common.Types
import io.sento.compiler.common.isAnnotation
import io.sento.compiler.reflection.ClassSpec
import org.objectweb.asm.Type
import java.util.HashSet

internal class ContentGeneratorFactory private constructor(
    private val environment: GenerationEnvironment,
    private val bindings: Set<Type>,
    private val listeners: Set<Type>
) {
  public companion object {
    public fun from(environment: GenerationEnvironment): ContentGeneratorFactory {
      return ContentGeneratorFactory(environment, createBindingAnnotations(environment), createListenerAnnotations(environment))
    }

    private fun createBindingAnnotations(environment: GenerationEnvironment): Set<Type> {
      return setOf(Types.getAnnotationType(Bind::class.java))
    }

    private fun createListenerAnnotations(environment: GenerationEnvironment): Set<Type> {
      return HashSet<Type>().apply {
        environment.registry.references.forEach {
          if (it.access.isAnnotation && !Types.isSystemClass(it.type)) {
            if (environment.registry.resolve(it).getAnnotation<ListenerClass>() != null) {
              add(it.type)
            }
          }
        }
      }
    }
  }

  public fun createBinding(clazz: ClassSpec): ContentGenerator {
    return SentoBindingContentGenerator(environment, bindings, listeners, clazz)
  }

  public fun createFactory(bindings: Collection<ClassSpec>): ContentGenerator {
    return SentoFactoryContentGenerator(environment, bindings)
  }
}
