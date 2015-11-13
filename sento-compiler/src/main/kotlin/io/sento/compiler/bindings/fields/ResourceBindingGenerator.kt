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
    val adapter = context.adapter
    val annotation = context.annotation

    val field = context.field
    val clazz = context.clazz

    val binding = bindings.firstOrNull {
      environment.registry.isSubclassOf(field.type, it.type)
    }

    logger.info("Generating @{} binding for '{}' field",
        annotation.type.simpleName, field.name)

    if (binding == null) {
      throw SentoException("Unable to generate @{0} binding for ''{1}#{2}'' field - its type is ''{3}'', but only the following types are supported {4}.",
          annotation.type.simpleName, clazz.type.className, field.name, field.type.className, bindings.map { it.type.className })
    }

    adapter.loadArg(context.variable("target"))
    adapter.loadArg(context.variable("finder"))
    adapter.loadArg(context.variable("source"))

    adapter.invokeInterface(Types.FINDER, Methods.get("resources", Types.RESOURCES, Types.OBJECT))
    adapter.push(Annotations.id(annotation))

    adapter.invokeVirtual(Types.RESOURCES, Methods.get(binding.getter))
    adapter.putField(clazz.type, field.name, field.type)

    return emptyList()
  }
}