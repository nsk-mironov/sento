package io.sento.compiler.model

import io.sento.compiler.ClassRegistry
import io.sento.compiler.SentoException
import io.sento.compiler.annotations.ListenerClass
import io.sento.compiler.common.Methods
import io.sento.compiler.common.Types
import io.sento.compiler.common.isAbstract
import io.sento.compiler.common.isInterface
import io.sento.compiler.common.isPublic
import io.sento.compiler.common.simpleName
import io.sento.compiler.reflect.ClassSpec
import io.sento.compiler.reflect.MethodSpec
import org.objectweb.asm.Type

internal data class ListenerClassSpec private constructor(
    val owner: ClassSpec,
    val listener: ClassSpec,
    val setter: MethodSpec,
    val unsetter: MethodSpec,
    val callback: MethodSpec
) {
  companion object {
    fun create(annotation: ClassSpec, binding: ListenerClass, registry: ClassRegistry): ListenerClassSpec {
      val ownerSpec = resolveClassSpec(binding.owner(), "owner", annotation, registry)
      val listenerSpec = resolveClassSpec(binding.listener(), "listener", annotation, registry)

      val listenerConstructor = listenerSpec.getConstructor()
      val listenerCallbacks = listenerSpec.methods.filter {
        it.name == binding.callback()
      }

      val listenerSetter = resolveMethodSpec(binding.setter(), annotation, binding, registry)
      val listenerUnsetter = resolveMethodSpec(binding.unsetter() ?: binding.setter(), annotation, binding, registry)

      if (!listenerSpec.isInterface && (listenerConstructor == null || !listenerConstructor.isPublic)) {
        throw SentoException("Unable to process @{0} annotation - listener type ''{1}'' must have a zero-arg constructor with public visibility.",
            annotation.type.simpleName, listenerSpec.type.className)
      }

      if (listenerCallbacks.size == 0) {
        throw SentoException("Unable to process @{0} annotation - listener type ''{1}'' must have exactly one abstract method named ''{2}'', but none was found.",
            annotation.type.simpleName, listenerSpec.type.className, binding.callback())
      }

      if (listenerCallbacks.size > 1) {
        throw SentoException("Unable to process @{0} annotation - listener type ''{1}'' must have exactly one abstract method named ''{2}'', but {3} were found {4}.",
            annotation.type.simpleName, listenerSpec.type.className, binding.callback(), listenerCallbacks.size, listenerCallbacks.map { Methods.asJavaDeclaration(it) })
      }

      listenerSpec.methods.forEach {
        if (it.isAbstract && it.returns !in listOf(Types.VOID, Types.BOOLEAN)) {
          throw SentoException("Unable to process @{0} annotation - listener method ''{1}'' returns ''{2}'', but only {3} are supported.",
              annotation.type.simpleName, it.name, it.returns.className, listOf(Types.VOID.className, Types.BOOLEAN.className))
        }
      }

      return ListenerClassSpec(ownerSpec, listenerSpec, listenerSetter, listenerUnsetter, listenerCallbacks[0])
    }

    private fun resolveClassSpec(name: String, kind: String, annotation: ClassSpec, registry: ClassRegistry): ClassSpec {
      val type = Types.getClassType(name)

      if (type.sort == Type.ARRAY) {
        throw SentoException("Unable to process @{0} annotation - $kind type mustn''t be an array, but ''{1}'' was found.",
            annotation.type.simpleName, type.className)
      }

      if (Types.isPrimitive(type)) {
        throw SentoException("Unable to process @{0} annotation - $kind type mustn''t be a primitive one, but ''{1}'' was found.",
            annotation.type.simpleName, type.className)
      }

      if (!registry.contains(type)) {
        throw SentoException("Unable to process @{0} annotation - $kind type ''{1}'' wasn''t found.",
            annotation.type.simpleName, type.className)
      }

      return registry.resolve(type).apply {
        if (!isPublic) {
          throw SentoException("Unable to process @{0} annotation - $kind type ''{1}'' must be public.",
              annotation.type.simpleName, type.className)
        }
      }
    }

    private fun resolveMethodSpec(name: String, annotation: ClassSpec, binding: ListenerClass, registry: ClassRegistry): MethodSpec {
      val ownerType = Types.getClassType(binding.owner())
      val listenerType = Types.getClassType(binding.listener())

      val ownerSpec = registry.resolve(ownerType)
      val listenerSpec = registry.resolve(listenerType)

      val listenerSetters = ownerSpec.methods.filter {
        it.name == name && it.arguments.size == 1
      }

      if (listenerSetters.size == 0) {
        throw SentoException("Unable to process @{0} annotation - owner type ''{1}'' must have exactly one single-arg method ''{2}'', but none was found.",
            annotation.type.simpleName, ownerType.className, name)
      }

      if (listenerSetters.size > 1) {
        throw SentoException("Unable to process @{0} annotation - owner type ''{1}'' must have exactly one single-arg method ''{2}'', but {3} were found {4}.",
            annotation.type.simpleName, ownerType.className, name, listenerSetters.size, listenerSetters.map { Methods.asJavaDeclaration(it) })
      }

      if (!registry.isSubclassOf(listenerSpec.type, listenerSetters[0].arguments[0])) {
        throw SentoException("Unable to process @{0} annotation - method ''{1}'' doesn''t accept ''{2}'' as an argument. Only subclasses of ''{3}'' are allowed.",
            annotation.type.simpleName, listenerSetters[0].name, listenerSpec.type.className, listenerSetters[0].arguments[0].className)
      }

      registry.resolve(listenerSetters[0].arguments[0]).methods.forEach {
        if (it.isAbstract && it.returns !in listOf(Types.VOID, Types.BOOLEAN)) {
          throw SentoException("Unable to process @{0} annotation - method ''{1}'' returns ''{2}'', but only {3} are supported.",
              annotation.type.simpleName, it.name, it.returns.className, listOf(Types.VOID.className, Types.BOOLEAN.className))
        }
      }

      return listenerSetters[0]
    }
  }
}
