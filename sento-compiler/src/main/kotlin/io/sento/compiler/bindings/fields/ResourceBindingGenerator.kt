package io.sento.compiler.bindings.fields

import io.sento.compiler.GeneratedContent
import io.sento.compiler.GenerationEnvironment
import io.sento.compiler.SentoException
import io.sento.compiler.common.Annotations
import io.sento.compiler.common.Methods
import io.sento.compiler.common.Types
import io.sento.compiler.common.simpleName
import io.sento.compiler.model.ResourceBindingSpec
import org.slf4j.LoggerFactory

internal class ResourceBindingGenerator(
    private val bindings: Collection<ResourceBindingSpec>
) : FieldBindingGenerator {
  private val logger = LoggerFactory.getLogger(ResourceBindingGenerator::class.java)

  override fun bind(context: FieldBindingContext, environment: GenerationEnvironment): List<GeneratedContent> {
    logger.info("Generating @{} binding for '{}' field",
        context.annotation.type.simpleName, context.field.name)

    val binding = bindings.firstOrNull {
      environment.registry.isSubclassOf(context.field.type, it.type)
    }

    if (binding == null) {
      throw SentoException("Unable to generate @{0} binding for ''{1}#{2}'' field - its type is ''{3}'', but only the following types are supported {4}.",
          context.annotation.type.simpleName, context.clazz.type.className, context.field.name, context.field.type.className, bindings.map { it.type.className })
    }

    context.adapter.apply {
      loadArg(context.variable("target"))
      loadArg(context.variable("finder"))
      loadArg(context.variable("source"))

      invokeInterface(Types.FINDER, Methods.get("resources", Types.RESOURCES, Types.OBJECT))
      push(Annotations.id(context.annotation))

      invokeVirtual(Types.RESOURCES, Methods.get(binding.getter))
      putField(context.clazz.type, context.field.name, context.field.type)
    }

    return emptyList()
  }
}
