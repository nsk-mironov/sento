package io.sento.compiler.model

import io.sento.compiler.GenerationEnvironment
import io.sento.compiler.SentoException
import io.sento.compiler.common.Types
import io.sento.compiler.common.simpleName
import io.sento.compiler.reflect.AnnotationSpec
import io.sento.compiler.reflect.ClassSpec
import io.sento.compiler.reflect.MethodSpec
import org.objectweb.asm.Type
import java.util.ArrayList
import java.util.LinkedHashSet

internal data class ListenerTargetSpec private constructor(
    val clazz: ClassSpec,
    val method: MethodSpec,
    val annotation: AnnotationSpec,
    val listener: ListenerClassSpec,
    val views: Collection<ViewSpec>,
    val arguments: Collection<ArgumentSpec>,
    val type: Type
) {
  companion object {
    fun create(clazz: ClassSpec, method: MethodSpec, annotation: AnnotationSpec, optional: Boolean, environment: GenerationEnvironment): ListenerTargetSpec {
      val listener = environment.registry.resolveListenerClassSpec(annotation)!!
      val binding = environment.naming.getBindingType(clazz)

      val type = environment.naming.getAnonymousType(Type.getObjectType("${binding.internalName}\$${method.name}"))
      val arguments = remapMethodArguments(clazz, method, annotation, listener, environment)

      val views = annotation.value<IntArray>("value").map {
        ViewSpec(it, optional, clazz, ViewOwner.from(method))
      }

      if (method.returns !in listOf(Types.VOID, Types.BOOLEAN)) {
        throw SentoException("Unable to generate @{0} binding for ''{1}#{2}'' method - it returns ''{3}'', but only {4} are supported.",
            annotation.type.simpleName, clazz.type.className, method.name, method.returns.className, listOf(Types.VOID.className, Types.BOOLEAN.className))
      }

      return ListenerTargetSpec(clazz, method, annotation, listener, views, arguments, type)
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
