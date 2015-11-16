package io.sento.compiler.model

import io.sento.annotations.ResourceBinding
import io.sento.annotations.ResourceBindings
import io.sento.compiler.GenerationEnvironment
import io.sento.compiler.SentoException
import io.sento.compiler.common.Types
import io.sento.compiler.common.simpleName
import org.objectweb.asm.Type
import org.slf4j.LoggerFactory

internal data class ResourceBindingSpec(
    public val annotation: ClassSpec,
    public val type: Type,
    public val getter: MethodSpec
) {
  public companion object {
    private val logger = LoggerFactory.getLogger(ResourceBindingSpec::class.java)

    public fun create(annotation: ClassSpec, binding: ResourceBinding, environment: GenerationEnvironment): ResourceBindingSpec {
      val resources = environment.registry.resolve(Types.RESOURCES)

      val type = if (binding.array()) Types.getArrayType(binding.type()) else binding.type()
      val component = Types.getComponentTypeOrSelf(type)

      val method = resources.getDeclaredMethod(binding.getter(), Types.INT)
      val value = annotation.getDeclaredMethod("value")

      logger.info("Processing annotation @{} with binding {}",
          annotation.type.simpleName, binding)

      if (value == null) {
        throw SentoException("Unable to process @{0} annotation - it must have a method called value()",
            annotation.type.className)
      }

      if (value.returns != Type.INT_TYPE) {
        throw SentoException("Unable to process @{0} annotation - value() method must return an int, but ''{1}'' was found",
            annotation.type.className, value.returns.className)
      }

      if (!Types.isPrimitive(component) && !environment.registry.contains(component)) {
        throw SentoException("Unable to process @{0} annotation - type ''{1}'' wasn''t found.",
            annotation.type.className, component.className)
      }

      if (method == null) {
        throw SentoException("Unable to process @{0} annotation - method ''{1}#{2}(int)'' wasn''t found.",
            annotation.type.className, Types.RESOURCES.className, binding.getter())
      }

      if (!environment.registry.isSubclassOf(method.returns, type)) {
        throw SentoException("Unable to process @{0} annotation - method ''{1}#{2}(int)'' returns a ''{3}'' which is not assignable to ''{4}''",
            annotation.type.className, Types.RESOURCES.className, binding.getter(), method.returns.className, type.className)
      }

      return ResourceBindingSpec(annotation, type, method)
    }

    public fun create(annotation: ClassSpec, binding: ResourceBindings, environment: GenerationEnvironment): Collection<ResourceBindingSpec> {
      return binding.value().map {
        create(annotation, it, environment)
      }
    }
  }
}
