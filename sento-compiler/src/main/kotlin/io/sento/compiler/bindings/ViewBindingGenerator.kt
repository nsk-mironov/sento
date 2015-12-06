package io.sento.compiler.bindings

import io.sento.compiler.GenerationEnvironment
import io.sento.compiler.SentoException
import io.sento.compiler.annotations.id
import io.sento.compiler.common.Types
import io.sento.compiler.common.isInterface
import io.sento.compiler.common.simpleName
import org.objectweb.asm.Type
import org.slf4j.LoggerFactory

internal class ViewBindingGenerator {
  private val logger = LoggerFactory.getLogger(ViewBindingGenerator::class.java)

  public fun bind(context: ViewBindingContext, environment: GenerationEnvironment) {
    logger.info("Generating @{} binder for '{}' field",
        context.target.annotation.type.simpleName, context.target.field.name)

    if (context.target.field.type.sort == Type.ARRAY) {
      throw SentoException("Unable to generate @{0} binding for ''{1}#{2}\'' field - arrays are not supported, but ''{3}'' was found.",
          context.target.annotation.type.simpleName, context.target.clazz.type.className, context.target.field.name, context.target.field.type.className)
    }

    val isView = environment.registry.isSubclassOf(context.target.field.type, Types.VIEW)
    val isInterface = environment.registry.reference(context.target.field.type).access.isInterface

    if (!isInterface && !isView) {
      throw SentoException("Unable to generate @{0} binding for ''{1}#{2}\'' field - it must be a subclass of ''{3}'' or an interface, but ''{4}'' was found.",
          context.target.annotation.type.simpleName, context.target.clazz.type.className, context.target.field.name, Types.VIEW.className, context.target.field.type.className)
    }

    context.adapter.loadLocal(context.variable("target"))
    context.adapter.loadLocal(context.variable("view${context.target.annotation.id}"))

    if (context.target.field.type != Types.VIEW) {
      context.adapter.checkCast(context.target.field.type)
    }

    context.adapter.putField(context.target.clazz, context.target.field)
  }

  public fun unbind(context: ViewBindingContext, environment: GenerationEnvironment) {
    logger.info("Generating @{} unbinder for '{}' field",
        context.target.annotation.type.simpleName, context.target.field.name)

    context.adapter.loadLocal(context.variable("target"))
    context.adapter.pushNull()
    context.adapter.putField(context.target.clazz, context.target.field)
  }
}
