package io.sento.compiler.model

import io.sento.compiler.GenerationEnvironment
import io.sento.compiler.SentoException
import io.sento.compiler.bindings.ArgumentSpec
import io.sento.compiler.common.Types
import io.sento.compiler.common.simpleName
import io.sento.compiler.reflection.AnnotationSpec
import io.sento.compiler.reflection.ClassSpec
import io.sento.compiler.reflection.MethodSpec
import org.objectweb.asm.Type
import java.util.ArrayList
import java.util.LinkedHashSet

internal data class ListenerTargetSpec private constructor(
    public val clazz: ClassSpec,
    public val method: MethodSpec,
    public val annotation: AnnotationSpec,
    public val optional: Boolean,
    public val listener: ListenerClassSpec,
    public val arguments: Collection<ArgumentSpec>,
    public val type: Type
) {
  public companion object {
    public fun create(clazz: ClassSpec, method: MethodSpec, annotation: AnnotationSpec, optional: Boolean, environment: GenerationEnvironment): ListenerTargetSpec {
      val listener = environment.registry.resolveListenerClassSpec(annotation)
      val type = environment.naming.getAnonymousType(environment.naming.getSentoBindingType(clazz))
      val arguments = remapMethodArguments(clazz, method, annotation, listener, environment)

      if (method.returns !in listOf(Types.VOID, Types.BOOLEAN)) {
        throw SentoException("Unable to generate @{0} binding for ''{1}#{2}'' method - it returns ''{3}'', but only {4} are supported.",
            annotation.type.simpleName, clazz.type.className, method.name, method.returns.className, listOf(Types.VOID.className, Types.BOOLEAN.className))
      }

      return ListenerTargetSpec(clazz, method, annotation, optional, listener, arguments, type)
    }

    private fun remapMethodArguments(clazz: ClassSpec, method: MethodSpec, annotation: AnnotationSpec, binding: ListenerClassSpec, environment: GenerationEnvironment): Collection<ArgumentSpec> {
      val result = ArrayList<ArgumentSpec>()
      val available = LinkedHashSet<Int>()

      for (index in 0..binding.callback.arguments.size - 1) {
        available.add(index)
      }

      for (argument in method.arguments) {
        val index = available.firstOrNull {
          available.contains(it) && environment.registry.isCastableFromTo(binding.callback.arguments[it], argument)
        }

        if (index == null) {
          throw SentoException("Unable to generate @{0} binding for ''{1}#{2}'' method - argument ''{3}'' didn''t match any listener parameters.",
              annotation.type.simpleName, clazz.type.className, method.name, argument.className)
        }

        result.add(ArgumentSpec(index, argument))
        available.remove(index)
      }

      return result
    }
  }
}
