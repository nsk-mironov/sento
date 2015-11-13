package io.sento.compiler.model

import io.sento.ResourceBinding
import io.sento.ResourceBindings
import io.sento.compiler.GenerationEnvironment
import io.sento.compiler.common.Types
import org.objectweb.asm.Type

internal data class ResourceBindingSpec(
    public val annotation: ClassSpec,
    public val type: Type,
    public val getter: MethodSpec
) {
  public companion object {
    public fun create(annotation: ClassSpec, binding: ResourceBinding, environment: GenerationEnvironment): ResourceBindingSpec {
      val resources = environment.registry.resolve(Types.RESOURCES)

      val type = Types.getClassType(binding.type)
      val component = Types.getComponentTypeOrSelf(type)

      val method = resources.method(binding.getter, Types.INT)
      val value = annotation.method("value")

      environment.debug("Processing annotation @{0} with binding {1}", annotation.type.className, binding)

      if (value == null) {
        environment.fatal("Unable to process @{0} annotation - it must have a method called value()",
            annotation.type.className)
      }

      if (value!!.type.returnType != Types.INT) {
        environment.fatal("Unable to process @{0} annotation - value() method must return an int",
            annotation.type.className)
      }

      if (!Types.isPrimitive(component) && !environment.registry.contains(component)) {
        environment.fatal("Unable to process @{0} annotation - class \"{1}\" wasn''t found.",
            annotation.type.className, component.className)
      }

      if (method == null) {
        environment.fatal("Unable to process @{0} annotation - method \"{1}#{2}(int)\" wasn''t found.",
            annotation.type.className, Types.RESOURCES.className, binding.getter)
      }

      if (!environment.registry.isSubclassOf(method!!.type.returnType, type)) {
        environment.fatal("Unable to process @{0} annotation - method \"{1}#{2}(int)\" returns a \"{3}\" which is not assignable to \"{4}\"",
            annotation.type.className, Types.RESOURCES.className, binding.getter, method!!.type.returnType.className, type.className)
      }

      return ResourceBindingSpec(annotation, type, method!!)
    }

    public fun create(annotation: ClassSpec, binding: ResourceBindings, environment: GenerationEnvironment): Collection<ResourceBindingSpec> {
      return binding.value.map {
        create(annotation, it, environment)
      }
    }
  }
}
