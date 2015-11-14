package io.sento.compiler.bindings.fields

import io.sento.Optional
import io.sento.compiler.GeneratedContent
import io.sento.compiler.GenerationEnvironment
import io.sento.compiler.SentoException
import io.sento.compiler.common.Annotations
import io.sento.compiler.common.Methods
import io.sento.compiler.common.Types
import io.sento.compiler.common.isInterface
import io.sento.compiler.common.simpleName
import org.objectweb.asm.Type
import org.slf4j.LoggerFactory

internal class ViewBindingGenerator : FieldBindingGenerator {
  private val logger = LoggerFactory.getLogger(ResourceBindingGenerator::class.java)

  override fun bind(context: FieldBindingContext, environment: GenerationEnvironment): List<GeneratedContent> {
    val adapter = context.adapter
    val annotation = context.annotation

    val field = context.field
    val clazz = context.clazz

    logger.info("Generating @{} binding for '{}' field",
        annotation.type.simpleName, field.name)

    if (field.type.sort == Type.ARRAY) {
      throw SentoException("Unable to generate @{0} binding for ''{1}#{2}\'' field - arrays are not supported, but ''{3}'' was found.",
          annotation.type.simpleName, clazz.type.className, field.name, field.type.className)
    }

    val optional = field.getAnnotation<Optional>() != null
    val isView = environment.registry.isSubclassOf(field.type, Types.VIEW)
    val isInterface = environment.registry.reference(field.type).access.isInterface

    if (!isInterface && !isView) {
      throw SentoException("Unable to generate @{0} binding for ''{1}#{2}\'' field - it must be a subclass of ''{3}'' or an interface, but ''{4}'' was found.",
          annotation.type.simpleName, clazz.type.className, field.name, Types.VIEW.className, field.type.className)
    }

    adapter.loadArg(context.variable("target"))
    adapter.loadArg(context.variable("finder"))
    adapter.push(Annotations.id(annotation))
    adapter.loadArg(context.variable("source"))
    adapter.push(optional)

    adapter.invokeInterface(Types.FINDER, Methods.get("find", Types.VIEW, Types.INT, Types.OBJECT, Types.BOOLEAN))
    adapter.checkCast(field.type)
    adapter.putField(clazz.type, field.name, field.type)

    return emptyList()
  }

  override fun unbind(context: FieldBindingContext, environment: GenerationEnvironment): List<GeneratedContent> {
    val adapter = context.adapter
    val field = context.field
    val clazz = context.clazz

    adapter.loadArg(context.variable("target"))
    adapter.push(null as String?)
    adapter.putField(clazz.type, field.name, field.type)

    return emptyList()
  }
}
