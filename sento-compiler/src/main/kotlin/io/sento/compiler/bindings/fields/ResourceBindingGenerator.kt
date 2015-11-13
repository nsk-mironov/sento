package io.sento.compiler.bindings.fields

import io.sento.compiler.GeneratedContent
import io.sento.compiler.GenerationEnvironment
import io.sento.compiler.common.Annotations
import io.sento.compiler.common.Methods
import io.sento.compiler.common.Types
import io.sento.compiler.model.ResourceBindingSpec

internal class ResourceBindingGenerator(
    private val bindings: Collection<ResourceBindingSpec>
) : FieldBindingGenerator {
  override fun bind(context: FieldBindingContext, environment: GenerationEnvironment): List<GeneratedContent> {
    val adapter = context.adapter
    val annotation = context.annotation

    val field = context.field
    val clazz = context.clazz

    val binding = bindings.firstOrNull {
      environment.registry.isSubclassOf(field.type, it.type)
    }

    environment.debug("Generating @{0} binding for \"{1}#{2}\" field",
        annotation.type.className, clazz.type.className, field.name)

    if (binding == null) {
      environment.fatal("Unable to generate @{0} binding for \"{1}#{2}\" field - its type is \"{3}\", but only the following types are supported {4}.",
          annotation.type.className, clazz.type.className, field.name, field.type.className, bindings.map { it.type.className })
    }

    adapter.loadArg(context.variable("target"))
    adapter.loadArg(context.variable("finder"))
    adapter.loadArg(context.variable("source"))

    adapter.invokeInterface(Types.FINDER, Methods.get("resources", Types.RESOURCES, Types.OBJECT))
    adapter.push(Annotations.id(annotation))

    adapter.invokeVirtual(Types.RESOURCES, Methods.get(binding!!.getter))
    adapter.putField(clazz.type, field.name, field.type)

    return emptyList()
  }
}