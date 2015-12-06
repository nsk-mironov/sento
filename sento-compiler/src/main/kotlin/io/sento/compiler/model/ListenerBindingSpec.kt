package io.sento.compiler.model

import io.sento.compiler.GenerationEnvironment
import io.sento.compiler.SentoException
import io.sento.compiler.common.Types
import io.sento.compiler.common.simpleName
import org.objectweb.asm.Type
import java.util.ArrayList
import java.util.LinkedHashSet

internal data class ListenerBindingSpec private constructor (
    public val target: ListenerTargetSpec,
    public val descriptor: ListenerClassSpec,
    public val args: Collection<ListenerBindingSpec.ArgumentSpec>,
    public val type: Type
) {
  public companion object {
    public fun create(target: ListenerTargetSpec, binding: ListenerClassSpec, environment: GenerationEnvironment): ListenerBindingSpec {
      val type = environment.naming.getAnonymousType(environment.naming.getSentoBindingType(target.clazz.type))
      val args = remapMethodArgs(target, binding, environment)

      if (target.method.returns !in listOf(Types.VOID, Types.BOOLEAN)) {
        throw SentoException("Unable to generate @{0} binding for ''{1}#{2}'' method - it returns ''{3}'', but only {4} are supported.",
            target.annotation.type.simpleName, target.clazz.type.className, target.method.name, target.method.returns.className, listOf(Types.VOID.className, Types.BOOLEAN.className))
      }

      return ListenerBindingSpec(target, binding, args, type)
    }

    private fun remapMethodArgs(target: ListenerTargetSpec, binding: ListenerClassSpec, environment: GenerationEnvironment): Collection<ArgumentSpec> {
      val result = ArrayList<ArgumentSpec>()
      val available = LinkedHashSet<Int>()

      val from = binding.callback
      val to = target.method

      for (index in 0..from.arguments.size - 1) {
        available.add(index)
      }

      for (argument in to.arguments) {
        val index = available.firstOrNull {
          available.contains(it) && environment.registry.isCastableFromTo(from.arguments[it], argument)
        }

        if (index == null) {
          throw SentoException("Unable to generate @{0} binding for ''{1}#{2}'' method - argument ''{3}'' didn''t match any listener parameters.",
              target.annotation.type.simpleName, target.clazz.type.className, target.method.name, argument.className)
        }

        result.add(ArgumentSpec(index, argument))
        available.remove(index)
      }

      return result
    }
  }

  public data class ArgumentSpec(
      public val index: Int,
      public val type: Type
  )
}
