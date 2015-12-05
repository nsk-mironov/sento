package io.sento.compiler.model

import io.sento.compiler.GenerationEnvironment
import io.sento.compiler.SentoException
import io.sento.compiler.annotations.ListenerClass
import io.sento.compiler.common.Methods
import io.sento.compiler.common.Types
import io.sento.compiler.common.isAbstract
import io.sento.compiler.common.isInterface
import io.sento.compiler.common.isPrivate
import io.sento.compiler.common.isPublic
import io.sento.compiler.common.simpleName
import io.sento.compiler.reflection.ClassSpec
import io.sento.compiler.reflection.MethodSpec
import org.objectweb.asm.Type

internal data class ListenerClassSpec private constructor(
    public val owner: ClassSpec,
    public val listener: ClassSpec,
    public val setter: MethodSpec,
    public val unsetter: MethodSpec,
    public val callback: MethodSpec
) {
  public companion object {
    public fun create(annotation: ClassSpec, binding: ListenerClass, environment: GenerationEnvironment): ListenerClassSpec {
      val ownerType = binding.owner()
      val listenerType = binding.listener()

      if (ownerType.sort == Type.ARRAY) {
        throw SentoException("Unable to process @{0} annotation - owner type mustn''t be an array, but ''{1}'' was found.",
            annotation.type.simpleName, ownerType.className)
      }

      if (Types.isPrimitive(ownerType)) {
        throw SentoException("Unable to process @{0} annotation - owner type mustn''t be a primitive one, but ''{1}'' was found.",
            annotation.type.simpleName, ownerType.className)
      }

      if (!environment.registry.contains(ownerType)) {
        throw SentoException("Unable to process @{0} annotation - owner type ''{1}'' wasn''t found.",
            annotation.type.simpleName, ownerType.className)
      }

      if (listenerType.sort == Type.ARRAY) {
        throw SentoException("Unable to process @{0} annotation - listener type mustn''t be an array, but ''{1}'' was found.",
            annotation.type.simpleName, listenerType.className)
      }

      if (Types.isPrimitive(listenerType)) {
        throw SentoException("Unable to process @{0} annotation - listener type mustn''t be a primitive one, but ''{1}'' was found.",
            annotation.type.simpleName, listenerType.className)
      }

      if (!environment.registry.contains(listenerType)) {
        throw SentoException("Unable to process @{0} annotation - listener type ''{1}'' wasn''t found.",
            annotation.type.simpleName, listenerType.className)
      }

      val ownerSpec = environment.registry.resolve(ownerType)
      val listenerSpec = environment.registry.resolve(listenerType)

      if (!ownerSpec.access.isPublic) {
        throw SentoException("Unable to process @{0} annotation - owner type ''{1}'' must be public.",
            annotation.type.simpleName, ownerType.className)
      }

      if (!listenerSpec.access.isPublic) {
        throw SentoException("Unable to process @{0} annotation - listener type ''{1}'' must be public.",
            annotation.type.simpleName, listenerType.className)
      }

      val listenerConstructor = listenerSpec.getConstructor()
      val listenerCallbacks = listenerSpec.methods.filter {
        it.name == binding.callback()
      }

      val listenerSetters = ownerSpec.methods.filter {
        it.name == binding.setter() && it.arguments.size == 1
      }

      if (!listenerSpec.access.isInterface && (listenerConstructor == null || listenerConstructor.access.isPrivate)) {
        throw SentoException("Unable to process @{0} annotation - listener type ''{1}'' must have a zero-arg constructor with public or protected visibility.",
            annotation.type.simpleName, listenerType.className)
      }

      if (listenerCallbacks.size == 0) {
        throw SentoException("Unable to process @{0} annotation - listener type ''{1}'' must have exactly one abstract method named ''{2}'', but none was found.",
            annotation.type.simpleName, listenerType.className, binding.callback())
      }

      if (listenerCallbacks.size > 1) {
        throw SentoException("Unable to process @{0} annotation - listener type ''{1}'' must have exactly one abstract method named ''{2}'', but {3} were found {4}.",
            annotation.type.simpleName, listenerType.className, binding.callback(), listenerCallbacks.size, listenerCallbacks.map { Methods.asJavaDeclaration(it) })
      }

      if (listenerSetters.size == 0) {
        throw SentoException("Unable to process @{0} annotation - owner type ''{1}'' must have exactly one single-arg method ''{2}'', but none was found.",
            annotation.type.simpleName, ownerType.className, binding.setter())
      }

      if (listenerSetters.size > 1) {
        throw SentoException("Unable to process @{0} annotation - owner type ''{1}'' must have exactly one single-arg method ''{2}'', but {3} were found {4}.",
            annotation.type.simpleName, ownerType.className, binding.setter(), listenerSetters.size, listenerSetters.map { Methods.asJavaDeclaration(it) })
      }

      if (!environment.registry.isSubclassOf(listenerSpec.type, listenerSetters[0].arguments[0])) {
        throw SentoException("Unable to process @{0} annotation - listener setter ''{1}'' doesn''t accept ''{2}'' as an argument. Only subclasses of ''{3}'' are allowed.",
            annotation.type.simpleName, listenerSetters[0].name, listenerSpec.type.className, listenerSetters[0].arguments[0].className)
      }

      listenerSpec.methods.forEach {
        if (it.access.isAbstract && it.returns !in listOf(Types.VOID, Types.BOOLEAN)) {
          throw SentoException("Unable to process @{0} annotation - listener method ''{1}'' returns ''{2}'', but only {3} are supported.",
              annotation.type.simpleName, it.name, it.returns.className, listOf(Types.VOID.className, Types.BOOLEAN.className))
        }
      }

      environment.registry.resolve(listenerSetters[0].arguments[0]).methods.forEach {
        if (it.access.isAbstract && it.returns !in listOf(Types.VOID, Types.BOOLEAN)) {
          throw SentoException("Unable to process @{0} annotation - listener method ''{1}'' returns ''{2}'', but only {3} are supported.",
              annotation.type.simpleName, it.name, it.returns.className, listOf(Types.VOID.className, Types.BOOLEAN.className))
        }
      }

      return ListenerClassSpec(ownerSpec, listenerSpec, listenerSetters[0], listenerSetters[0], listenerCallbacks[0])
    }
  }
}
